package com.vmiforall.server.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@InstallIn(SingletonComponent::class)
@Module
object CoroutineScopeModule {

    @Provides
    @ServiceCoroutineScope
    fun provideServiceCoroutineScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(defaultDispatcher + SupervisorJob())
    }

}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ServiceCoroutineScope
