package com.rique.maillite.features.auth.data.remote

import com.rique.maillite.features.auth.data.remote.dto.LoginRequestDto
import com.rique.maillite.features.auth.data.remote.dto.RegisterRequestDto
import com.rique.maillite.features.auth.data.remote.dto.TokenResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<ResponseBody>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenResponseDto>
}