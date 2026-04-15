package com.valencia.streamhub.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valencia.streamhub.core.database.dao.ChatMessageDao
import com.valencia.streamhub.core.database.dao.StreamDao
import com.valencia.streamhub.core.database.entities.ChatMessageEntity
import com.valencia.streamhub.core.database.entities.StreamEntity

@Database(
    entities = [
        StreamEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StreamhubDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao
    abstract fun chatMessageDao(): ChatMessageDao
}

