package com.example.elder_connect.data.dao

import androidx.room.*
import com.example.elder_connect.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /**
     * Βρίσκει user από το Firebase UID
     * (Αυτό χρησιμοποιείται για sync με Firebase Auth)
     */
    @Query("SELECT * FROM users WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun findByFirebaseUid(firebaseUid: String): User?

    /**
     * Βρίσκει user από το Firebase UID (ως Flow για reactive updates)
     */
    @Query("SELECT * FROM users WHERE firebaseUid = :firebaseUid LIMIT 1")
    fun observeByFirebaseUid(firebaseUid: String): Flow<User?>

    /**
     * Βρίσκει user από Room ID
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?

    /**
     * Βρίσκει user από email
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    /**
     * Insert νέου user - επιστρέφει το generated ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    /**
     * Update υπάρχοντος user
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Διαγραφή user
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * Όλοι οι users (για admin purposes)
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Καθαρισμός όλων των users (για testing/reset)
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}