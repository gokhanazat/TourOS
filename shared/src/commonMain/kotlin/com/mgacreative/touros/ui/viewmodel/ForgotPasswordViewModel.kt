package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ForgotPasswordUiState {
    data object Idle : ForgotPasswordUiState
    data object Loading : ForgotPasswordUiState
    data object EmailSent : ForgotPasswordUiState
    data object PasswordUpdated : ForgotPasswordUiState
    data class Error(val message: String) : ForgotPasswordUiState
}

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            forgotPasswordUseCase.sendResetEmail(email)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.EmailSent
                }
                .onFailure { exception ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        exception.message ?: "Şifre sıfırlama e-postası gönderilemedi"
                    )
                }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            forgotPasswordUseCase.updatePassword(newPassword)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.PasswordUpdated
                }
                .onFailure { exception ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        exception.message ?: "Şifre güncellenirken hata oluştu"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
}
