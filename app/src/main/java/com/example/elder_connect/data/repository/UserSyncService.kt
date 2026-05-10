package com.example.elder_connect.data.sync

import com.example.elder_connect.data.dao.UserDao
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.firestore.AuthService
import com.example.elder_connect.data.firestore.UserProfile
import kotlinx.coroutines.flow.first

/**
 * Συγχρονίζει τους Firebase users με τη Room database.
 *
 * Λειτουργία:
 * - Όταν κάνει login ένας χρήστης, ελέγχουμε αν υπάρχει στη Room
 * - Αν όχι, δημιουργούμε νέο Room record
 * - Αν ναι, ενημερώνουμε τα στοιχεία του
 */
class UserSyncService(
    private val userDao: UserDao,
    private val authService: AuthService
) {

    /**
     * Συγχρονίζει έναν Firebase user με τη Room.
     *
     * @param firebaseUid Το UID από το Firebase Auth
     * @param profile Το profile από τη Firestore
     * @return Το Room user ID (για χρήση σε foreign keys)
     */
    suspend fun syncUser(firebaseUid: String, profile: UserProfile): Long {
        // Ψάχνουμε αν υπάρχει ήδη
        val existingUser = userDao.findByFirebaseUid(firebaseUid)

        return if (existingUser != null) {
            // Υπάρχει - ενημερώνουμε τα στοιχεία του
            val updated = existingUser.copy(
                fullName = profile.fullName,
                email = profile.email
            )
            userDao.updateUser(updated)
            existingUser.id
        } else {
            // Δεν υπάρχει - δημιουργούμε νέο
            val newUser = User(
                firebaseUid = firebaseUid,
                fullName = profile.fullName,
                email = profile.email,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser)
        }
    }

    /**
     * Βρίσκει το Room user ID για έναν Firebase user.
     * Χρήσιμο για ViewModels που χρειάζονται το Room ID.
     */
    suspend fun getRoomUserId(firebaseUid: String): Long? {
        return userDao.findByFirebaseUid(firebaseUid)?.id
    }

    /**
     * Διαγράφει τον local user (logout cleanup)
     */
    suspend fun clearLocalUser(firebaseUid: String) {
        val user = userDao.findByFirebaseUid(firebaseUid)
        if (user != null) {
            userDao.deleteUser(user)
        }
    }

    /**
     * Επιστρέφει τον τρέχοντα logged-in user (αν υπάρχει)
     */
    suspend fun getCurrentLocalUser(): User? {
        val firebaseUser = authService.getCurrentUser() ?: return null
        return userDao.findByFirebaseUid(firebaseUser.uid)
    }
}