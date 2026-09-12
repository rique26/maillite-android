package com.rique.maillite.features.messages.presentation.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.features.messages.presentation.compose.ComposeMessageUiState.SendStatus
import com.rique.maillite.features.users.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ComposeMessageViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeMessageUiState())
    val uiState: StateFlow<ComposeMessageUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    // TODO: substituir pelo GetInboxUseCase/UserRepository real
    // (GET /v1/users/search?query=) quando a Data layer existir.
    // O usuário "logado" (id=1) fica fora do pool pra simular a regra de
    // impedir autoenvio (RF04) enquanto não existe sessão real.
    private val fakeUserPool = listOf(
        User(id = 2, name = "Ana Souza", email = "ana.souza@mail.com"),
        User(id = 3, name = "Bruno Lima", email = "bruno.lima@mail.com"),
        User(id = 4, name = "Carla Mendes", email = "carla.mendes@mail.com"),
        User(id = 5, name = "Diego Alves", email = "diego.alves@mail.com"),
        User(id = 6, name = "Fernanda Ribeiro", email = "fernanda.ribeiro@mail.com"),
        User(id = 7, name = "Gustavo Pires", email = "gustavo.pires@mail.com"),
        User(id = 8, name = "Helena Castro", email = "helena.castro@mail.com")
    )

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
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

        val validationError = when {
            recipient == null -> "Selecione um destinatário válido"
            subject.isBlank() -> "Informe o assunto"
            body.isBlank() -> "Informe o corpo da mensagem"
            else -> null
        }

        if (validationError != null) {
            _uiState.value = state.copy(sendStatus = SendStatus.Error(validationError))
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(sendStatus = SendStatus.Sending)

            // TODO: substituir pela chamada real ao MessageRepository/SendMessageUseCase
            // (POST /v1/messages) quando a Data layer existir. Por enquanto, fake fixo.
            delay(SEND_DELAY_MS)

            _uiState.value = _uiState.value.copy(sendStatus = SendStatus.Sent)
        }
    }

    private fun search(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            fakeUserPool.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = _uiState.value.copy(suggestions = results)
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val SEND_DELAY_MS = 800L
    }
}
