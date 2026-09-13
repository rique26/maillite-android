package com.rique.maillite.features.auth.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<Unit> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.Error("Preencha todos os campos")
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return Result.Error("A senha deve ter no mínimo $MIN_PASSWORD_LENGTH caracteres")
        }
        return authRepository.register(name, email, password)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}