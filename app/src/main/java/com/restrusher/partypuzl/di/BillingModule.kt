package com.restrusher.partypuzl.di

import com.restrusher.partypuzl.data.billing.BillingManager
import com.restrusher.partypuzl.data.billing.PremiumBilling
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    abstract fun bindPremiumBilling(billingManager: BillingManager): PremiumBilling
}
