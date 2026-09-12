package com.rique.maillite.core.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Aplica insets da Status Bar no topo da [topView]
 * e insets da Navigation Bar no rodapé da [bottomView].
 */
fun applyWindowInsets(
    rootView: View,
    topView: View? = null,
    bottomView: View? = null
) {
    val topInitialMarginTop =
        (topView?.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    val bottomInitialMarginBottom =
        (bottomView?.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
        val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

        (topView?.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            params.topMargin = topInitialMarginTop + statusBar.top
            topView.layoutParams = params
        }

        (bottomView?.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            params.bottomMargin = bottomInitialMarginBottom + navigationBar.bottom
            bottomView.layoutParams = params
        }

        insets
    }

    if (rootView.isAttachedToWindow) {
        ViewCompat.requestApplyInsets(rootView)
    } else {
        rootView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }
            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}