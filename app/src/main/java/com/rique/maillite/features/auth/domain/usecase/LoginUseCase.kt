package com.rique.maillite.features.auth.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Preencha e-mail e senha")
        }
        return authRepository.login(email, password)
    }
}