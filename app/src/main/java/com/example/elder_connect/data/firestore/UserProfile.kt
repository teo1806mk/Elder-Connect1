package com.example.elder_connect.data.firestore

import com.google.firebase.Timestamp

/**
 * Προφίλ χρήστη που αποθηκεύεται στη Firestore.
 *
 * Το Firebase Auth διαχειρίζεται email/password,
 * αλλά τα υπόλοιπα στοιχεία (όνομα, role, photo, κλπ) μένουν εδώ.
 *
 * Document path: /users/{authUid}
 */
data class UserProfile(
    val uid: String = "",                  // Firebase Auth UID
    val email: String = "",
    val fullName: String = "",
    val age: Int = 0,
    val city: String = "",
    val photoUrl: String = "",
    val role: String = UserRole.USER.key,  // Admin / User / Elder

    // Για ELDER: ο uid του caregiver που τον δημιούργησε
    val caregiverId: String? = null,

    // Για ELDER: μοναδικός 6-ψήφιος κωδικός pairing
    val pairingCode: String? = null,

    // Πότε δημιουργήθηκε ο λογαριασμός
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * 3 ρόλοι λογαριασμών:
 *
 * - ADMIN: Πλήρης πρόσβαση + δημιουργία ανακοινώσεων δήμου
 * - USER (Caregiver): Νέος υπεύθυνος ηλικιωμένου, μπορεί να δει όλα,
 *   να δημιουργήσει elder profile, να λάβει ειδοποιήσεις
 * - ELDER: Ηλικιωμένος, βασικές λειτουργίες μόνο
 */
enum class UserRole(val key: String, val label: String, val description: String) {
    ADMIN(
        key = "admin",
        label = "Διαχειριστής",
        description = "Πλήρης πρόσβαση και δημιουργία ανακοινώσεων"
    ),
    USER(
        key = "user",
        label = "Νέος Υπεύθυνος",
        description = "Διαχείριση ηλικιωμένου συγγενή"
    ),
    ELDER(
        key = "elder",
        label = "Υπερήλικας",
        description = "Βασικές λειτουργίες (κλήσεις, διάθεση, εκδρομές)"
    );

    companion object {
        fun fromKey(key: String): UserRole =
            values().firstOrNull { it.key == key } ?: USER
    }
}