package com.restrusher.partypuzl.data.local.proxies

import com.restrusher.partypuzl.data.local.dao.PackQuestionCount
import com.restrusher.partypuzl.data.local.dao.QuestionDao
import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.proxies.QuestionProxy
import javax.inject.Inject

class QuestionLocalProxy @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionProxy {

    override suspend fun count(): Int = questionDao.getAll().size

    override suspend fun getPlayableQuestions(): List<QuestionEntity> =
        questionDao.getPlayableQuestions()

    override suspend fun countsByPack(): List<PackQuestionCount> = questionDao.countsByPack()

    override suspend fun replaceAll(questions: List<QuestionEntity>) =
        questionDao.replaceAll(questions)

    override suspend fun setEnabled(id: String, enabled: Boolean) =
        questionDao.setEnabled(id, enabled)
}
