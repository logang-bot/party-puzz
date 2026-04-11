package com.restrusher.partypuzz.di

import com.restrusher.partypuzz.data.local.proxies.PartyLocalProxy
import com.restrusher.partypuzz.data.local.proxies.PartyPhotoLocalProxy
import com.restrusher.partypuzz.data.local.proxies.PlayerLocalProxy
import com.restrusher.partypuzz.data.proxies.PartyPhotoProxy
import com.restrusher.partypuzz.data.proxies.PartyProxy
import com.restrusher.partypuzz.data.proxies.PlayerProxy
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
}

// TODO: Implement a remote module to fetch data from an api
