package com.rique.maillite.features.users.data.mapper

import com.rique.maillite.features.users.data.remote.dto.UserResponseDto
import com.rique.maillite.features.users.domain.model.User

fun UserResponseDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email
)