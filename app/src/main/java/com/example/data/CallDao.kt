package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    // --- Call Logs ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLog(id: Int)

    @Query("DELETE FROM call_logs")
    suspend fun clearCallLogs()

    // --- Contacts ---
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactsCount(): Int
}
