package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideRecommendation
import com.mgacreative.touros.domain.usecase.AssignGuideToDepartureUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetRecommendedGuidesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DepartureInfo(
    val id: String = "dep-101",
    val tourTitle: String = "Kapadokya Balon & Vadi Turu",
    val departureDate: String = "2026-08-10",
    val requiredLanguage: String = "İngilizce",
    val bookedPax: Int = 24,
    val capacity: Int = 30,
    val currentGuideName: String? = null
)

sealed interface GuideAssignmentUiState {
    data object Loading : GuideAssignmentUiState
    data class Success(
        val departure: DepartureInfo = DepartureInfo(),
        val recommendations: List<GuideRecommendation> = emptyList(),
        val selectedLanguage: String? = "İngilizce",
        val onlyAvailableFilter: Boolean = false,
        val assignedSuccessMessage: String? = null
    ) : GuideAssignmentUiState
    data class Error(val message: String) : GuideAssignmentUiState
}

class GuideAssignmentViewModel(
    private val getRecommendedGuidesUseCase: GetRecommendedGuidesUseCase,
    private val assignGuideToDepartureUseCase: AssignGuideToDepartureUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuideAssignmentUiState>(GuideAssignmentUiState.Loading)
    val uiState: StateFlow<GuideAssignmentUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations(languageFilter: String? = "İngilizce", onlyAvailable: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = GuideAssignmentUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val departure = DepartureInfo(
                id = "dep-101",
                tourTitle = "Kapadokya Balon & Vadi Turu",
                departureDate = "2026-08-10",
                requiredLanguage = languageFilter ?: "İngilizce",
                bookedPax = 24,
                capacity = 30
            )

            val res = getRecommendedGuidesUseCase(tenantId, languageFilter, onlyAvailable)
            res.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        GuideRecommendation(
                            guide = Guide(
                                id = "g1",
                                fullName = "Zeynep Arslan",
                                phone = "0532 100 2030",
                                email = "zeynep@touros.com",
                                licenseNumber = "K-12345",
                                languages = listOf("Türkçe", "İngilizce", "Almanca"),
                                specialization = "Kapadokya & Kültür Turları",
                                rating = 4.9,
                                totalToursCompleted = 48
                            ),
                            isAvailable = true,
                            matchScore = 98,
                            languageMatch = true,
                            recommendationReason = "🌐 Tur Dili (İngilizce) Birebir Eşleşiyor & ⭐ 4.9 Puan"
                        ),
                        GuideRecommendation(
                            guide = Guide(
                                id = "g3",
                                fullName = "Canan Öztürk",
                                phone = "0505 330 4050",
                                email = "canan@touros.com",
                                licenseNumber = "K-11223",
                                languages = listOf("Türkçe", "İtalyanca", "İngilizce"),
                                specialization = "Gastronomi & Sanat Turları",
                                rating = 5.0,
                                totalToursCompleted = 65
                            ),
                            isAvailable = true,
                            matchScore = 95,
                            languageMatch = true,
                            recommendationReason = "🌐 İngilizce Biliyo & ⭐ 5.0 En Yüksek Puanlı Rehber"
                        ),
                        GuideRecommendation(
                            guide = Guide(
                                id = "g2",
                                fullName = "Murat Celal",
                                phone = "0542 220 3040",
                                email = "murat@touros.com",
                                licenseNumber = "K-67890",
                                languages = listOf("Türkçe", "Fransızca", "İspanyolca"),
                                specialization = "Doğa & Trekking Turları",
                                rating = 4.8,
                                totalToursCompleted = 32
                            ),
                            isAvailable = false,
                            matchScore = 60,
                            languageMatch = false,
                            recommendationReason = "⚠️ Tur Dili Farklı & Aynı Tarihte Başka Turda Görevde"
                        )
                    )
                } else list

                _uiState.value = GuideAssignmentUiState.Success(
                    departure = departure,
                    recommendations = fallbackList,
                    selectedLanguage = languageFilter,
                    onlyAvailableFilter = onlyAvailable
                )
            }.onFailure { err ->
                _uiState.value = GuideAssignmentUiState.Error(err.message ?: "Rehber önerileri getirilemedi.")
            }
        }
    }

    fun setLanguageFilter(lang: String?) {
        val currentState = _uiState.value as? GuideAssignmentUiState.Success
        loadRecommendations(lang, currentState?.onlyAvailableFilter ?: false)
    }

    fun toggleOnlyAvailable(onlyAvailable: Boolean) {
        val currentState = _uiState.value as? GuideAssignmentUiState.Success
        loadRecommendations(currentState?.selectedLanguage, onlyAvailable)
    }

    fun assignGuide(guide: Guide) {
        viewModelScope.launch {
            val currentState = _uiState.value as? GuideAssignmentUiState.Success ?: return@launch
            val res = assignGuideToDepartureUseCase(currentState.departure.id, guide.id)
            res.onSuccess {
                _uiState.value = currentState.copy(
                    departure = currentState.departure.copy(currentGuideName = guide.fullName),
                    assignedSuccessMessage = "✅ ${guide.fullName} başarıyla ${currentState.departure.tourTitle} kalkışına atandı!"
                )
            }.onFailure { err ->
                _uiState.value = GuideAssignmentUiState.Error(err.message ?: "Atama başarısız.")
            }
        }
    }
}
