package com.restrusher.partypuzz.di

import com.restrusher.partypuzz.data.local.dao.PlayerDao
import com.restrusher.partypuzz.data.repositories.PlayerRepositoryImpl
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePlayerRepository(playerDao: PlayerDao): PlayerRepository {
        return PlayerRepositoryImpl(playerDao)
    }
}