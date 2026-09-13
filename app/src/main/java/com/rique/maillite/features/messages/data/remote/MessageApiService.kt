package com.rique.maillite.features.messages.data.remote

import com.rique.maillite.core.data.remote.dto.PageResponseDto
import com.rique.maillite.features.messages.data.remote.dto.MessageResponseDto
import com.rique.maillite.features.messages.data.remote.dto.SendMessageRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApiService {

    @POST("v1/messages")
    suspend fun send(@Body request: SendMessageRequestDto): Response<MessageResponseDto>

    @GET("v1/messages/inbox")
    suspend fun getInbox(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "sentAt,desc"
    ): Response<PageResponseDto<MessageResponseDto>>

    @GET("v1/messages/{id}")
    suspend fun getById(@Path("id") id: Long): Response<MessageResponseDto>

    @DELETE("v1/messages/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}