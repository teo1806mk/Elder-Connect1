package com.example.elder_connect.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity για τον χρήστη (τοπική cache).
 *
 * Συνδέεται με Firebase Auth μέσω του firebaseUid.
 * Κάθε Firebase user έχει ένα αντίστοιχο Room user record.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["firebaseUid"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Firebase Auth UID - το unique ID από το Firebase Authentication.
     * Χρησιμοποιείται για να συνδέσουμε τον local user με τον Firebase user.
     */
    val firebaseUid: String,

    /**
     * Ονοματεπώνυμο του χρήστη
     */
    val fullName: String,

    /**
     * Email (αν είναι Caregiver/Admin), ή null αν είναι Elder με pairing code
     */
    val email: String? = null,

    /**
     * Ημερομηνία δημιουργίας (timestamp σε milliseconds)
     */
    val createdAt: Long = System.currentTimeMillis()
)