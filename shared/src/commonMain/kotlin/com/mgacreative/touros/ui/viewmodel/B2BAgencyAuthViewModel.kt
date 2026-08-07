package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2BAgencyProfile
import com.mgacreative.touros.domain.usecase.GetB2BAgencyCurrentAccountUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2BAgencyAuthUiState(
    val isAuthenticated: Boolean = false,
    val agencyProfile: B2BAgencyProfile? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2BAgencyAuthViewModel(
    private val getB2BAgencyCurrentAccountUseCase: GetB2BAgencyCurrentAccountUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2BAgencyAuthUiState())
    val uiState: StateFlow<B2BAgencyAuthUiState> = _uiState.asStateFlow()

    fun loginB2BAgency(agencyCode: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2BAgencyCurrentAccountUseCase(tenantId)
            res.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    agencyProfile = profile,
                    notificationMessage = "✅ B2B Girişi Başarılı. Hoş geldiniz, ${profile.agencyName}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "B2B Acente girişi başarısız."
                )
            }
        }
    }

    fun logout() {
        _uiState.value = B2BAgencyAuthUiState()
    }
}
