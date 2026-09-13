package com.rique.maillite.features.messages.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.usecase.DeleteMessageUseCase
import com.rique.maillite.features.messages.domain.usecase.GetMessageByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Chave "messageId" vem direto do argumento do nav_graph via Safe Args.
    private val messageId: Long = checkNotNull(savedStateHandle["messageId"]) {
        "MessageDetailFragment requer o argumento messageId"
    }

    private val _uiState = MutableStateFlow<MessageDetailUiState>(MessageDetailUiState.Loading)
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()

    // Evento avulso só pra falha ao excluir (não deve apagar o conteúdo já carregado da tela).
    private val _deleteError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deleteError: SharedFlow<String> = _deleteError.asSharedFlow()

    init {
        loadMessage()
    }

    private fun loadMessage() {
        viewModelScope.launch {
            _uiState.value = MessageDetailUiState.Loading

            // GET /v1/messages/{id} já marca como lida no backend (RF06) — não precisa de
            // uma segunda chamada aqui.
            when (val result = getMessageByIdUseCase(messageId)) {
                is Result.Success -> _uiState.value = MessageDetailUiState.Success(result.data)
                is Result.Error -> _uiState.value = MessageDetailUiState.Error(result.message)
            }
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state !is MessageDetailUiState.Success) return

        viewModelScope.launch {
            when (val result = deleteMessageUseCase(state.message.id)) {
                is Result.Success -> _uiState.value = MessageDetailUiState.Deleted
                is Result.Error -> _deleteError.emit(result.message)
            }
        }
    }
}