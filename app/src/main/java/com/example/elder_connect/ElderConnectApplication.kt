package com.example.elder_connect

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.elder_connect.data.database.ElderConnectDatabase
import com.example.elder_connect.data.firestore.AuthService
import com.example.elder_connect.data.firestore.FirestoreService
import com.example.elder_connect.data.repository.ElderConnectRepository
import com.example.elder_connect.data.sync.UserSyncService

/**
 * Application class - singleton για όλη την εφαρμογή.
 *
 * Δημιουργεί και κρατάει:
 * - Database
 * - Repository
 * - UserSyncService (για Firebase ↔ Room sync)
 * - Notification channels
 */
class ElderConnectApplication : Application() {

    // Database (lazy initialization)
    val database: ElderConnectDatabase by lazy {
        ElderConnectDatabase.getDatabase(this)
    }

    // Firestore Service
    val firestoreService: FirestoreService by lazy {
        FirestoreService()
    }

    // Auth Service
    val authService: AuthService by lazy {
        AuthService(firestoreService)
    }

    // User Sync Service (συνδέει Firebase με Room)
    val userSyncService: UserSyncService by lazy {
        UserSyncService(
            userDao = database.userDao(),
            authService = authService
        )
    }

    // Repository (ενοποιεί όλες τις πηγές δεδομένων)
    val repository: ElderConnectRepository by lazy {
        ElderConnectRepository(
            userDao = database.userDao(),
            contactDao = database.contactDao(),
            moodEntryDao = database.moodEntryDao(),
            firestoreService = firestoreService,
            userSyncService = userSyncService
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Δημιουργία notification channels (απαιτείται για Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Channel για mood notifications
            val moodChannel = NotificationChannel(
                "mood_channel",
                "Καταγραφή Διάθεσης",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ειδοποιήσεις όταν καταγράφεται η διάθεσή σας"
            }

            // Channel για announcements
            val newsChannel = NotificationChannel(
                "news_channel",
                "Ανακοινώσεις",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ειδοποιήσεις για νέες ανακοινώσεις και δραστηριότητες"
            }

            // Channel για emergency (υψηλή προτεραιότητα)
            val emergencyChannel = NotificationChannel(
                "emergency_channel",
                "Έκτακτη Ανάγκη",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ειδοποιήσεις έκτακτης ανάγκης"
            }

            notificationManager.createNotificationChannel(moodChannel)
            notificationManager.createNotificationChannel(newsChannel)
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }
}