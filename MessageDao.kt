package com.msgsafe.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isDeleted = 1 ORDER BY timestamp DESC")
    fun getDeletedOnly(): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE chatName LIKE '%' || :query || '%' 
           OR message LIKE '%' || :query || '%'
           OR sender LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun search(query: String): Flow<List<MessageEntity>>

    // Finds the most recent, not-yet-deleted message from the same chat
    // within a time window — used to guess which message a "deleted" event refers to.
    @Query("""
        SELECT * FROM messages 
        WHERE chatName = :chatName 
          AND isDeleted = 0
          AND timestamp <= :aroundTime
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun findLikelyDeletedCandidate(chatName: String, aroundTime: Long): MessageEntity?

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
