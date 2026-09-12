package com.rique.maillite.features.messages.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.features.messages.data.fake.FakeMessagesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messagesDataSource: FakeMessagesDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Chave "messageId" vem direto do argumento do nav_graph via Safe Args.
    private val messageId: Long = checkNotNull(savedStateHandle["messageId"]) {
        "MessageDetailFragment requer o argumento messageId"
    }

    private val _uiState = MutableStateFlow<MessageDetailUiState>(MessageDetailUiState.Loading)
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()

    init {
        loadMessage()
    }

    private fun loadMessage() {
        viewModelScope.launch {
            _uiState.value = MessageDetailUiState.Loading

            // TODO: substituir pela chamada real ao MessageRepository/GetMessageDetailUseCase
            // (GET /v1/messages/{id}, que já marca como lida no backend) quando a Data layer
            // existir. Por enquanto, lê e marca como lida na fonte fake compartilhada.
            delay(LOAD_DELAY_MS)

            val message = messagesDataSource.getById(messageId)
            if (message == null) {
                _uiState.value = MessageDetailUiState.Error(MESSAGE_NOT_FOUND)
                return@launch
            }

            if (!message.read) {
                messagesDataSource.markAsRead(messageId)
            }

            _uiState.value = MessageDetailUiState.Success(
                messagesDataSource.getById(messageId) ?: message
            )
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state !is MessageDetailUiState.Success) return

        // TODO: substituir pela chamada real DELETE /v1/messages/{id} quando a Data layer existir.
        messagesDataSource.delete(state.message.id)
        _uiState.value = MessageDetailUiState.Deleted
    }

    private companion object {
        const val LOAD_DELAY_MS = 400L
        const val MESSAGE_NOT_FOUND = "Mensagem não encontrada"
    }
}