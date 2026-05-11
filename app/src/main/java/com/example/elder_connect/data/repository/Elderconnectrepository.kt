package com.example.elder_connect.data.repository

import com.example.elder_connect.data.dao.ContactDao
import com.example.elder_connect.data.dao.MoodEntryDao
import com.example.elder_connect.data.dao.UserDao
import com.example.elder_connect.data.dao.RelationshipCount
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.entities.MoodEntry
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.entities.MoodType
import com.example.elder_connect.data.firestore.FirestoreService
import com.example.elder_connect.data.firestore.Announcement
import com.example.elder_connect.data.firestore.Interest
import com.example.elder_connect.data.sync.UserSyncService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    val userSyncService: UserSyncService
) {

    // ==================== USER OPERATIONS ====================

    /**
     * Reactive Flow για τον τρέχοντα συνδεδεμένο χρήστη.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<User?> = userSyncService.authService.authStateFlow()
        .flatMapLatest { firebaseUser ->
            if (firebaseUser != null) {
                userDao.observeByFirebaseUid(firebaseUser.uid)
            } else {
                flowOf(null)
            }
        }

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

    fun getContactCountByRelationship(userId: Long): Flow<List<RelationshipCount>> =
        contactDao.getContactCountByRelationship(userId)

    fun searchContacts(userId: Long, query: String): Flow<List<Contact>> =
        contactDao.searchContacts(userId, query)

    suspend fun getContactById(contactId: Long): Contact? =
        contactDao.getContactById(contactId)

    suspend fun insertContact(contact: Contact): Long =
        contactDao.insert(contact)

    suspend fun updateContact(contact: Contact) =
        contactDao.update(contact)

    suspend fun deleteContact(contact: Contact) =
        contactDao.delete(contact)

    // ==================== MOOD OPERATIONS ====================

    fun getMoodsInRange(userId: Long, startDate: Date, endDate: Date): Flow<List<MoodEntry>> =
        moodEntryDao.getMoodsInRange(userId, startDate.time, endDate.time)

    fun getRecentNegativeMoods(userId: Long, limit: Int): Flow<List<MoodEntry>> =
        moodEntryDao.getRecentNegativeMoods(userId, listOf(MoodType.OXI_KALA, MoodType.ASXIMA), limit)

    fun getLatestMood(userId: Long): Flow<MoodEntry?> =
        moodEntryDao.getLatestMood(userId)

    suspend fun insertMoodEntry(moodEntry: MoodEntry): Long =
        moodEntryDao.insert(moodEntry)

    fun getAllMoodsByUser(userId: Long): Flow<List<MoodEntry>> =
        moodEntryDao.getAllMoodsByUser(userId)

    // ==================== FIRESTORE OPERATIONS ====================

    // Announcements
    fun getAllAnnouncements(): Flow<List<Announcement>> =
        firestoreService.getAllAnnouncements()

    fun getAnnouncementsByCategory(category: String): Flow<List<Announcement>> =
        firestoreService.getAnnouncementsByCategory(category)

    fun getUpcomingEvents(): Flow<List<Announcement>> =
        firestoreService.getUpcomingEvents()

    suspend fun addAnnouncement(announcement: Announcement): Result<String> =
        firestoreService.addAnnouncement(announcement)

    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> =
        firestoreService.updateAnnouncement(announcement)

    suspend fun deleteAnnouncement(id: String): Result<Unit> =
        firestoreService.deleteAnnouncement(id)

    // Interests
    suspend fun setInterest(announcementId: String, userId: Long, userName: String, isInterested: Boolean) =
        firestoreService.setInterest(announcementId, userId, userName, isInterested)

    fun getUserInterest(announcementId: String, userId: Long): Flow<Interest?> =
        firestoreService.getUserInterest(announcementId, userId)
}