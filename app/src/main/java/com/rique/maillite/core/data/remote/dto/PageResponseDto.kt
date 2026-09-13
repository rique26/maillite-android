package com.rique.maillite.core.data.remote.dto

data class PageResponseDto<T>(
    val content: List<T>,
    val number: Int,
    val totalPages: Int
)