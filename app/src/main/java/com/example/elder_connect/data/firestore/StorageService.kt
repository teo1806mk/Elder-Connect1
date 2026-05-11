package com.example.elder_connect.data.firestore

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Service για Firebase Storage.
 *
 * Διαχειρίζεται upload/delete εικόνων:
 *  - Εικόνες προφίλ χρήστη : profile_images/{uid}.jpg
 *  - Εικόνες επαφών        : contact_images/{userId}/{contactId}.jpg
 */
class StorageService {

    private val storage = FirebaseStorage.getInstance()

    // ─── Profile Images ──────────────────────────────────────────────────────

    /**
     * Ανεβάζει εικόνα προφίλ και επιστρέφει το download URL.
     */
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Contact Images ──────────────────────────────────────────────────────

    /**
     * Ανεβάζει εικόνα επαφής και επιστρέφει το download URL.
     * Χρησιμοποιεί timestamp για μοναδικό filename ώστε να αποφύγουμε caching issues.
     */
    suspend fun uploadContactImage(userId: Long, contactId: Long, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference
                .child("contact_images/$userId/${contactId}_${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Delete ──────────────────────────────────────────────────────────────

    /**
     * Διαγράφει εικόνα από το Storage (για cleanup κατά αλλαγή ή διαγραφή).
     * Αν το URL είναι κενό ή άκυρο, επιστρέφει success χωρίς error.
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        if (imageUrl.isBlank()) return Result.success(Unit)
        return try {
            storage.getReferenceFromUrl(imageUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Αν δεν βρεθεί το αρχείο, δεν είναι κρίσιμο error
            Result.success(Unit)
        }
    }
}
