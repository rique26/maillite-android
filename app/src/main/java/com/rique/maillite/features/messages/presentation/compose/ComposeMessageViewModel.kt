package com.rique.maillite.features.messages.presentation.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.messages.domain.usecase.SendMessageUseCase
import com.rique.maillite.features.messages.presentation.compose.ComposeMessageUiState.SendStatus
import com.rique.maillite.features.users.domain.model.User
import com.rique.maillite.features.users.domain.usecase.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ComposeMessageViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeMessageUiState())
    val uiState: StateFlow<ComposeMessageUiState> = _uiState.asStateFlow()

    // Evento avulso só pra falha na busca de destinatário (não deve travar o formulário).
    private val _searchError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val searchError: SharedFlow<String> = _searchError.asSharedFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                // search() é chamada direto (suspend) dentro do collectLatest, não em um
                // viewModelScope.launch separado — assim, se uma query nova chegar antes da
                // busca anterior terminar, o collectLatest cancela ela corretamente em vez de
                // deixar um resultado desatualizado sobrescrever o mais recente por último.
                .collectLatest { query -> search(query) }
        }
    }

    fun onRecipientQueryChanged(query: String) {
        val state = _uiState.value

        // Se o usuário volta a editar o campo depois de já ter selecionado alguém,
        // a seleção anterior deixa de valer e a busca recomeça.
        val stillMatchesSelection = state.selectedRecipient?.name == query

        _uiState.value = state.copy(
            recipientQuery = query,
            selectedRecipient = if (stillMatchesSelection) state.selectedRecipient else null
        )

        if (!stillMatchesSelection) {
            queryFlow.value = query
        }
    }

    fun selectRecipient(user: User) {
        _uiState.value = _uiState.value.copy(
            recipientQuery = user.name,
            selectedRecipient = user,
            suggestions = emptyList()
        )
    }

    fun send(subject: String, body: String) {
        val state = _uiState.value
        val recipient = state.selectedRecipient

        if (recipient == null) {
            _uiState.value = state.copy(sendStatus = SendStatus.Error("Selecione um destinatário válido"))
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(sendStatus = SendStatus.Sending)

            // Validação de assunto/corpo em branco já é feita dentro do SendMessageUseCase.
            when (val result = sendMessageUseCase(recipient.id, subject, body)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(sendStatus = SendStatus.Sent)
                }

                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(sendStatus = SendStatus.Error(result.message))
                }
            }
        }
    }

    private suspend fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
            return
        }

        when (val result = searchUsersUseCase(query)) {
            is Result.Success -> _uiState.value = _uiState.value.copy(suggestions = result.data)
            is Result.Error -> _searchError.emit(result.message)
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}