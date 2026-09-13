package com.rique.maillite.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.core.push.FcmTokenSynchronizer
import com.rique.maillite.features.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val fcmTokenSynchronizer: FcmTokenSynchronizer
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Validação de campo em branco já é feita dentro do LoginUseCase
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success
                    // Fire-and-forget: registrar o token de push (RF08) nunca deve atrasar
                    // ou bloquear a navegação pós-login.
                    fcmTokenSynchronizer.sync()
                }
                is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }
}