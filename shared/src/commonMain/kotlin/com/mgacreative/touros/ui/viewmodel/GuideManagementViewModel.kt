package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.repository.GuideRepository
import com.mgacreative.touros.domain.usecase.CreateGuideUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetGuidesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GuideUiState {
    data object Loading : GuideUiState
    data class Success(
        val guides: List<Guide> = emptyList(),
        val searchQuery: String = "",
        val selectedLanguageFilter: String? = null
    ) : GuideUiState
    data class Error(val message: String) : GuideUiState
}

data class GuideFormState(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val licenseNumber: String = "", // Kokart / Lisans No
    val languagesCsv: String = "Türkçe, İngilizce",
    val specialization: String = "Kültür Turları",
    val tcNo: String = "",
    val rating: String = "5.0",
    val totalToursCompleted: String = "0",
    val notes: String = "",
    val isActive: Boolean = true,
    val isFormOpen: Boolean = false,
    val isEditing: Boolean = false
)

class GuideManagementViewModel(
    private val getGuidesUseCase: GetGuidesUseCase,
    private val createGuideUseCase: CreateGuideUseCase,
    private val guideRepository: GuideRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuideUiState>(GuideUiState.Loading)
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(GuideFormState())
    val formState: StateFlow<GuideFormState> = _formState.asStateFlow()

    init {
        loadGuides()
    }

    fun loadGuides(query: String = "", languageFilter: String? = null) {
        viewModelScope.launch {
            _uiState.value = GuideUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getGuidesUseCase(tenantId)
            res.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        Guide(
                            id = "g1",
                            fullName = "Zeynep Arslan",
                            phone = "0532 100 2030",
                            email = "zeynep@touros.com",
                            licenseNumber = "K-12345",
                            languages = listOf("Türkçe", "İngilizce", "Almanca"),
                            specialization = "Kapadokya & Kültür Turları",
                            rating = 4.9,
                            totalToursCompleted = 48,
                            notes = "TUREB lisanslı A Grabı Profesyonel Ülke Rehberi.",
                            tenantId = tenantId
                        ),
                        Guide(
                            id = "g2",
                            fullName = "Murat Celal",
                            phone = "0542 220 3040",
                            email = "murat@touros.com",
                            licenseNumber = "K-67890",
                            languages = listOf("Türkçe", "Fransızca", "İspanyolca"),
                            specialization = "Doğa & Trekking Turları",
                            rating = 4.8,
                            totalToursCompleted = 32,
                            notes = "Ege ve Doğu Karadeniz rotalarında uzman.",
                            tenantId = tenantId
                        ),
                        Guide(
                            id = "g3",
                            fullName = "Canan Öztürk",
                            phone = "0505 330 4050",
                            email = "canan@touros.com",
                            licenseNumber = "K-11223",
                            languages = listOf("Türkçe", "İtalyanca", "İngilizce"),
                            specialization = "Gastronomi & Sanat Turları",
                            rating = 5.0,
                            totalToursCompleted = 65,
                            notes = "VIP gruplar ve gurme turlar için tercih edilen rehber.",
                            tenantId = tenantId
                        )
                    )
                } else list

                var filtered = fallbackList
                if (query.isNotBlank()) {
                    filtered = filtered.filter { it.fullName.contains(query, ignoreCase = true) || it.licenseNumber?.contains(query, ignoreCase = true) == true }
                }
                if (languageFilter != null) {
                    filtered = filtered.filter { it.languages?.contains(languageFilter) == true }
                }

                _uiState.value = GuideUiState.Success(
                    guides = filtered,
                    searchQuery = query,
                    selectedLanguageFilter = languageFilter
                )
            }.onFailure { err ->
                _uiState.value = GuideUiState.Error(err.message ?: "Rehber listesi çekilemedi.")
            }
        }
    }

    fun openNewForm() {
        _formState.value = GuideFormState(isFormOpen = true, isEditing = false)
    }

    fun openEditForm(guide: Guide) {
        _formState.value = GuideFormState(
            id = guide.id,
            fullName = guide.fullName,
            phone = guide.phone ?: "",
            email = guide.email ?: "",
            licenseNumber = guide.licenseNumber ?: "",
            languagesCsv = guide.languages?.joinToString(", ") ?: "Türkçe",
            specialization = guide.specialization ?: "",
            tcNo = guide.tcNo ?: "",
            rating = guide.rating.toString(),
            totalToursCompleted = guide.totalToursCompleted.toString(),
            notes = guide.notes ?: "",
            isActive = guide.isActive,
            isFormOpen = true,
            isEditing = true
        )
    }

    fun closeForm() {
        _formState.value = GuideFormState(isFormOpen = false)
    }

    fun updateFullName(v: String) { _formState.value = _formState.value.copy(fullName = v) }
    fun updatePhone(v: String) { _formState.value = _formState.value.copy(phone = v) }
    fun updateEmail(v: String) { _formState.value = _formState.value.copy(email = v) }
    fun updateLicenseNumber(v: String) { _formState.value = _formState.value.copy(licenseNumber = v) }
    fun updateLanguagesCsv(v: String) { _formState.value = _formState.value.copy(languagesCsv = v) }
    fun updateSpecialization(v: String) { _formState.value = _formState.value.copy(specialization = v) }
    fun updateRating(v: String) { _formState.value = _formState.value.copy(rating = v) }
    fun updateTotalTours(v: String) { _formState.value = _formState.value.copy(totalToursCompleted = v) }
    fun updateNotes(v: String) { _formState.value = _formState.value.copy(notes = v) }
    fun updateIsActive(v: Boolean) { _formState.value = _formState.value.copy(isActive = v) }

    fun saveGuide() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val state = _formState.value

            val langList = state.languagesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }

            val guide = Guide(
                id = state.id,
                fullName = state.fullName,
                phone = state.phone.ifBlank { null },
                email = state.email.ifBlank { null },
                licenseNumber = state.licenseNumber.ifBlank { null },
                languages = langList,
                specialization = state.specialization.ifBlank { null },
                rating = state.rating.toDoubleOrNull() ?: 5.0,
                totalToursCompleted = state.totalToursCompleted.toIntOrNull() ?: 0,
                notes = state.notes.ifBlank { null },
                isActive = state.isActive,
                tenantId = tenantId
            )

            val res = createGuideUseCase(guide)
            res.onSuccess {
                closeForm()
                loadGuides()
            }.onFailure { err ->
                _uiState.value = GuideUiState.Error(err.message ?: "Rehber kaydedilemedi.")
            }
        }
    }

    fun deleteGuide(guideId: String) {
        viewModelScope.launch {
            guideRepository.deleteGuide(guideId)
            loadGuides()
        }
    }
}
