package com.valencia.streamhub.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valencia.streamhub.core.database.entities.DeviceTokenEntity

@Dao
interface DeviceTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(token: DeviceTokenEntity)

    @Update
    suspend fun update(token: DeviceTokenEntity)

    @Query("SELECT * FROM device_tokens WHERE userId = :userId AND isValid = 1")
    suspend fun getValidByUser(userId: String): List<DeviceTokenEntity>

    @Query("UPDATE device_tokens SET isValid = 0 WHERE token = :token")
    suspend fun invalidate(token: String)

    @Query("DELETE FROM device_tokens WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}
