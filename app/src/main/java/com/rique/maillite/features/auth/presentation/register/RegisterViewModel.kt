package com.rique.maillite.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rique.maillite.core.domain.util.Result
import com.rique.maillite.features.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        // Validações de campo em branco e tamanho mínimo de senha já ficam no RegisterUseCase.
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            when (val result = registerUseCase(name, email, password)) {
                is Result.Success -> _uiState.value = RegisterUiState.Success
                is Result.Error -> _uiState.value = RegisterUiState.Error(result.message)
            }
        }
    }
}