package com.example.elder_connect.data.dao

import androidx.room.*
import com.example.elder_connect.data.entities.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    fun getContactsByUser(userId: Long): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isFavorite = 1")
    fun getFavoriteVideoCallContacts(userId: Long): Flow<List<Contact>>

    @Query("SELECT relationship, COUNT(*) as count FROM contacts WHERE userId = :userId GROUP BY relationship")
    fun getContactCountByRelationship(userId: Long): Flow<List<RelationshipCount>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND name LIKE '%' || :query || '%'")
    fun searchContacts(userId: Long, query: String): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Long): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)
}

data class RelationshipCount(
    val relationship: String,
    val count: Int
)
