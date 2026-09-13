package com.rique.maillite.features.users.data.repository

import com.rique.maillite.core.data.remote.safeApiCall
import com.rique.maillite.core.data.remote.safeApiCallUnit
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.data.mapper.toDomain
import com.rique.maillite.features.users.data.remote.UserApiService
import com.rique.maillite.features.users.data.remote.dto.FcmTokenRequestDto
import com.rique.maillite.features.users.domain.model.User
import com.rique.maillite.features.users.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : UserRepository {

    override suspend fun search(query: String): Result<List<User>> {
        val result = safeApiCall { userApiService.search(query) }

        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return safeApiCallUnit { userApiService.updateFcmToken(FcmTokenRequestDto(token)) }
    }
}