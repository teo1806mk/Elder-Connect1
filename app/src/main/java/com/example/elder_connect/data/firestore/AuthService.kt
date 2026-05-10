package com.example.elder_connect.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Service για Firebase Authentication + User Profiles στη Firestore.
 *
 * Λειτουργίες:
 * - Login / Register / Logout
 * - Δημιουργία profile στη Firestore μετά το register
 * - Δημιουργία Elder profile από Caregiver (με pairing code)
 * - Login ELDER με pairing code
 */
class AuthService {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersRef = firestore.collection("users")

    /** Ο τρέχων logged-in χρήστης (ή null) */
    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Reactive Flow για auth state changes.
     * Επιστρέφει τον FirebaseUser ή null αν δεν είναι logged in.
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Reactive Flow για το profile του τρέχοντα χρήστη από Firestore.
     */
    fun currentUserProfileFlow(): Flow<UserProfile?> = callbackFlow {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = usersRef.document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }

        awaitClose { listener.remove() }
    }

    // =================================================================
    //                      LOGIN / REGISTER
    // =================================================================

    /**
     * Login με email + password.
     */
    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")
            val profile = getUserProfile(uid).getOrThrow()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register νέου ADMIN ή USER (Caregiver).
     * Δημιουργεί Firebase Auth account + profile στη Firestore.
     */
    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        age: Int,
        city: String,
        role: UserRole
    ): Result<UserProfile> {
        return try {
            require(role != UserRole.ELDER) {
                "Οι ELDER λογαριασμοί δημιουργούνται μόνο από caregiver"
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Register failed")

            val profile = UserProfile(
                uid = uid,
                email = email,
                fullName = fullName,
                age = age,
                city = city,
                role = role.key
            )

            usersRef.document(uid).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Δημιουργία ELDER profile από Caregiver.
     *
     * ΔΕΝ δημιουργεί Firebase Auth user — ο elder θα κάνει login
     * με τον pairing code, όχι με password.
     *
     * Επιστρέφει τον unique pairing code ώστε να τον δώσει
     * ο caregiver στον elder.
     */
    suspend fun createElderProfile(
        caregiverId: String,
        elderName: String,
        elderAge: Int,
        elderCity: String
    ): Result<UserProfile> {
        return try {
            val pairingCode = generatePairingCode()
            // Χρησιμοποιούμε pairing code ως uid για να βρίσκεται εύκολα
            val uid = "elder_$pairingCode"

            val profile = UserProfile(
                uid = uid,
                email = "",  // Elders δεν έχουν email
                fullName = elderName,
                age = elderAge,
                city = elderCity,
                role = UserRole.ELDER.key,
                caregiverId = caregiverId,
                pairingCode = pairingCode
            )

            usersRef.document(uid).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login ELDER με τον pairing code.
     *
     * Δεν χρησιμοποιεί Firebase Auth — απλά "διαβάζει" το profile
     * από τη Firestore με τον κωδικό.
     */
    suspend fun loginElderWithCode(pairingCode: String): Result<UserProfile> {
        return try {
            val cleanCode = pairingCode.trim().uppercase()
            val docId = "elder_$cleanCode"

            val doc = usersRef.document(docId).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: throw Exception("Λάθος κωδικός")

            if (profile.role != UserRole.ELDER.key) {
                throw Exception("Λάθος κωδικός")
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout.
     */
    fun logout() {
        auth.signOut()
    }

    // =================================================================
    //                      USER PROFILE
    // =================================================================

    suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            val doc = usersRef.document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: throw Exception("Δεν βρέθηκε profile")
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersRef.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ο caregiver παίρνει λίστα με τους elders που διαχειρίζεται.
     */
    fun getMyElders(caregiverId: String): Flow<List<UserProfile>> = callbackFlow {
        val listener = usersRef
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("role", UserRole.ELDER.key)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val elders = snapshot?.documents?.mapNotNull {
                    it.toObject(UserProfile::class.java)
                } ?: emptyList()
                trySend(elders)
            }
        awaitClose { listener.remove() }
    }

    // =================================================================
    //                      HELPERS
    // =================================================================

    /**
     * Δημιουργεί έναν 6-ψήφιο pairing code (γράμματα + αριθμοί).
     * π.χ. "A3B7K9"
     */
    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  // χωρίς I, O, 0, 1 (μπερδεύονται)
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}