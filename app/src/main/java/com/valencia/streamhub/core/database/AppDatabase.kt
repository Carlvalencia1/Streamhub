package com.valencia.streamhub.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valencia.streamhub.core.database.dao.DeviceTokenDao
import com.valencia.streamhub.core.database.dao.MessageDao
import com.valencia.streamhub.core.database.dao.StreamDao
import com.valencia.streamhub.core.database.dao.UserDao
import com.valencia.streamhub.core.database.entities.DeviceTokenEntity
import com.valencia.streamhub.core.database.entities.MessageEntity
import com.valencia.streamhub.core.database.entities.StreamEntity
import com.valencia.streamhub.core.database.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        StreamEntity::class,
        MessageEntity::class,
        DeviceTokenEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun streamDao(): StreamDao
    abstract fun messageDao(): MessageDao
    abstract fun deviceTokenDao(): DeviceTokenDao
}
