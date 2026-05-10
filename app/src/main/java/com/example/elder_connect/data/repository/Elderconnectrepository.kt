package com.example.elder_connect.data.repository

import com.example.elder_connect.data.dao.ContactDao
import com.example.elder_connect.data.dao.MoodEntryDao
import com.example.elder_connect.data.dao.UserDao
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.entities.MoodEntry
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.entities.Mood
import com.example.elder_connect.data.firestore.FirestoreService
import com.example.elder_connect.data.firestore.Announcement
import com.example.elder_connect.data.sync.UserSyncService
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository pattern - ενοποιεί όλες τις πηγές δεδομένων:
 * - Room (local database)
 * - Firestore (remote database)
 * - UserSyncService (Firebase Auth ↔ Room sync)
 */
class ElderConnectRepository(
    private val userDao: UserDao,
    private val contactDao: ContactDao,
    private val moodEntryDao: MoodEntryDao,
    private val firestoreService: FirestoreService,
    val userSyncService: UserSyncService  // ← ΝΕΟΣ
) {

    // ==================== USER OPERATIONS ====================

    /**
     * Επιστρέφει τον τρέχοντα local user (από Room)
     */
    suspend fun getCurrentLocalUser(): User? {
        return userSyncService.getCurrentLocalUser()
    }

    suspend fun getUserById(id: Long): User? = userDao.findById(id)

    suspend fun getUserByFirebaseUid(firebaseUid: String): User? =
        userDao.findByFirebaseUid(firebaseUid)

    fun observeUserByFirebaseUid(firebaseUid: String): Flow<User?> =
        userDao.observeByFirebaseUid(firebaseUid)

    // ==================== CONTACT OPERATIONS ====================

    fun getContactsByUser(userId: Long): Flow<List<Contact>> =
        contactDao.getContactsByUser(userId)

    fun getFavoriteVideoCallContacts(userId: Long): Flow<List<Contact>> =
        contactDao.getFavoriteVideoCallContacts(userId)

    suspend fun getContactCountByRelationship(userId: Long): List<Pair<String, Int>> =
        contactDao.getContactCountByRelationship(userId)

    fun searchContacts(userId: Long, query: String): Flow<List<Contact>> =
        contactDao.searchContacts(userId, query)

    suspend fun getContactById(contactId: Long): Contact? =
        contactDao.getContactById(contactId)

    suspend fun insertContact(contact: Contact): Long =
        contactDao.insertContact(contact)

    suspend fun updateContact(contact: Contact) =
        contactDao.updateContact(contact)

    suspend fun deleteContact(contact: Contact) =
        contactDao.deleteContact(contact)

    // ==================== MOOD OPERATIONS ====================

    fun getMoodsInRange(userId: Long, startDate: Date, endDate: Date): Flow<List<MoodEntry>> =
        moodEntryDao.getMoodsInRange(userId, startDate, endDate)

    fun getRecentNegativeMoods(userId: Long, limit: Int): Flow<List<MoodEntry>> =
        moodEntryDao.getRecentNegativeMoods(userId, listOf(Mood.SAD, Mood.ANGRY), limit)

    suspend fun getLatestMood(userId: Long): MoodEntry? =
        moodEntryDao.getLatestMood(userId)

    suspend fun insertMoodEntry(moodEntry: MoodEntry): Long =
        moodEntryDao.insertMoodEntry(moodEntry)

    suspend fun getAllMoodsByUser(userId: Long): List<MoodEntry> =
        moodEntryDao.getAllMoodsByUser(userId)

    // ==================== FIRESTORE OPERATIONS ====================

    // Announcements
    suspend fun getAllAnnouncements(): List<Announcement> =
        firestoreService.getAllAnnouncements()

    suspend fun getAnnouncementsByCategory(category: String): List<Announcement> =
        firestoreService.getAnnouncementsByCategory(category)

    suspend fun getRecentAnnouncements(limit: Int): List<Announcement> =
        firestoreService.getRecentAnnouncements(limit)

    suspend fun addAnnouncement(announcement: Announcement): String =
        firestoreService.addAnnouncement(announcement)

    suspend fun updateAnnouncement(id: String, announcement: Announcement) =
        firestoreService.updateAnnouncement(id, announcement)

    suspend fun deleteAnnouncement(id: String) =
        firestoreService.deleteAnnouncement(id)

    // Interests
    suspend fun declareInterest(announcementId: String, userId: String, userName: String) =
        firestoreService.declareInterest(announcementId, userId, userName)

    suspend fun getInterestsForAnnouncement(announcementId: String): List<Map<String, Any>> =
        firestoreService.getInterestsForAnnouncement(announcementId)
}