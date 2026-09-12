package com.rique.maillite.features.messages.data.fake

import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.users.domain.model.User
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TODO: fonte de dados temporária, só pra camada de presentation antes de existir a Data layer
 * real. Será substituída por um MessageRepository de verdade consumindo os endpoints
 * GET /v1/messages/inbox, GET /v1/messages/{id} e DELETE /v1/messages/{id}.
 *
 * É `@Singleton` de propósito: Inbox e Detalhe da Mensagem compartilham essa mesma instância
 * via Hilt, então marcar como lida ou excluir em uma tela reflete na outra durante a sessão.
 */
@Singleton
class FakeMessagesDataSource @Inject constructor() {

    private val _messages = MutableStateFlow(generateFakeMessages())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun getById(id: Long): Message? = _messages.value.firstOrNull { it.id == id }

    fun markAsRead(id: Long) {
        _messages.value = _messages.value.map { message ->
            if (message.id == id) message.copy(read = true) else message
        }
    }

    fun delete(id: Long) {
        _messages.value = _messages.value.filterNot { it.id == id }
    }

    fun restore(message: Message, index: Int) {
        val updated = _messages.value.toMutableList()
        updated.add(index.coerceAtMost(updated.size), message)
        _messages.value = updated
    }

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
        const val TOTAL_FAKE_MESSAGES = 45
    }
}
