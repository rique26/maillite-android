package com.rique.maillite.features.auth.domain.usecase

import com.rique.maillite.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean = authRepository.hasValidSession()
}