package com.rique.maillite.features.messages.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.features.messages.data.fake.FakeMessagesDataSource
import com.rique.maillite.features.messages.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val messagesDataSource: FakeMessagesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    // Ordem estável definida na primeira carga (equivalente ao que um GET /v1/messages/inbox
    // ordenado por sentAt desc retornaria). A paginação avança sobre essa lista de ids;
    // o conteúdo de cada mensagem é sempre lido de volta da fonte compartilhada.
    private var allIdsSorted: List<Long> = emptyList()
    private var currentPage = 0
    private var lastDeleted: Pair<Message, Int>? = null

    init {
        loadFirstPage()
    }

    fun retry() {
        loadFirstPage()
    }

    /**
     * Rechama isso no onResume() da Fragment: sincroniza as mensagens já carregadas com o
     * estado atual da fonte compartilhada (uma leitura ou exclusão feita na tela de Detalhe
     * só aparece aqui quando o usuário volta pra Inbox, sem precisar recarregar do zero).
     */
    fun refreshFromSource() {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val sourceById = messagesDataSource.messages.value.associateBy { it.id }
        val updatedMessages = state.messages.mapNotNull { sourceById[it.id] }

        _uiState.value = if (updatedMessages.isEmpty()) {
            InboxUiState.Empty
        } else {
            state.copy(messages = updatedMessages)
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = InboxUiState.Loading
            delay(LOAD_DELAY_MS)

            allIdsSorted = messagesDataSource.messages.value
                .sortedByDescending { it.sentAt }
                .map { it.id }
            currentPage = 1

            val firstPageIds = allIdsSorted.take(PAGE_SIZE)
            val firstPage = resolveMessages(firstPageIds)

            _uiState.value = if (firstPage.isEmpty()) {
                InboxUiState.Empty
            } else {
                InboxUiState.Success(
                    messages = firstPage,
                    endReached = firstPageIds.size >= allIdsSorted.size
                )
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state !is InboxUiState.Success || state.isLoadingMore || state.endReached) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            delay(LOAD_DELAY_MS)

            val nextChunkIds = allIdsSorted.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)
            currentPage++

            val updatedMessages = state.messages + resolveMessages(nextChunkIds)
            _uiState.value = InboxUiState.Success(
                messages = updatedMessages,
                isLoadingMore = false,
                endReached = updatedMessages.size >= allIdsSorted.size
            )
        }
    }

    fun deleteMessage(message: Message) {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val index = state.messages.indexOf(message)
        if (index == -1) return

        lastDeleted = message to index
        messagesDataSource.delete(message.id)

        val updatedMessages = state.messages.toMutableList().apply { removeAt(index) }
        _uiState.value = if (updatedMessages.isEmpty()) {
            InboxUiState.Empty
        } else {
            state.copy(messages = updatedMessages)
        }
    }

    fun undoDelete() {
        val (message, index) = lastDeleted ?: return
        messagesDataSource.restore(message, index)

        val current = _uiState.value
        val currentMessages = when (current) {
            is InboxUiState.Success -> current.messages
            is InboxUiState.Empty -> emptyList()
            else -> return
        }

        val updatedMessages = currentMessages.toMutableList().apply {
            add(index.coerceAtMost(size), message)
        }

        _uiState.value = InboxUiState.Success(
            messages = updatedMessages,
            endReached = (current as? InboxUiState.Success)?.endReached ?: false
        )
        lastDeleted = null
    }

    private fun resolveMessages(ids: List<Long>): List<Message> {
        val sourceById = messagesDataSource.messages.value.associateBy { it.id }
        return ids.mapNotNull { sourceById[it] }
    }

    private companion object {
        const val LOAD_DELAY_MS = 600L
        const val PAGE_SIZE = 20
    }
}