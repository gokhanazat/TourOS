package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.AppLanguageItem
import com.mgacreative.touros.domain.usecase.GetAppTranslationsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetSupportedLanguagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MultiLanguageUiState(
    val supportedLanguages: List<AppLanguageItem> = emptyList(),
    val selectedLanguage: AppLanguageItem = AppLanguageItem("tr", "Türkçe", false, "🇹🇷"),
    val translations: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class MultiLanguageViewModel(
    private val getSupportedLanguagesUseCase: GetSupportedLanguagesUseCase,
    private val getAppTranslationsUseCase: GetAppTranslationsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiLanguageUiState())
    val uiState: StateFlow<MultiLanguageUiState> = _uiState.asStateFlow()

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = getSupportedLanguagesUseCase()
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    supportedLanguages = list,
                    isLoading = false
                )
                selectLanguage(list.firstOrNull() ?: AppLanguageItem())
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun selectLanguage(language: AppLanguageItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedLanguage = language, isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getAppTranslationsUseCase(language.code, tenantId)
            res.onSuccess { map ->
                _uiState.value = _uiState.value.copy(
                    translations = map,
                    isLoading = false,
                    notificationMessage = if (language.isRtl) "🇸🇦 Arapça Sağdan Sola (RTL) Düzeni Aktif Edildi!" else "${language.flagEmoji} ${language.name} Dili Aktif."
                )
            }
        }
    }
}
