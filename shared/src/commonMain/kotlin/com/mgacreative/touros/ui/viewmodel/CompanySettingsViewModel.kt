package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CompanySeason
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.usecase.GetCompanySettingsUseCase
import com.mgacreative.touros.domain.usecase.UpdateCompanySettingsUseCase
import com.mgacreative.touros.domain.usecase.UploadCompanyLogoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CompanySettingsUiState {
    data object Idle : CompanySettingsUiState
    data object Loading : CompanySettingsUiState
    data class Success(val settings: CompanySettings) : CompanySettingsUiState
    data class Error(val message: String) : CompanySettingsUiState
    data object Saving : CompanySettingsUiState
}

class CompanySettingsViewModel(
    private val getCompanySettingsUseCase: GetCompanySettingsUseCase,
    private val updateCompanySettingsUseCase: UpdateCompanySettingsUseCase,
    private val uploadCompanyLogoUseCase: UploadCompanyLogoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompanySettingsUiState>(CompanySettingsUiState.Idle)
    val uiState: StateFlow<CompanySettingsUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun loadSettings(companyId: String) {
        viewModelScope.launch {
            _uiState.value = CompanySettingsUiState.Loading
            getCompanySettingsUseCase(companyId)
                .onSuccess { settings ->
                    _uiState.value = CompanySettingsUiState.Success(settings)
                }
                .onFailure { exception ->
                    _uiState.value = CompanySettingsUiState.Error(
                        exception.message ?: "Şirket ayarları yüklenemedi"
                    )
                }
        }
    }

    fun saveSettings(settings: CompanySettings) {
        viewModelScope.launch {
            _uiState.value = CompanySettingsUiState.Saving
            updateCompanySettingsUseCase(settings)
                .onSuccess { updated ->
                    _uiState.value = CompanySettingsUiState.Success(updated)
                    _userMessage.value = "Ayarlar başarıyla kaydedildi"
                }
                .onFailure { exception ->
                    _uiState.value = CompanySettingsUiState.Error(
                        exception.message ?: "Ayarlar kaydedilirken hata oluştu"
                    )
                }
        }
    }

    fun uploadLogo(companyId: String, fileBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is CompanySettingsUiState.Success) {
                _uiState.value = CompanySettingsUiState.Saving
                uploadCompanyLogoUseCase(companyId, fileBytes, fileName)
                    .onSuccess { logoUrl ->
                        val updated = currentState.settings.copy(logoUrl = logoUrl)
                        _uiState.value = CompanySettingsUiState.Success(updated)
                        _userMessage.value = "Logo başarıyla yüklendi"
                    }
                    .onFailure { exception ->
                        _uiState.value = CompanySettingsUiState.Error(
                            exception.message ?: "Logo yüklenirken hata oluştu"
                        )
                    }
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
