package com.valencia.streamhub.core.di

import android.content.Context
import androidx.room.Room
import com.valencia.streamhub.core.database.StreamhubDatabase
import com.valencia.streamhub.core.database.dao.ChatMessageDao
import com.valencia.streamhub.core.database.dao.StreamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDatabaseModule {

    @Provides
    @Singleton
    fun provideStreamhubDatabase(
        @ApplicationContext context: Context
    ): StreamhubDatabase {
        return Room.databaseBuilder(
            context,
            StreamhubDatabase::class.java,
            "streamhub.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStreamDao(database: StreamhubDatabase): StreamDao = database.streamDao()

    @Provides
    @Singleton
    fun provideChatMessageDao(database: StreamhubDatabase): ChatMessageDao = database.chatMessageDao()
}


