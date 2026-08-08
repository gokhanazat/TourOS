package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.LoginUseCase
import com.mgacreative.touros.domain.usecase.LogoutUseCase
import com.mgacreative.touros.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUserState: StateFlow<User?> = getCurrentUserUseCase.observe()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { exception ->
                    val userFriendlyMsg = mapAuthErrorMessage(exception.message)
                    _uiState.value = AuthUiState.Error(userFriendlyMsg)
                }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(email, password, fullName)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { exception ->
                    val userFriendlyMsg = mapAuthErrorMessage(exception.message)
                    _uiState.value = AuthUiState.Error(userFriendlyMsg)
                }
        }
    }

    private fun mapAuthErrorMessage(rawMessage: String?): String {
        if (rawMessage.isNullOrBlank()) {
            return "Giriş yapılırken beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."
        }
        val msg = rawMessage.lowercase()
        return when {
            msg.contains("invalid_credentials") || msg.contains("invalid login credentials") || msg.contains("grant_type=password") -> {
                "E-posta adresi veya şifre hatalı. Lütfen bilgilerinizi kontrol edip tekrar deneyin."
            }
            msg.contains("email_not_confirmed") || msg.contains("email not confirmed") -> {
                "E-posta adresiniz henüz doğrulanmamış. Lütfen gelen kutunuzu doğrulayın."
            }
            msg.contains("user_already_exists") || msg.contains("user already registered") -> {
                "Bu e-posta adresiyle zaten kayıtlı bir hesap mevcut."
            }
            msg.contains("too_many_requests") || msg.contains("rate limit") -> {
                "Çok fazla hatalı giriş denemesi yapıldı. Lütfen kısa bir süre sonra tekrar deneyin."
            }
            msg.contains("network") || msg.contains("timeout") || msg.contains("connection") -> {
                "İnternet bağlantısı kurulamadı. Lütfen ağ bağlantınızı kontrol edin."
            }
            else -> {
                "E-posta adresi veya şifre hatalı. Lütfen bilgilerinizi kontrol edin."
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
