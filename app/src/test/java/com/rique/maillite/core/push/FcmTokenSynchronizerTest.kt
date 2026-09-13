package com.rique.maillite.core.push

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.domain.usecase.UpdateFcmTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test

class FcmTokenSynchronizerTest {

    private lateinit var fcmTokenProvider: FcmTokenProvider
    private lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase
    private lateinit var synchronizer: FcmTokenSynchronizer

    @Before
    fun setUp() {
        fcmTokenProvider = mockk()
        updateFcmTokenUseCase = mockk()
        // UnconfinedTestDispatcher executa o corpo da coroutine lançada em sync() de forma
        // síncrona (até o próximo ponto de suspensão), então dá pra verificar logo em
        // seguida sem precisar de advanceUntilIdle(). Isso também simula, de propósito, o
        // ApplicationScope real: um escopo próprio, independente do que chamou sync().
        val testScope = CoroutineScope(UnconfinedTestDispatcher())
        synchronizer = FcmTokenSynchronizer(fcmTokenProvider, updateFcmTokenUseCase, testScope)
    }

    @Test
    fun `sync envia o token para o backend quando o Firebase retorna um token valido`() {
        // Arrange
        val token = "fcm-token-xyz"
        coEvery { fcmTokenProvider.getCurrentToken() } returns token
        coEvery { updateFcmTokenUseCase(token) } returns Result.Success(Unit)

        // Act
        synchronizer.sync()

        // Assert
        coVerify(exactly = 1) { updateFcmTokenUseCase(token) }
    }

    @Test
    fun `sync nao chama o backend quando o Firebase nao retorna token`() {
        // Arrange
        coEvery { fcmTokenProvider.getCurrentToken() } returns null

        // Act
        synchronizer.sync()

        // Assert
        coVerify(exactly = 0) { updateFcmTokenUseCase(any()) }
    }

    @Test
    fun `sync nao lanca excecao quando o backend retorna erro ao registrar o token`() {
        // Arrange
        val token = "fcm-token-xyz"
        coEvery { fcmTokenProvider.getCurrentToken() } returns token
        coEvery { updateFcmTokenUseCase(token) } returns Result.Error("401 - sessão inválida")

        // Act & Assert (não deve lançar — sync() é fire-and-forget por design)
        synchronizer.sync()
    }

    @Test
    fun `sync roda em um escopo independente de quem chamou`() {
        // Arrange: simula o cenário real do bug — o escopo que chama sync() é cancelado
        // logo em seguida (equivalente ao viewModelScope morrendo após popUpToInclusive).
        val token = "fcm-token-xyz"
        coEvery { fcmTokenProvider.getCurrentToken() } returns token
        coEvery { updateFcmTokenUseCase(token) } returns Result.Success(Unit)

        val callerScope = CoroutineScope(UnconfinedTestDispatcher())

        // Act
        callerScope.launch { synchronizer.sync() }
        callerScope.cancel() // o "ViewModel" morre logo depois de chamar sync()

        // Assert: mesmo com o caller cancelado, o backend foi chamado — porque sync()
        // roda no próprio ApplicationScope injetado, não no do caller.
        coVerify(exactly = 1) { updateFcmTokenUseCase(token) }
    }
}