package com.example.elder_connect.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility για όλα τα είδη κλήσεων μέσω Intents.
 *
 * Χρησιμοποιούμε ACTION_DIAL αντί για ACTION_CALL ώστε:
 *  - Να μη χρειάζεται permission CALL_PHONE
 *  - Ο χρήστης (ηλικιωμένος) να βλέπει τον αριθμό πριν καλέσει
 *  - Να μπορεί να ακυρώσει αν τον πάτησε κατά λάθος
 */
object CallHelper {

    /**
     * Κανονική φωνητική κλήση μέσω της εφαρμογής τηλεφωνίας.
     * Ανοίγει το dialer με τον αριθμό συμπληρωμένο.
     */
    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${cleanPhoneNumber(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Δεν βρέθηκε εφαρμογή τηλεφωνίας",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Βιντεοκλήση μέσω WhatsApp.
     * Ανοίγει απευθείας το video call screen του WhatsApp.
     */
    fun makeWhatsAppVideoCall(context: Context, phoneNumber: String) {
        try {
            // Καθαρίζουμε τον αριθμό (μόνο ψηφία + κωδικός χώρας)
            val cleaned = phoneNumberForWhatsApp(phoneNumber)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleaned?video=1")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Αν δεν υπάρχει WhatsApp, ρίξε σε browser ή δείξε μήνυμα
            Toast.makeText(
                context,
                "Δεν βρέθηκε το WhatsApp. Παρακαλώ εγκατέστησέ το.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Φωνητική κλήση μέσω WhatsApp.
     */
    fun makeWhatsAppVoiceCall(context: Context, phoneNumber: String) {
        try {
            val cleaned = phoneNumberForWhatsApp(phoneNumber)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleaned")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Δεν βρέθηκε το WhatsApp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Άνοιγμα WhatsApp chat με συγκεκριμένη επαφή.
     */
    fun openWhatsAppChat(context: Context, phoneNumber: String) {
        try {
            val cleaned = phoneNumberForWhatsApp(phoneNumber)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleaned")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Δεν βρέθηκε εφαρμογή", Toast.LENGTH_SHORT).show()
        }
    }

    /** Έλεγχος αν είναι εγκατεστημένο το WhatsApp */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Καθαρισμός αριθμού - αφαιρεί κενά, παύλες, κλπ
     * αλλά κρατάει το + για διεθνή κωδικό.
     */
    private fun cleanPhoneNumber(phone: String): String {
        return phone.filter { it.isDigit() || it == '+' }
    }

    /**
     * Για το WhatsApp χρειάζεται format χωρίς + ή κενά,
     * π.χ. "+30 6971234567" -> "306971234567"
     */
    private fun phoneNumberForWhatsApp(phone: String): String {
        return phone.filter { it.isDigit() }
    }
}