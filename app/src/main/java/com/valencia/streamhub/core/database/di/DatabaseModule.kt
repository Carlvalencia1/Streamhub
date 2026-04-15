package com.valencia.streamhub.core.database.di

import android.content.Context
import androidx.room.Room
import com.valencia.streamhub.core.database.AppDatabase
import com.valencia.streamhub.core.database.dao.DeviceTokenDao
import com.valencia.streamhub.core.database.dao.MessageDao
import com.valencia.streamhub.core.database.dao.StreamDao
import com.valencia.streamhub.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "streamhub.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideStreamDao(db: AppDatabase): StreamDao = db.streamDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideDeviceTokenDao(db: AppDatabase): DeviceTokenDao = db.deviceTokenDao()
}
