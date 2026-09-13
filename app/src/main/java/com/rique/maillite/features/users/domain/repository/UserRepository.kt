package com.rique.maillite.features.users.domain.repository

import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.users.domain.model.User

interface UserRepository {

    /** RF03 — GET /v1/users/search?query= */
    suspend fun search(query: String): Result<List<User>>

    /** RF08 — POST /v1/users/fcm-token */
    suspend fun updateFcmToken(token: String): Result<Unit>
}