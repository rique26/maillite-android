package com.rique.maillite.core.push

import com.rique.maillite.core.di.ApplicationScope
import com.rique.maillite.features.users.domain.usecase.UpdateFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ponto único que junta "pegar o token atual do Firebase" com "mandar pro backend" (RF08).
 * Fica em core/push (não em domain) porque depende do SDK do Firebase — não é regra de
 * negócio pura. Chamado depois de login bem-sucedido, quando a Splash confirma sessão
 * válida, e sempre que o Firebase rotaciona o token (onNewToken do Service).
 *
 * sync() se auto-lança no [ApplicationScope] em vez de depender do escopo de quem chama:
 * tanto LoginViewModel quanto SplashViewModel navegam para a Inbox logo depois de chamar
 * isso, com popUpToInclusive removendo a tela (e o ViewModel, e o viewModelScope) da back
 * stack quase imediatamente — o que cancelava essa coroutine no meio da espera pelo Firebase,
 * antes do callback disparar, quando ela rodava no escopo de quem chamou.
 */
@Singleton
class FcmTokenSynchronizer @Inject constructor(
    private val fcmTokenProvider: FcmTokenProvider,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun sync() {
        applicationScope.launch {
            val token = fcmTokenProvider.getCurrentToken() ?: return@launch
            // Resultado ignorado de propósito: falha em registrar o token de push nunca deve
            // travar login/navegação — o app continua funcionando normalmente sem push.
            updateFcmTokenUseCase(token)
        }
    }
}