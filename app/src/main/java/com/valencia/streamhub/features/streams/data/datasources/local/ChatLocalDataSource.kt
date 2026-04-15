package com.valencia.streamhub.features.streams.data.datasources.local

import com.valencia.streamhub.core.database.dao.ChatMessageDao
import com.valencia.streamhub.core.database.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) {
    fun observeMessagesByStream(streamId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.observeMessagesByStream(streamId)
    }

    suspend fun getMessagesSnapshotByStream(streamId: String): List<ChatMessageEntity> {
        return chatMessageDao.getMessagesSnapshotByStream(streamId)
    }

    suspend fun upsert(message: ChatMessageEntity) {
        chatMessageDao.upsert(message)
    }

    suspend fun upsertAll(messages: List<ChatMessageEntity>) {
        chatMessageDao.upsertAll(messages)
    }

    suspend fun clearByStream(streamId: String) {
        chatMessageDao.clearByStream(streamId)
    }
}

