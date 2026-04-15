package com.valencia.streamhub.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valencia.streamhub.core.database.entities.StreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {
    @Query("SELECT * FROM streams ORDER BY createdAt DESC")
    fun observeStreams(): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams ORDER BY createdAt DESC")
    suspend fun getStreamsSnapshot(): List<StreamEntity>

    @Query("SELECT * FROM streams WHERE id = :id LIMIT 1")
    fun observeStreamById(id: String): Flow<StreamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(streams: List<StreamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stream: StreamEntity)

    @Query("DELETE FROM streams")
    suspend fun clearAll()
}

