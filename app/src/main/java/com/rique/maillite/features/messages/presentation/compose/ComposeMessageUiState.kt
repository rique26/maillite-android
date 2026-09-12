package com.rique.maillite.features.messages.presentation.compose

import com.rique.maillite.features.users.domain.model.User

data class ComposeMessageUiState(
    val recipientQuery: String = "",
    val suggestions: List<User> = emptyList(),
    val selectedRecipient: User? = null,
    val sendStatus: SendStatus = SendStatus.Idle
) {
    sealed class SendStatus {
        data object Idle : SendStatus()
        data object Sending : SendStatus()
        data object Sent : SendStatus()
        data class Error(val message: String) : SendStatus()
    }
}
