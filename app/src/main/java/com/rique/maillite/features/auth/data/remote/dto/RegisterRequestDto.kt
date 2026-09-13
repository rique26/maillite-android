package com.rique.maillite.features.auth.data.remote.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)