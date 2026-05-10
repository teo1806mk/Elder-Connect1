package com.example.elder_connect.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elder_connect.data.dao.ContactDao
import com.example.elder_connect.data.dao.MoodEntryDao
import com.example.elder_connect.data.dao.UserDao
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.entities.MoodEntry
import com.example.elder_connect.data.entities.User

/**
 * Room Database για το Elder-Connect.
 *
 * Version 2: Προσθήκη firebaseUid στον User, αφαίρεση seed data
 * (Οι χρήστες τώρα έρχονται από Firebase Authentication)
 */
@Database(
    entities = [User::class, Contact::class, MoodEntry::class],
    version = 2,  // ← Αυξημένο από 1 σε 2
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ElderConnectDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun moodEntryDao(): MoodEntryDao

    companion object {
        @Volatile
        private var INSTANCE: ElderConnectDatabase? = null

        fun getDatabase(context: Context): ElderConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ElderConnectDatabase::class.java,
                    "elder_connect_database"
                )
                    // Για development: σβήνει και ξαναφτιάχνει τη βάση αν αλλάξει το schema
                    // ⚠️ Σε production θα έπρεπε να γράψουμε migration scripts
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Καθαρισμός instance (για testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}