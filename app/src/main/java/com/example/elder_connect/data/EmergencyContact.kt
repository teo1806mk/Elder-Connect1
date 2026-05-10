package com.example.elder_connect.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Σταθερά τηλέφωνα έκτακτης ανάγκης για την Ελλάδα.
 *
 * Εμφανίζονται στην οθόνη EmergencyPhones και στο SOS button της αρχικής.
 */
data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * Λίστα με τα 3 βασικά τηλέφωνα έκτακτης ανάγκης.
 * Παγκόσμιο 112 + ειδικά νούμερα.
 */
val emergencyContactsList = listOf(
    EmergencyContact(
        name = "Πανευρωπαϊκός 112",
        phoneNumber = "112",
        description = "Γενικός αριθμός έκτακτης ανάγκης",
        icon = Icons.Filled.LocalHospital,
        color = Color(0xFFD32F2F)  // κόκκινο
    ),
    EmergencyContact(
        name = "Αστυνομία",
        phoneNumber = "100",
        description = "Άμεση Δράση",
        icon = Icons.Filled.LocalPolice,
        color = Color(0xFF1565C0)  // μπλε
    ),
    EmergencyContact(
        name = "ΕΚΑΒ",
        phoneNumber = "166",
        description = "Ασθενοφόρο",
        icon = Icons.Filled.LocalHospital,
        color = Color(0xFFE65100)  // πορτοκαλί
    ),
    EmergencyContact(
        name = "Πυροσβεστική",
        phoneNumber = "199",
        description = "Σε περίπτωση φωτιάς",
        icon = Icons.Filled.LocalFireDepartment,
        color = Color(0xFFD84315)  // κόκκινο-πορτοκαλί
    )
)