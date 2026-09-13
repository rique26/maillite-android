package com.rique.maillite.features.users.data.repository

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.data.remote.UserApiService
import com.rique.maillite.features.users.data.remote.dto.FcmTokenRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class UserRepositoryImplTest {

    private lateinit var userApiService: UserApiService
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        userApiService = mockk()
        repository = UserRepositoryImpl(userApiService)
    }

    @Test
    fun `updateFcmToken retorna sucesso quando a API responde 204`() = runTest {
        // Arrange
        val token = "fcm-token-abc"
        coEvery { userApiService.updateFcmToken(FcmTokenRequestDto(token)) } returns
                Response.success(null)

        // Act
        val result = repository.updateFcmToken(token)

        // Assert
        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateFcmToken retorna erro quando a API responde 401`() = runTest {
        // Arrange
        val token = "fcm-token-abc"
        val errorJson = """{ "message": "Token inválido ou expirado", "status": 401 }"""
        val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { userApiService.updateFcmToken(FcmTokenRequestDto(token)) } returns
                Response.error(401, errorBody)

        // Act
        val result = repository.updateFcmToken(token)

        // Assert
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("inválido"))
    }
}