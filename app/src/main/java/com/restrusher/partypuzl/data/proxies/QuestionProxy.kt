package com.restrusher.partypuzl.data.proxies

import com.restrusher.partypuzl.data.local.dao.PackQuestionCount
import com.restrusher.partypuzl.data.local.entities.QuestionEntity

interface QuestionProxy {
    suspend fun count(): Int
    suspend fun getPlayableQuestions(): List<QuestionEntity>
    suspend fun countsByPack(): List<PackQuestionCount>
    suspend fun replaceAll(questions: List<QuestionEntity>)
    suspend fun setEnabled(id: String, enabled: Boolean)
}
