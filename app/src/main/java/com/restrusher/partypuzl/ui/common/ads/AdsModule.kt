package com.restrusher.partypuzl.ui.common.ads

import com.restrusher.partypuzl.ui.common.ads.none.NoAdsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The one line each ad branch changes: which [AdsProvider] the app runs with. */
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    fun provideAdsProvider(provider: NoAdsProvider): AdsProvider = provider
}
