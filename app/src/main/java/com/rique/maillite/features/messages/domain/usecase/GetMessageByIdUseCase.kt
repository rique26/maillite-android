package com.rique.maillite.features.messages.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageByIdUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(id: Long): Result<Message> = messageRepository.getById(id)
}