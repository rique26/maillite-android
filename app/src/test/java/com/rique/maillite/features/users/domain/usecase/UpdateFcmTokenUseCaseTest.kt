package com.rique.maillite.features.users.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateFcmTokenUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateFcmTokenUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        useCase = UpdateFcmTokenUseCase(userRepository)
    }

    @Test
    fun `invoke delega o token para o repositorio e repassa sucesso`() = runTest {
        // Arrange
        val token = "fcm-token-abc"
        coEvery { userRepository.updateFcmToken(token) } returns Result.Success(Unit)

        // Act
        val result = useCase(token)

        // Assert
        coVerify(exactly = 1) { userRepository.updateFcmToken(token) }
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke repassa o erro retornado pelo repositorio`() = runTest {
        // Arrange
        val token = "fcm-token-abc"
        coEvery { userRepository.updateFcmToken(token) } returns Result.Error("Erro inesperado (401)")

        // Act
        val result = useCase(token)

        // Assert
        assertEquals(Result.Error("Erro inesperado (401)"), result)
    }
}