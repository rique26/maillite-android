package com.rique.maillite.features.messages.domain.repository

import com.rique.maillite.core.domain.model.PagedResult
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.model.Message

interface MessageRepository {

    /** RF04 — POST /v1/messages */
    suspend fun send(recipientId: Long, subject: String, body: String): Result<Message>

    /** RF05 — GET /v1/messages/inbox?page=&size= (paginado) */
    suspend fun getInbox(page: Int, size: Int): Result<PagedResult<Message>>

    /** RF06 — GET /v1/messages/{id} (marca como lida no backend) */
    suspend fun getById(id: Long): Result<Message>

    /** RF07 — DELETE /v1/messages/{id} (soft delete) */
    suspend fun delete(id: Long): Result<Unit>
}