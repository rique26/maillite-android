package com.rique.maillite.features.users.data.remote

import com.rique.maillite.features.users.data.remote.dto.FcmTokenRequestDto
import com.rique.maillite.features.users.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {

    @GET("v1/users/search")
    suspend fun search(@Query("query") query: String): Response<List<UserResponseDto>>

    @POST("v1/users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequestDto): Response<Unit>
}