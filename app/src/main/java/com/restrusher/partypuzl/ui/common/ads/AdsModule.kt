package com.restrusher.partypuzl.ui.common.ads

import com.restrusher.partypuzl.ui.common.ads.max.MaxProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The one line each ad branch changes: which [AdsProvider] the app runs with. */
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    fun provideAdsProvider(provider: MaxProvider): AdsProvider = provider
}
