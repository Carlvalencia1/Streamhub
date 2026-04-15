package com.valencia.streamhub.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valencia.streamhub.core.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE streamId = :streamId ORDER BY createdAt ASC")
    fun observeByStream(streamId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE streamId = :streamId ORDER BY createdAt ASC")
    suspend fun getByStream(streamId: String): List<MessageEntity>

    @Query("DELETE FROM messages WHERE streamId = :streamId")
    suspend fun deleteByStream(streamId: String)
}
