package com.restrusher.partypuzl.data.repositories.interfaces

import com.restrusher.partypuzl.data.local.dao.PackQuestionCount
import com.restrusher.partypuzl.data.local.entities.QuestionEntity

interface QuestionRepository {
    suspend fun count(): Int

    /** Questions in enabled packs that aren't individually muted. */
    suspend fun getPlayableQuestions(): List<QuestionEntity>

    suspend fun countsByPack(): Map<String, Int>

    /** Rebuilds the whole mapping — used when the catalog's mapping version changes. */
    suspend fun replaceAll(questions: List<QuestionEntity>)

    suspend fun setEnabled(id: String, enabled: Boolean)
}
