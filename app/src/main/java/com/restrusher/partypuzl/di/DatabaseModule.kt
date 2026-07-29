package com.restrusher.partypuzl.di

import android.content.Context
import com.restrusher.partypuzl.data.local.PartyPuzlDatabase
import com.restrusher.partypuzl.data.local.dao.CustomEntryDao
import com.restrusher.partypuzl.data.local.dao.CustomPackDao
import com.restrusher.partypuzl.data.local.dao.PartyDao
import com.restrusher.partypuzl.data.local.dao.PartyPhotoDao
import com.restrusher.partypuzl.data.local.dao.PlayerDao
import com.restrusher.partypuzl.data.local.dao.QuestionDao
import com.restrusher.partypuzl.data.local.dao.QuestionPackDao
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
    fun providePlayerDao(database: PartyPuzlDatabase): PlayerDao {
        return database.playerDao()
    }

    @Provides
    fun providePartyDao(database: PartyPuzlDatabase): PartyDao {
        return database.partyDao()
    }

    @Provides
    fun providePartyPhotoDao(database: PartyPuzlDatabase): PartyPhotoDao {
        return database.partyPhotoDao()
    }

    @Provides
    fun provideQuestionPackDao(database: PartyPuzlDatabase): QuestionPackDao {
        return database.questionPackDao()
    }

    @Provides
    fun provideQuestionDao(database: PartyPuzlDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    fun provideCustomPackDao(database: PartyPuzlDatabase): CustomPackDao {
        return database.customPackDao()
    }

    @Provides
    fun provideCustomEntryDao(database: PartyPuzlDatabase): CustomEntryDao {
        return database.customEntryDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): PartyPuzlDatabase {
        return PartyPuzlDatabase.getDatabase(appContext)
    }
}
