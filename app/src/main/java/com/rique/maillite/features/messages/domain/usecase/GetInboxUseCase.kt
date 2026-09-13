package com.rique.maillite.features.messages.domain.usecase

import com.rique.maillite.core.domain.model.PagedResult
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.domain.repository.MessageRepository
import javax.inject.Inject

class GetInboxUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(page: Int, size: Int = DEFAULT_PAGE_SIZE): Result<PagedResult<Message>> {
        return messageRepository.getInbox(page, size)
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}