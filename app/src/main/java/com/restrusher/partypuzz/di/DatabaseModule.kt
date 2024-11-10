package com.restrusher.partypuzz.di

import android.content.Context
import com.restrusher.partypuzz.data.local.PartyPuzzDatabase
import com.restrusher.partypuzz.data.local.dao.PlayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun providePlayerDao(database: PartyPuzzDatabase): PlayerDao {
        return database.playerDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): PartyPuzzDatabase {
        return PartyPuzzDatabase.getDatabase(appContext)
    }
}