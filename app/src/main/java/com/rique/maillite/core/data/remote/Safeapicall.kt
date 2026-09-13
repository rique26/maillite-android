package com.rique.maillite.core.data.remote

import com.google.gson.Gson
import com.rique.maillite.core.data.remote.dto.ErrorResponseDto
import com.rique.maillite.core.domain.util.Result
import retrofit2.Response
import java.io.IOException

/**
 * Pra chamadas onde um corpo de sucesso é esperado (login, search, send, getInbox, getById,
 * e também register — que devolve ResponseBody não-nulo mesmo vazio, então body() nunca é
 * null aqui de verdade).
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) Result.Success(body) else Result.Error("Resposta vazia do servidor")
        } else {
            Result.Error(parseErrorMessage(response))
        }
    } catch (e: IOException) {
        Result.Error("Não foi possível conectar ao servidor. Verifique sua internet.")
    } catch (e: Exception) {
        Result.Error("Ocorreu um erro inesperado.")
    }
}

/**
 * Pra chamadas 204 No Content (updateFcmToken, delete): o Retrofit devolve body() null por
 * design nesse caso, então aqui sucesso é só isSuccessful() — não faz sentido checar body.
 */
suspend fun <T> safeApiCallUnit(apiCall: suspend () -> Response<T>): Result<Unit> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            Result.Success(Unit)
        } else {
            Result.Error(parseErrorMessage(response))
        }
    } catch (e: IOException) {
        Result.Error("Não foi possível conectar ao servidor. Verifique sua internet.")
    } catch (e: Exception) {
        Result.Error("Ocorreu um erro inesperado.")
    }
}

private fun <T> parseErrorMessage(response: Response<T>): String {
    return try {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrBlank()) {
            "Erro inesperado (${response.code()})"
        } else {
            Gson().fromJson(errorBody, ErrorResponseDto::class.java).message
        }
    } catch (e: Exception) {
        "Erro inesperado (${response.code()})"
    }
}