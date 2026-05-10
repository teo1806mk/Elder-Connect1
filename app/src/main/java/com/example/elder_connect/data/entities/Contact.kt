package com.example.elder_connect.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Πίνακας 2: CONTACTS
 * Αποθηκεύει τις επαφές του χρήστη (Ελένη, Κώστας, Άννα, κτλ).
 *
 * Ξένο κλειδί (FK): userId -> users.id
 * onDelete = CASCADE: αν διαγραφεί ο χρήστης, διαγράφονται και οι επαφές του.
 */
@Entity(
    tableName = "contacts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long,                    // Ξένο κλειδί προς users.id
    val name: String,                    // π.χ. "Ελένη"
    val relationship: String,            // π.χ. "Κόρη", "Γιός", "Εγγονή"
    val phoneNumber: String,
    val avatarResource: String? = null,  // π.χ. "avatar_eleni"
    val isFavorite: Boolean = false,     // αν εμφανίζεται στην αρχική
    val supportsVideoCall: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)