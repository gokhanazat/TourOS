package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.usecase.InviteUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InviteUserUiState {
    data object Idle : InviteUserUiState
    data object Loading : InviteUserUiState
    data class Success(val email: String, val role: UserRole) : InviteUserUiState
    data class Error(val message: String) : InviteUserUiState
}

class InviteUserViewModel(
    private val inviteUserUseCase: InviteUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InviteUserUiState>(InviteUserUiState.Idle)
    val uiState: StateFlow<InviteUserUiState> = _uiState.asStateFlow()

    fun inviteUser(email: String, role: UserRole, fullName: String) {
        viewModelScope.launch {
            _uiState.value = InviteUserUiState.Loading
            inviteUserUseCase(email, role, fullName)
                .onSuccess {
                    _uiState.value = InviteUserUiState.Success(email, role)
                }
                .onFailure { exception ->
                    _uiState.value = InviteUserUiState.Error(
                        exception.message ?: "Kullanıcı davet edilirken hata oluştu"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = InviteUserUiState.Idle
    }
}
