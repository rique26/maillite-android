package com.rique.maillite.features.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // TODO: quando a camada de Data existir, trocar por uma leitura real do token
            // salvo no DataStore (ex: authRepository.hasValidToken()). Por enquanto, fake fixo
            // simulando "sem sessão salva" — troque para true pra simular sessão ativa.
            delay(SPLASH_DELAY_MS)

            val hasValidToken = false

            _uiState.value = if (hasValidToken) {
                SplashUiState.NavigateToInbox
            } else {
                SplashUiState.NavigateToLogin
            }
        }
    }

    private companion object {
        const val SPLASH_DELAY_MS = 800L
    }
}
