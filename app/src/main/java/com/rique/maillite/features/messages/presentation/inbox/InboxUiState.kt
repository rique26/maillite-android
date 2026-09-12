package com.rique.maillite.features.messages.presentation.inbox

import com.rique.maillite.features.messages.domain.model.Message

sealed class InboxUiState {

    data object Loading : InboxUiState()

    data object Empty : InboxUiState()

    data class Error(val message: String) : InboxUiState()

    data class Success(
        val messages: List<Message>,
        val isLoadingMore: Boolean = false,
        val endReached: Boolean = false
    ) : InboxUiState()
}
