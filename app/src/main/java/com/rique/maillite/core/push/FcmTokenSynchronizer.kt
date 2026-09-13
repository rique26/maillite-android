package com.rique.maillite.core.push

import com.rique.maillite.features.users.domain.usecase.UpdateFcmTokenUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ponto único que junta "pegar o token atual do Firebase" com "mandar pro backend" (RF08).
 * Fica em core/push (não em domain) porque depende do SDK do Firebase — não é regra de
 * negócio pura. Chamado depois de login bem-sucedido, quando a Splash confirma sessão
 * válida, e sempre que o Firebase rotaciona o token (onNewToken do Service).
 */
@Singleton
class FcmTokenSynchronizer @Inject constructor(
    private val fcmTokenProvider: FcmTokenProvider,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase
) {
    suspend fun sync() {
        val token = fcmTokenProvider.getCurrentToken() ?: return
        // Resultado ignorado de propósito: falha em registrar o token de push nunca deve
        // travar login/navegação — o app continua funcionando normalmente sem push.
        updateFcmTokenUseCase(token)
    }
}