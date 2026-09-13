package com.rique.maillite.features.users.domain.usecase

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.domain.model.User
import com.rique.maillite.features.users.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.isBlank()) return Result.Success(emptyList())
        return userRepository.search(query)
    }
}