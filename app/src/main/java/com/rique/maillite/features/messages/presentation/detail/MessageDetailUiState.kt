package com.rique.maillite.features.messages.presentation.detail

import com.rique.maillite.features.messages.domain.model.Message

sealed class MessageDetailUiState {

    data object Loading : MessageDetailUiState()

    data class Success(val message: Message) : MessageDetailUiState()

    data class Error(val message: String) : MessageDetailUiState()

    data object Deleted : MessageDetailUiState()
}