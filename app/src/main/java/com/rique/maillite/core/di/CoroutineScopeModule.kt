package com.rique.maillite.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Marca um CoroutineScope vinculado ao ciclo de vida do processo da aplicação, não a uma
 * tela específica. Necessário para trabalho "fire-and-forget" que precisa sobreviver à
 * navegação que o disparou (ex: sincronizar o token FCM depois do login, mesmo que a tela
 * de login já tenha sido destruída pela navegação para a Inbox).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}