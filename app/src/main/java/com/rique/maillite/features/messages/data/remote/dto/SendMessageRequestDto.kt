package com.rique.maillite.features.messages.data.remote.dto

data class SendMessageRequestDto(
    val recipientId: Long,
    val subject: String,
    val body: String
)