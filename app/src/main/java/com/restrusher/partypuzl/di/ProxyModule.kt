package com.restrusher.partypuzl.di

import com.restrusher.partypuzl.data.local.proxies.CustomPackLocalProxy
import com.restrusher.partypuzl.data.local.proxies.PartyLocalProxy
import com.restrusher.partypuzl.data.local.proxies.PartyPhotoLocalProxy
import com.restrusher.partypuzl.data.local.proxies.PlayerLocalProxy
import com.restrusher.partypuzl.data.local.proxies.QuestionLocalProxy
import com.restrusher.partypuzl.data.local.proxies.QuestionPackLocalProxy
import com.restrusher.partypuzl.data.proxies.CustomPackProxy
import com.restrusher.partypuzl.data.proxies.PartyPhotoProxy
import com.restrusher.partypuzl.data.proxies.PartyProxy
import com.restrusher.partypuzl.data.proxies.PlayerProxy
import com.restrusher.partypuzl.data.proxies.QuestionPackProxy
import com.restrusher.partypuzl.data.proxies.QuestionProxy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class DatabaseProxy

@InstallIn(SingletonComponent::class)
@Module
abstract class ProxyModule {

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalPlayerProxy(impl: PlayerLocalProxy): PlayerProxy

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalPartyProxy(impl: PartyLocalProxy): PartyProxy

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalPartyPhotoProxy(impl: PartyPhotoLocalProxy): PartyPhotoProxy

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalQuestionPackProxy(impl: QuestionPackLocalProxy): QuestionPackProxy

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalQuestionProxy(impl: QuestionLocalProxy): QuestionProxy

    @DatabaseProxy
    @Singleton
    @Binds
    abstract fun bindLocalCustomPackProxy(impl: CustomPackLocalProxy): CustomPackProxy
}

// TODO: Implement a remote module to fetch data from an api
