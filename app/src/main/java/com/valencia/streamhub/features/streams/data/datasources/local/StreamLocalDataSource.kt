package com.valencia.streamhub.features.streams.data.datasources.local

import com.valencia.streamhub.core.database.dao.StreamDao
import com.valencia.streamhub.core.database.entities.StreamEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamLocalDataSource @Inject constructor(
    private val streamDao: StreamDao
) {
    fun observeStreams(): Flow<List<StreamEntity>> = streamDao.observeAll()

    suspend fun getStreamsSnapshot(): List<StreamEntity> = streamDao.getStreamsSnapshot()

    suspend fun replaceAll(streams: List<StreamEntity>) {
        streamDao.deleteAll()
        streamDao.upsertAll(streams)
    }

    suspend fun upsert(stream: StreamEntity) = streamDao.upsert(stream)
}

