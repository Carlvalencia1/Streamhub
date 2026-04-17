package com.valencia.streamhub.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valencia.streamhub.core.database.entities.StreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stream: StreamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(streams: List<StreamEntity>)

    @Update
    suspend fun update(stream: StreamEntity)

    @Query("SELECT * FROM streams WHERE id = :id")
    suspend fun getById(id: String): StreamEntity?

    @Query("SELECT * FROM streams ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams ORDER BY createdAt DESC")
    suspend fun getStreamsSnapshot(): List<StreamEntity>

    @Query("SELECT * FROM streams WHERE isLive = 1 ORDER BY viewersCount DESC")
    fun observeLive(): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<StreamEntity>>

    @Query("DELETE FROM streams WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM streams")
    suspend fun deleteAll()
}
