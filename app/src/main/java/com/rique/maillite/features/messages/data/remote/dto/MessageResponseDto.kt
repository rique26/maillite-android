package com.rique.maillite.features.messages.data.remote.dto

import com.rique.maillite.features.users.data.remote.dto.UserResponseDto

data class MessageResponseDto(
    val id: Long,
    val sender: UserResponseDto,
    val recipient: UserResponseDto,
    val subject: String,
    val body: String,
    val sentAt: String,
    val read: Boolean
)