package com.rique.maillite.features.messages.data.mapper

import com.rique.maillite.features.messages.data.remote.dto.MessageResponseDto
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.users.data.mapper.toDomain
import java.time.LocalDateTime

fun MessageResponseDto.toDomain(): Message = Message(
    id = id,
    sender = sender.toDomain(),
    recipient = recipient.toDomain(),
    subject = subject,
    body = body,
    sentAt = LocalDateTime.parse(sentAt),
    read = read
)