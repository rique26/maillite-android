package com.rique.maillite.features.messages.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.auth.domain.usecase.LogoutUseCase
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.domain.usecase.DeleteMessageUseCase
import com.rique.maillite.features.messages.domain.usecase.GetInboxUseCase
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
class InboxViewModel @Inject constructor(
    private val getInboxUseCase: GetInboxUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    // Evento avulso só pra falha ao carregar mais itens (não deve derrubar a lista já visível).
    private val _loadMoreError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val loadMoreError: SharedFlow<String> = _loadMoreError.asSharedFlow()

    // Evento avulso de navegação: a Fragment só navega pro Login depois que o token local
    // já foi limpo (garantia de que um "voltar" não deixa a sessão velha acessível).
    private val _loggedOut = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loggedOut: SharedFlow<Unit> = _loggedOut.asSharedFlow()

    private var currentPage = 0

    // Guardado enquanto a Snackbar de "Desfazer" está no ar; só vira uma chamada real de
    // DELETE se expirar sem o usuário desfazer (ver confirmPendingDelete/undoDelete).
    private var pendingDeletion: Pair<Message, Int>? = null

    init {
        loadFirstPage()
    }

    fun retry() {
        loadFirstPage()
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = InboxUiState.Loading

            when (val result = getInboxUseCase(page = FIRST_PAGE)) {
                is Result.Success -> {
                    currentPage = FIRST_PAGE
                    val paged = result.data
                    _uiState.value = if (paged.items.isEmpty()) {
                        InboxUiState.Empty
                    } else {
                        InboxUiState.Success(messages = paged.items, endReached = !paged.hasMore)
                    }
                }

                is Result.Error -> {
                    _uiState.value = InboxUiState.Error(result.message)
                }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state !is InboxUiState.Success || state.isLoadingMore || state.endReached) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)

            when (val result = getInboxUseCase(page = currentPage + 1)) {
                is Result.Success -> {
                    currentPage++
                    val paged = result.data
                    _uiState.value = InboxUiState.Success(
                        messages = state.messages + paged.items,
                        isLoadingMore = false,
                        endReached = !paged.hasMore
                    )
                }

                is Result.Error -> {
                    _uiState.value = state.copy(isLoadingMore = false)
                    _loadMoreError.emit(result.message)
                }
            }
        }
    }

    fun deleteMessage(message: Message) {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val index = state.messages.indexOf(message)
        if (index == -1) return

        pendingDeletion = message to index

        val updated = state.messages.toMutableList().apply { removeAt(index) }
        _uiState.value = if (updated.isEmpty()) InboxUiState.Empty else state.copy(messages = updated)
    }

    fun undoDelete() {
        val (message, index) = pendingDeletion ?: return
        pendingDeletion = null

        val current = _uiState.value
        val currentMessages = when (current) {
            is InboxUiState.Success -> current.messages
            is InboxUiState.Empty -> emptyList()
            else -> return
        }

        val updated = currentMessages.toMutableList().apply { add(index.coerceAtMost(size), message) }
        _uiState.value = InboxUiState.Success(
            messages = updated,
            endReached = (current as? InboxUiState.Success)?.endReached ?: false
        )
    }

    /**
     * Chamado pela Fragment quando a Snackbar de "Desfazer" expira sem toque do usuário.
     * Só aqui o DELETE real acontece no backend — se undoDelete() rodou antes, isso nunca chama.
     */
    fun confirmPendingDelete() {
        val (message, _) = pendingDeletion ?: return
        pendingDeletion = null

        viewModelScope.launch {
            // O item já saiu da lista visualmente. Se a chamada falhar aqui, não há rollback
            // automático nessa versão — aceitável pro escopo do MVP (o item volta na próxima
            // vez que a inbox recarregar do zero).
            deleteMessageUseCase(message.id)
        }
    }

    /** Aplicado quando o Detalhe avisa (via Fragment Result) que essa mensagem foi lida. */
    fun markLocalAsRead(messageId: Long) {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val updated = state.messages.map { message ->
            if (message.id == messageId) message.copy(read = true) else message
        }
        _uiState.value = state.copy(messages = updated)
    }

    /** Aplicado quando o Detalhe avisa (via Fragment Result) que essa mensagem foi excluída. */
    fun removeLocal(messageId: Long) {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val updated = state.messages.filterNot { it.id == messageId }
        _uiState.value = if (updated.isEmpty()) InboxUiState.Empty else state.copy(messages = updated)
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.emit(Unit)
        }
    }

    private companion object {
        const val FIRST_PAGE = 0
    }
}