package com.example.elder_connect.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Παλέτα χρωμάτων Elder-Connect
 *
 * Βασισμένη στο Figma design (ζεστές αποχρώσεις - πορτοκαλί/μπεζ),
 * με υψηλή αντίθεση για ηλικιωμένους χρήστες (WCAG AA compliant).
 */

// Light Theme - Primary
val ElderOrange = Color(0xFFE07A3E)        // Κύριο ζεστό πορτοκαλί
val ElderOrangeDark = Color(0xFFB85F2A)    // Πιο σκούρο για contrast
val ElderOrangeLight = Color(0xFFFFD9B8)   // Απαλό φόντο

// Background tones (από το Figma - μπεζ/κρέμ)
val ElderCream = Color(0xFFFAF6F0)         // Φόντο εφαρμογής
val ElderBeige = Color(0xFFF5E9D7)         // Cards, surfaces
val ElderSoftYellow = Color(0xFFFFE9B8)    // Highlights

// Text colors (υψηλή αντίθεση)
val ElderTextPrimary = Color(0xFF3E2723)   // Σχεδόν μαύρο, ζεστή απόχρωση
val ElderTextSecondary = Color(0xFF6D4C41) // Καφέ-γκρι

// Action colors
val ElderGreen = Color(0xFF2E7D32)         // Πράσινο για κλήσεις (όπως στο Figma)
val ElderRed = Color(0xFFC62828)           // Κόκκινο για SOS / emergency
val ElderBlue = Color(0xFF1565C0)          // Μπλε για κουμπιά "Αποθήκευσε"

// Mood colors (5 emojis)
val MoodGreatColor = Color(0xFFFFC107)     // Κίτρινο φωτεινό
val MoodGoodColor = Color(0xFFFFCA28)
val MoodOkColor = Color(0xFFFFB300)
val MoodNotGoodColor = Color(0xFFFF8F00)
val MoodBadColor = Color(0xFFE65100)

// Dark theme variants
val ElderOrangeDarkMode = Color(0xFFFFAB7B)
val ElderBackgroundDark = Color(0xFF1F1B16)
val ElderSurfaceDark = Color(0xFF2D2620)