package com.rique.maillite.features.messages.domain.model

import com.rique.maillite.features.users.domain.model.User
import java.time.LocalDateTime

data class Message(
    val id: Long,
    val sender: User,
    val recipient: User,
    val subject: String,
    val body: String,
    val sentAt: LocalDateTime,
    val read: Boolean
)
