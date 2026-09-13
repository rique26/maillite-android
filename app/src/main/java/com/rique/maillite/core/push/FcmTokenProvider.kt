package com.rique.maillite.core.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Busca o token FCM atual sob demanda. onNewToken() do FirebaseMessagingService só dispara
 * quando o token é criado ou rotacionado — não cobre o caso de "app reaberto com um token
 * que já existia antes", por isso esse ponto de leitura explícita também é necessário
 * (chamado após login e quando a Splash confirma sessão já válida).
 */
@Singleton
class FcmTokenProvider @Inject constructor() {

    suspend fun getCurrentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> continuation.resume(token) }
            .addOnFailureListener { continuation.resume(null) }
    }
}