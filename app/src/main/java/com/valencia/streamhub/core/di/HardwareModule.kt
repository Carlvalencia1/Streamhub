package com.valencia.streamhub.core.di

import com.valencia.streamhub.core.hardware.data.AndroidCamaraManager
import com.valencia.streamhub.core.hardware.data.AndroidMicrofonoManager
import com.valencia.streamhub.core.hardware.data.AndroidNotificacionManager
import com.valencia.streamhub.core.hardware.domain.CamaraManager
import com.valencia.streamhub.core.hardware.domain.MicrofonoManager
import com.valencia.streamhub.core.hardware.domain.NotificacionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindNotificacionManager(impl: AndroidNotificacionManager): NotificacionManager

    @Binds
    @Singleton
    abstract fun bindCamaraManager(impl: AndroidCamaraManager): CamaraManager

    @Binds
    @Singleton
    abstract fun bindMicrofonoManager(impl: AndroidMicrofonoManager): MicrofonoManager
}

