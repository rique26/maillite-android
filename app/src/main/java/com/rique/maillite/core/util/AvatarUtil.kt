package com.rique.maillite.core.util

import com.rique.maillite.R

/**
 * Gera as iniciais e a cor de fundo de um avatar de identidade (badge circular) a partir
 * de um nome. A cor é determinística (mesmo nome sempre gera a mesma cor), calculada via
 * hash do nome sobre uma paleta fixa — não depende do tema (funciona igual em light/dark,
 * já que o fundo do avatar é opaco).
 */
object AvatarUtil {

    private val PALETTE = intArrayOf(
        R.color.avatar_palette_1,
        R.color.avatar_palette_2,
        R.color.avatar_palette_3,
        R.color.avatar_palette_4,
        R.color.avatar_palette_5,
        R.color.avatar_palette_6
    )

    fun initialsOf(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts.first().take(1).uppercase()
            else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
        }
    }

    fun colorResFor(name: String): Int {
        val index = kotlin.math.abs(name.hashCode()) % PALETTE.size
        return PALETTE[index]
    }
}
