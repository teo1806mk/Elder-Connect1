package com.example.elder_connect.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Η οντότητα MoodEntry αποθηκεύει την καθημερινή διάθεση του χρήστη.
 * "Πώς νιώθεις σήμερα;"
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: MoodType,         // η διάθεση (enum)
    val note: String = ""       // προαιρετικό σχόλιο
)

/**
 * Οι 5 βασικές επιλογές διάθεσης (όπως στο Figma)
 */
enum class MoodType(val label: String, val emoji: String) {
    TELEIA("Τέλεια", "😊"),
    KALA("Καλά", "🙂"),
    METRIA("Μέτρια", "😐"),
    OXI_KALA("Όχι καλά", "🙁"),
    ASXIMA("Άσχημα", "😞")
}
