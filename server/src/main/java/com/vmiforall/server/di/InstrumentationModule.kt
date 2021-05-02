package com.vmiforall.server.di

import androidx.test.runner.MonitoringInstrumentation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object InstrumentationModule {

    @Provides
    @Singleton
    fun provideMonitoringInstrumentation(): MonitoringInstrumentation {
        return MonitoringInstrumentation()
    }

}
