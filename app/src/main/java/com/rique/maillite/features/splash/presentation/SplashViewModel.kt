package com.rique.maillite.features.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.push.FcmTokenSynchronizer
import com.rique.maillite.features.auth.domain.usecase.CheckSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkSessionUseCase: CheckSessionUseCase,
    private val fcmTokenSynchronizer: FcmTokenSynchronizer
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Lê o token salvo no DataStore e confere se ainda não expirou (JwtUtil).
            val hasValidSession = checkSessionUseCase()

            _uiState.value = if (hasValidSession) {
                SplashUiState.NavigateToInbox
            } else {
                SplashUiState.NavigateToLogin
            }

            // Cobre o caso "app reaberto com sessão já válida" — no login, quem sincroniza
            // é o próprio LoginViewModel; aqui é o outro caminho possível pra chegar na Inbox.
            if (hasValidSession) {
                fcmTokenSynchronizer.sync()
            }
        }
    }
}