package com.rique.maillite.features.messages.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(recipientId: Long, subject: String, body: String): Result<Message> {
        if (subject.isBlank() || body.isBlank()) {
            return Result.Error("Preencha assunto e corpo da mensagem")
        }
        return messageRepository.send(recipientId, subject, body)
    }
}