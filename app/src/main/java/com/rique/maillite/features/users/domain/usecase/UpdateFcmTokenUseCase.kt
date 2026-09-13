package com.rique.maillite.features.users.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.domain.repository.UserRepository
import javax.inject.Inject

class UpdateFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> = userRepository.updateFcmToken(token)
}