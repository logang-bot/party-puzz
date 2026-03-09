package com.restrusher.partypuzz.di

import com.restrusher.partypuzz.data.proxies.PartyProxy
import com.restrusher.partypuzz.data.proxies.PlayerProxy
import com.restrusher.partypuzz.data.repositories.PartyRepositoryImpl
import com.restrusher.partypuzz.data.repositories.PlayerRepositoryImpl
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
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
    fun providePlayerRepository(@DatabaseProxy playerProxy: PlayerProxy): PlayerRepository {
        return PlayerRepositoryImpl(playerProxy)
    }

    @Provides
    @Singleton
    fun providePartyRepository(@DatabaseProxy partyProxy: PartyProxy): PartyRepository {
        return PartyRepositoryImpl(partyProxy)
    }
}
