package com.rique.maillite.features.auth.data.remote.dto

data class TokenResponseDto(
    val token: String,
    val type: String,
    val expiration: Long
)