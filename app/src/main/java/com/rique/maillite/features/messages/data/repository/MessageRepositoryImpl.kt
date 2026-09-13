package com.rique.maillite.features.messages.data.repository

import com.rique.maillite.core.data.mapper.toDomain
import com.rique.maillite.core.data.remote.safeApiCall
import com.rique.maillite.core.data.remote.safeApiCallUnit
import com.rique.maillite.core.domain.model.PagedResult
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.data.mapper.toDomain
import com.rique.maillite.features.messages.data.remote.MessageApiService
import com.rique.maillite.features.messages.data.remote.dto.SendMessageRequestDto
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.domain.repository.MessageRepository
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageApiService: MessageApiService
) : MessageRepository {

    override suspend fun send(recipientId: Long, subject: String, body: String): Result<Message> {
        val result = safeApiCall { messageApiService.send(SendMessageRequestDto(recipientId, subject, body)) }

        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
        }
    }

    override suspend fun getInbox(page: Int, size: Int): Result<PagedResult<Message>> {
        val result = safeApiCall { messageApiService.getInbox(page, size) }

        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain { dto -> dto.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun getById(id: Long): Result<Message> {
        val result = safeApiCall { messageApiService.getById(id) }

        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
        }
    }

    override suspend fun delete(id: Long): Result<Unit> {
        return safeApiCallUnit { messageApiService.delete(id) }
    }
}