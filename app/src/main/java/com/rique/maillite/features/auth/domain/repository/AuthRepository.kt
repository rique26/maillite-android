package com.rique.maillite.features.auth.domain.repository

import com.rique.maillite.core.domain.util.Result

interface AuthRepository {

    /** RF02 — POST /v1/auth/login. Em caso de sucesso, o token já fica salvo internamente. */
    suspend fun login(email: String, password: String): Result<Unit>

    /** RF01 — POST /v1/auth/register. Não loga automaticamente (o backend não retorna token). */
    suspend fun register(name: String, email: String, password: String): Result<Unit>

    /** Usado pela Splash pra decidir a navegação inicial. */
    suspend fun hasValidSession(): Boolean
}