package com.example.elder_connect.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.elder_connect.MainActivity
import com.example.elder_connect.R

/**
 * Utility για διαχείριση όλων των notifications του app.
 *
 * Καλύπτει την απαίτηση: "Χρήση ειδοποιήσεων (Notifications)
 * για κατάλληλη ενημέρωση του χρήστη."
 *
 * Δύο channels:
 *  - MOOD_CHANNEL: επιβεβαιώσεις διάθεσης
 *  - NEWS_CHANNEL: νέες ανακοινώσεις δήμου
 */
object NotificationHelper {

    const val MOOD_CHANNEL_ID = "mood_channel"
    const val NEWS_CHANNEL_ID = "news_channel"

    private const val MOOD_NOTIFICATION_ID = 1001
    private const val NEWS_NOTIFICATION_ID_BASE = 2000

    /**
     * Δημιουργία των channels (απαραίτητο σε Android 8+).
     * Καλείται από το ElderConnectApplication.onCreate().
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val moodChannel = NotificationChannel(
            MOOD_CHANNEL_ID,
            "Διαθέσεις",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Επιβεβαιώσεις όταν καταγράφεις τη διάθεσή σου"
        }
        manager.createNotificationChannel(moodChannel)

        val newsChannel = NotificationChannel(
            NEWS_CHANNEL_ID,
            "Νέα από Δήμο",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ειδοποιήσεις για νέες ανακοινώσεις και εκδρομές"
        }
        manager.createNotificationChannel(newsChannel)
    }

    /**
     * Έλεγχος αν έχουμε permission για notifications.
     * Σε Android 13+ απαιτείται runtime permission.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true  // Android < 13: δεν χρειάζεται permission
        }
    }

    /**
     * Notification όταν ο χρήστης καταγράφει mood.
     *
     * Η ειδοποίηση επιβεβαιώνει την αποθήκευση και αναφέρει
     * ότι οι συγγενείς ενημερώθηκαν (όπως περιγράφεται στο σενάριο).
     */
    fun showMoodSavedNotification(
        context: Context,
        moodLabel: String,
        moodEmoji: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, MOOD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$moodEmoji Καταγράφηκε η διάθεσή σου")
            .setContentText("Νιώθεις: $moodLabel. Οι συγγενείς σου ενημερώθηκαν.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Νιώθεις: $moodLabel\n\nΟι αγαπημένοι σου ενημερώθηκαν για την διάθεσή σου σήμερα.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(MOOD_NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked - ignore silently
        }
    }

    /**
     * Notification για νέα ανακοίνωση δήμου.
     * Κάθε ανακοίνωση παίρνει unique ID ώστε να μη χτυπιούνται μεταξύ τους.
     */
    fun showNewAnnouncementNotification(
        context: Context,
        announcementId: String,
        title: String,
        description: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            announcementId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NEWS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📢 $title")
            .setContentText(description.take(80) + if (description.length > 80) "..." else "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(
                    NEWS_NOTIFICATION_ID_BASE + announcementId.hashCode(),
                    builder.build()
                )
        } catch (e: SecurityException) {
            // Permission revoked - ignore
        }
    }

    /**
     * Notification για επιτυχημένη δήλωση συμμετοχής.
     */
    fun showInterestRegisteredNotification(
        context: Context,
        announcementTitle: String,
        isInterested: Boolean
    ) {
        if (!hasNotificationPermission(context)) return

        val title = if (isInterested) "✅ Δήλωσες συμμετοχή" else "❌ Δεν θα συμμετάσχεις"
        val text = if (isInterested)
            "Δήλωσες ότι θες να πάεις στο: $announcementTitle"
        else
            "Δήλωσες ότι δε θα συμμετάσχεις στο: $announcementTitle"

        val builder = NotificationCompat.Builder(context, NEWS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // ignore
        }
    }
}