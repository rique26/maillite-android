package com.rique.maillite.features.messages.presentation.inbox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.users.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    // TODO: substituir por dados reais vindos do MessageRepository/GetInboxUseCase
    // (GET /v1/messages/inbox?page=&size=&sort=sentAt,desc) quando a Data layer existir.
    // O tamanho de página (20) já é o mesmo combinado para o backend, pra facilitar a troca depois.
    @RequiresApi(Build.VERSION_CODES.O)
    private val allMessages: List<Message> = generateFakeMessages()

    private var currentPage = 0
    private var lastDeleted: Pair<Message, Int>? = null

    init {
        loadFirstPage()
    }

    fun retry() {
        loadFirstPage()
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = InboxUiState.Loading
            delay(LOAD_DELAY_MS)

            currentPage = 1
            val firstPage = allMessages.take(PAGE_SIZE)

            _uiState.value = if (firstPage.isEmpty()) {
                InboxUiState.Empty
            } else {
                InboxUiState.Success(
                    messages = firstPage,
                    endReached = firstPage.size >= allMessages.size
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

            val nextChunk = allMessages.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)
            currentPage++

            val updatedMessages = state.messages + nextChunk
            _uiState.value = InboxUiState.Success(
                messages = updatedMessages,
                isLoadingMore = false,
                endReached = updatedMessages.size >= allMessages.size
            )
        }
    }

    fun deleteMessage(message: Message) {
        val state = _uiState.value
        if (state !is InboxUiState.Success) return

        val index = state.messages.indexOf(message)
        if (index == -1) return

        lastDeleted = message to index

        val updatedMessages = state.messages.toMutableList().apply { removeAt(index) }
        _uiState.value = if (updatedMessages.isEmpty()) {
            InboxUiState.Empty
        } else {
            state.copy(messages = updatedMessages)
        }
    }

    fun undoDelete() {
        val (message, index) = lastDeleted ?: return

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateFakeMessages(): List<Message> {
        val currentUser = User(id = 1, name = "Você", email = "voce@mail.com")
        val senders = listOf(
            User(id = 2, name = "Ana Souza", email = "ana.souza@mail.com"),
            User(id = 3, name = "Bruno Lima", email = "bruno.lima@mail.com"),
            User(id = 4, name = "Carla Mendes", email = "carla.mendes@mail.com"),
            User(id = 5, name = "Diego Alves", email = "diego.alves@mail.com"),
            User(id = 6, name = "Fernanda Ribeiro", email = "fernanda.ribeiro@mail.com")
        )

        return (1..TOTAL_FAKE_MESSAGES).map { index ->
            val sender = senders[index % senders.size]
            Message(
                id = index.toLong(),
                sender = sender,
                recipient = currentUser,
                subject = "Assunto de teste #$index",
                body = "Corpo completo da mensagem número $index, gerado como dado fake " +
                        "para validar a camada de apresentação antes da integração com a API.",
                sentAt = LocalDateTime.now().minusHours(index.toLong()),
                read = index % 3 == 0
            )
        }
    }

    private companion object {
        const val LOAD_DELAY_MS = 600L
        const val PAGE_SIZE = 20
        const val TOTAL_FAKE_MESSAGES = 45
    }
}
