package com.restrusher.partypuzl.di

import com.restrusher.partypuzl.data.proxies.PartyPhotoProxy
import com.restrusher.partypuzl.data.proxies.PartyProxy
import com.restrusher.partypuzl.data.proxies.PlayerProxy
import com.restrusher.partypuzl.data.proxies.QuestionPackProxy
import com.restrusher.partypuzl.data.proxies.QuestionProxy
import com.restrusher.partypuzl.data.repositories.PartyPhotoRepositoryImpl
import com.restrusher.partypuzl.data.repositories.PartyRepositoryImpl
import com.restrusher.partypuzl.data.repositories.PlayerRepositoryImpl
import com.restrusher.partypuzl.data.repositories.QuestionPackRepositoryImpl
import com.restrusher.partypuzl.data.repositories.QuestionRepositoryImpl
import com.restrusher.partypuzl.data.repositories.interfaces.PartyPhotoRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
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

    @Provides
    @Singleton
    fun providePartyPhotoRepository(@DatabaseProxy partyPhotoProxy: PartyPhotoProxy): PartyPhotoRepository {
        return PartyPhotoRepositoryImpl(partyPhotoProxy)
    }

    @Provides
    @Singleton
    fun provideQuestionPackRepository(
        @DatabaseProxy questionPackProxy: QuestionPackProxy
    ): QuestionPackRepository {
        return QuestionPackRepositoryImpl(questionPackProxy)
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(
        @DatabaseProxy questionProxy: QuestionProxy
    ): QuestionRepository {
        return QuestionRepositoryImpl(questionProxy)
    }
}
