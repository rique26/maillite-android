package com.rique.maillite.features.auth.presentation.register

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
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Preencha todos os campos")
            return
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.value = RegisterUiState.Error("A senha deve ter no mínimo $MIN_PASSWORD_LENGTH caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            // TODO: substituir pela chamada real ao AuthRepository/RegisterUseCase quando a
            // camada de Data existir (POST /v1/auth/register). Por enquanto, fake fixo.
            delay(REGISTER_DELAY_MS)

            val registerSucceeded = true

            _uiState.value = if (registerSucceeded) {
                RegisterUiState.Success
            } else {
                RegisterUiState.Error("Não foi possível concluir o cadastro")
            }
        }
    }

    private companion object {
        const val REGISTER_DELAY_MS = 800L
        const val MIN_PASSWORD_LENGTH = 6
    }
}
