package com.rique.maillite.features.splash.presentation

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data object NavigateToInbox : SplashUiState()
}
