package com.rique.maillite.core.util

import com.google.gson.Gson
import java.util.Base64

object JwtUtil {

    private data class JwtPayload(val exp: Long? = null)

    fun isExpired(token: String): Boolean {
        val payloadJson = decodePayload(token) ?: return true

        return try {
            val payload = Gson().fromJson(payloadJson, JwtPayload::class.java)
            val expirationMillis = (payload.exp ?: return true) * 1000
            System.currentTimeMillis() >= expirationMillis
        } catch (e: Exception) {
            true
        }
    }

    private fun decodePayload(token: String): String? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        return try {
            val payload = parts[1]
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val bytes = Base64.getUrlDecoder().decode(padded)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}