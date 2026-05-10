package com.example.elder_connect.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Service για αλληλεπίδραση με Firestore.
 *
 * Καλύπτει τις απαιτήσεις της εργασίας:
 * - Insert / Update / Delete (CRUD)
 * - 3 ερωτήματα διαφορετικής δομής:
 *   1) WHERE equality + ORDER BY (whereEqualTo + orderBy)
 *   2) Range query (whereGreaterThan)
 *   3) Multiple WHERE clauses + LIMIT (compound query)
 */
class FirestoreService {

    private val firestore = FirebaseFirestore.getInstance()

    private val announcementsRef = firestore.collection("announcements")
    private val interestsRef = firestore.collection("interests")

    // =========================================================
    //                     ANNOUNCEMENTS - QUERIES
    // =========================================================

    /**
     * ΕΡΩΤΗΜΑ 1 (Firestore): Απλό SELECT με ORDER BY
     * Επιστρέφει όλες τις ανακοινώσεις, ταξινομημένες από τη νεότερη.
     *
     * Ισοδύναμο SQL:
     *   SELECT * FROM announcements ORDER BY createdAt DESC
     *
     * Real-time: επιστρέφει Flow που ενημερώνεται αυτόματα.
     */
    fun getAllAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = announcementsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val announcements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(announcements)
            }

        awaitClose { listener.remove() }
    }

    /**
     * ΕΡΩΤΗΜΑ 2 (Firestore): WHERE equality + ORDER BY
     * Φιλτράρισμα ανά κατηγορία.
     *
     * Ισοδύναμο SQL:
     *   SELECT * FROM announcements
     *   WHERE category = ?
     *   ORDER BY createdAt DESC
     */
    fun getAnnouncementsByCategory(category: String): Flow<List<Announcement>> = callbackFlow {
        val listener = announcementsRef
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val announcements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(announcements)
            }

        awaitClose { listener.remove() }
    }

    /**
     * ΕΡΩΤΗΜΑ 3 (Firestore): Range query με WHERE GREATER THAN
     * Επιστρέφει επερχόμενες εκδηλώσεις (eventDate >= τώρα).
     *
     * Ισοδύναμο SQL:
     *   SELECT * FROM announcements
     *   WHERE eventDate >= NOW()
     *   ORDER BY eventDate ASC
     */
    fun getUpcomingEvents(): Flow<List<Announcement>> = callbackFlow {
        val now = Timestamp.now()
        val listener = announcementsRef
            .whereGreaterThanOrEqualTo("eventDate", now)
            .orderBy("eventDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val announcements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(announcements)
            }

        awaitClose { listener.remove() }
    }

    /**
     * ΕΡΩΤΗΜΑ 4 (Firestore - bonus): Compound query (WHERE + WHERE + LIMIT)
     * Επιστρέφει ανακοινώσεις σε συγκεκριμένη πόλη και κατηγορία.
     *
     * Ισοδύναμο SQL:
     *   SELECT * FROM announcements
     *   WHERE city = ? AND category = ?
     *   ORDER BY createdAt DESC
     *   LIMIT N
     */
    fun searchAnnouncements(city: String, category: String, limit: Int = 10): Flow<List<Announcement>> = callbackFlow {
        val listener = announcementsRef
            .whereEqualTo("city", city)
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val announcements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(announcements)
            }

        awaitClose { listener.remove() }
    }

    // =========================================================
    //                     ANNOUNCEMENTS - CRUD
    // =========================================================

    /** Δημιουργία νέας ανακοίνωσης */
    suspend fun addAnnouncement(announcement: Announcement): Result<String> {
        return try {
            val docRef = announcementsRef.add(announcement).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Ενημέρωση υπάρχουσας */
    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            announcementsRef.document(announcement.id).set(announcement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Διαγραφή */
    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        return try {
            announcementsRef.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================
    //                     INTERESTS (συμμετοχές)
    // =========================================================

    /**
     * Δήλωση ενδιαφέροντος για ανακοίνωση.
     * "Ενδιαφέρομαι" / "Δεν ενδιαφέρομαι"
     */
    suspend fun setInterest(
        announcementId: String,
        userId: Long,
        userName: String,
        isInterested: Boolean
    ): Result<Unit> {
        return try {
            // Βρες αν υπάρχει ήδη interest από αυτόν τον χρήστη
            val existing = interestsRef
                .whereEqualTo("announcementId", announcementId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val interest = Interest(
                announcementId = announcementId,
                userId = userId,
                userName = userName,
                isInterested = isInterested
            )

            if (existing.isEmpty) {
                interestsRef.add(interest).await()
            } else {
                interestsRef.document(existing.documents.first().id).set(interest).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Παίρνει το ενδιαφέρον του χρήστη για συγκεκριμένη ανακοίνωση */
    fun getUserInterest(announcementId: String, userId: Long): Flow<Interest?> = callbackFlow {
        val listener = interestsRef
            .whereEqualTo("announcementId", announcementId)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val interest = snapshot?.documents?.firstOrNull()
                    ?.toObject(Interest::class.java)
                    ?.copy(id = snapshot.documents.first().id)

                trySend(interest)
            }

        awaitClose { listener.remove() }
    }
}