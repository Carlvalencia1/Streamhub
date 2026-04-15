package com.valencia.streamhub.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valencia.streamhub.core.database.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE streamId = :streamId ORDER BY createdAt ASC")
    fun observeMessagesByStream(streamId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE streamId = :streamId ORDER BY createdAt ASC")
    suspend fun getMessagesSnapshotByStream(streamId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE streamId = :streamId")
    suspend fun clearByStream(streamId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

