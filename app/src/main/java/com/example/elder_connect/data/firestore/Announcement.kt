package com.example.elder_connect.data.firestore

import com.google.firebase.Timestamp

/**
 * Ανακοίνωση από τον δήμο (Firestore collection: "announcements").
 *
 * Αυτή η κλάση αντιπροσωπεύει ένα document της Firestore.
 * Το no-arg constructor (όλα defaults) είναι απαραίτητο για το
 * Firestore deserialization.
 */
data class Announcement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "general",  // "excursion", "health", "event", "general"
    val city: String = "",
    val eventDate: Timestamp? = null,
    val imageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Δηλώσεις συμμετοχής (Firestore collection: "interests").
 *
 * Όταν ο χρήστης πατάει "Ενδιαφέρομαι" σε μια ανακοίνωση,
 * δημιουργείται ένα interest document.
 */
data class Interest(
    val id: String = "",
    val announcementId: String = "",
    val userId: Long = 0,
    val userName: String = "",
    val isInterested: Boolean = true,  // true = "Ενδιαφέρομαι", false = "Δεν ενδιαφέρομαι"
    val timestamp: Timestamp = Timestamp.now()
)

/** Categories για φιλτράρισμα */
enum class AnnouncementCategory(val key: String, val label: String, val emoji: String) {
    ALL("all", "Όλα", "📋"),
    EXCURSION("excursion", "Εκδρομές", "🚌"),
    HEALTH("health", "Υγεία", "❤️"),
    EVENT("event", "Εκδηλώσεις", "🎭"),
    GENERAL("general", "Γενικά", "📢")
}