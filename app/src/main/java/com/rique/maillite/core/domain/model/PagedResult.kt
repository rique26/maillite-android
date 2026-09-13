package com.rique.maillite.core.domain.model

data class PagedResult<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int
) {
    val hasMore: Boolean
        get() = currentPage < totalPages - 1
}