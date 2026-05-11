package com.example.elder_connect.data.sync

import com.example.elder_connect.data.dao.UserDao
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.firestore.AuthService
import com.example.elder_connect.data.firestore.UserProfile

/**
 * Συγχρονίζει τους Firebase/Elder users με τη Room database.
 *
 * Λειτουργία:
 * - Όταν κάνει login ένας χρήστης, ελέγχουμε αν υπάρχει στη Room
 * - Αν όχι, δημιουργούμε νέο Room record
 * - Αν ναι, ενημερώνουμε τα στοιχεία του
 *
 * ΣΗΜΕΙΩΣΗ: Το "firebaseUid" για Elder χρήστες είναι το "elder_XXXXXX" (pairing code),
 * όχι πραγματικό Firebase Auth UID.
 */
class UserSyncService(
    private val userDao: UserDao,
    val authService: AuthService
) {

    /**
     * Συγχρονίζει έναν χρήστη με τη Room.
     *
     * @param uid Το UID (Firebase Auth UID για Admin/User, "elder_CODE" για Elder)
     * @param profile Το profile από τη Firestore
     * @return Το Room [User] object (με το generated id)
     */
    suspend fun syncUser(uid: String, profile: UserProfile): User {
        val existingUser = userDao.findByFirebaseUid(uid)

        return if (existingUser != null) {
            // Υπάρχει — ενημερώνουμε τα στοιχεία
            val updated = existingUser.copy(
                fullName = profile.fullName,
                email = profile.email.ifBlank { null },
                profilePictureUri = profile.photoUrl.ifBlank { null }
            )
            userDao.updateUser(updated)
            updated
        } else {
            // Δεν υπάρχει — δημιουργούμε νέο
            val newUser = User(
                firebaseUid = uid,
                fullName = profile.fullName,
                email = profile.email.ifBlank { null },
                profilePictureUri = profile.photoUrl.ifBlank { null },
                createdAt = System.currentTimeMillis()
            )
            val id = userDao.insertUser(newUser)
            newUser.copy(id = id)
        }
    }

    /**
     * Βρίσκει Room user από UID.
     */
    suspend fun getRoomUserByUid(uid: String): User? {
        return userDao.findByFirebaseUid(uid)
    }

    /**
     * Επιστρέφει τον τρέχοντα logged-in user (μόνο για Firebase Auth users).
     * Για Elder users χρησιμοποίησε getRoomUserByUid με το pairing code UID.
     */
    suspend fun getCurrentLocalUser(): User? {
        val firebaseUser = authService.currentFirebaseUser ?: return null
        return userDao.findByFirebaseUid(firebaseUser.uid)
    }

    /**
     * Διαγράφει τον local user (logout cleanup)
     */
    suspend fun clearLocalUser(uid: String) {
        val user = userDao.findByFirebaseUid(uid)
        if (user != null) {
            userDao.deleteUser(user)
        }
    }
}
