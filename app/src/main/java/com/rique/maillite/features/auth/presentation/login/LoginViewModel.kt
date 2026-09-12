package com.rique.maillite.features.auth.presentation.login

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
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Preencha e-mail e senha")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            // TODO: substituir pela chamada real ao AuthRepository/LoginUseCase quando a
            // camada de Data existir (POST /v1/auth/login). Por enquanto, fake fixo.
            delay(LOGIN_DELAY_MS)

            val loginSucceeded = true

            _uiState.value = if (loginSucceeded) {
                LoginUiState.Success
            } else {
                LoginUiState.Error("E-mail ou senha inválidos")
            }
        }
    }

    private companion object {
        const val LOGIN_DELAY_MS = 800L
    }
}
