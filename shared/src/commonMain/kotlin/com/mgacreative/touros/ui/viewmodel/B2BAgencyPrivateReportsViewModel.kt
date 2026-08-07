package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2BAgencyPrivateReport
import com.mgacreative.touros.domain.usecase.GetB2BAgencyPrivateReportsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2BAgencyPrivateReportsUiState(
    val report: B2BAgencyPrivateReport = B2BAgencyPrivateReport(),
    val isLoading: Boolean = false,
    val rlsProtected: Boolean = true,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2BAgencyPrivateReportsViewModel(
    private val getB2BAgencyPrivateReportsUseCase: GetB2BAgencyPrivateReportsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2BAgencyPrivateReportsUiState())
    val uiState: StateFlow<B2BAgencyPrivateReportsUiState> = _uiState.asStateFlow()

    init {
        loadPrivateReport()
    }

    fun loadPrivateReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2BAgencyPrivateReportsUseCase(tenantId)
            res.onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    report = data,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }
}
