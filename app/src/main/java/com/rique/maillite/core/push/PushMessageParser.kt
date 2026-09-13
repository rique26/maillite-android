package com.rique.maillite.core.push

import javax.inject.Inject

/**
 * Resolve o título e o corpo exibidos na notificação local a partir do payload recebido do
 * FCM. Extraído do FirebaseMessagingService para ser testável sem depender do Android
 * Framework (RemoteMessage/Context).
 */
class PushMessageParser @Inject constructor() {

    fun parse(title: String?, body: String?, defaultTitle: String): PushContent =
        PushContent(
            title = title ?: defaultTitle,
            body = body.orEmpty()
        )
}

data class PushContent(val title: String, val body: String)