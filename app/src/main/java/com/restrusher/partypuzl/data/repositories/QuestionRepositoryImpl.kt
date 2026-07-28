package com.restrusher.partypuzl.data.repositories

import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.proxies.QuestionProxy
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import com.restrusher.partypuzl.di.DatabaseProxy

class QuestionRepositoryImpl(
    @DatabaseProxy private val questionLocalProxy: QuestionProxy
) : QuestionRepository {

    override suspend fun count(): Int = questionLocalProxy.count()

    override suspend fun getPlayableQuestions(): List<QuestionEntity> =
        questionLocalProxy.getPlayableQuestions()

    override suspend fun countsByPack(): Map<String, Int> =
        questionLocalProxy.countsByPack().associate { it.packId to it.count }

    override suspend fun replaceAll(questions: List<QuestionEntity>) =
        questionLocalProxy.replaceAll(questions)

    override suspend fun setEnabled(id: String, enabled: Boolean) =
        questionLocalProxy.setEnabled(id, enabled)
}
