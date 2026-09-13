package com.rique.maillite.features.auth.data.repository

import com.rique.maillite.core.data.local.TokenDataStore
import com.rique.maillite.core.data.remote.safeApiCall
import com.rique.maillite.core.data.remote.safeApiCallUnit
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.core.util.JwtUtil
import com.rique.maillite.features.auth.data.remote.AuthApiService
import com.rique.maillite.features.auth.data.remote.dto.LoginRequestDto
import com.rique.maillite.features.auth.data.remote.dto.RegisterRequestDto
import com.rique.maillite.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        val result = safeApiCall { authApiService.login(LoginRequestDto(email, password)) }

        return when (result) {
            is Result.Success -> {
                tokenDataStore.saveToken(result.data.token)
                Result.Success(Unit)
            }
            is Result.Error -> result
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return safeApiCallUnit { authApiService.register(RegisterRequestDto(name, email, password)) }
    }

    override suspend fun hasValidSession(): Boolean {
        val token = tokenDataStore.getToken()
        return !token.isNullOrBlank() && !JwtUtil.isExpired(token)
    }

    override suspend fun logout() {
        tokenDataStore.clearToken()
    }
}