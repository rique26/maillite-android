package com.rique.maillite.core.di

import com.rique.maillite.features.auth.data.repository.AuthRepositoryImpl
import com.rique.maillite.features.auth.domain.repository.AuthRepository
import com.rique.maillite.features.messages.data.repository.MessageRepositoryImpl
import com.rique.maillite.features.messages.domain.repository.MessageRepository
import com.rique.maillite.features.users.data.repository.UserRepositoryImpl
import com.rique.maillite.features.users.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository
}