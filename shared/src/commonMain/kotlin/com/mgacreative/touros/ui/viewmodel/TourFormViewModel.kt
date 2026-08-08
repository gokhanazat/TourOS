package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.domain.usecase.CreateOrUpdateTourUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetToursUseCase
import com.mgacreative.touros.data.util.isValidUuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TourFormUiState {
    data object Idle : TourFormUiState
    data object Loading : TourFormUiState
    data class Success(val message: String) : TourFormUiState
    data class Error(val message: String) : TourFormUiState
}

class TourFormViewModel(
    private val createOrUpdateTourUseCase: CreateOrUpdateTourUseCase,
    private val getToursUseCase: GetToursUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourFormUiState>(TourFormUiState.Idle)
    val uiState: StateFlow<TourFormUiState> = _uiState.asStateFlow()

    private val _loadedTour = MutableStateFlow<Tour?>(null)
    val loadedTour: StateFlow<Tour?> = _loadedTour.asStateFlow()

    fun loadTourForEdit(tourId: String?) {
        if (tourId == null) return
        viewModelScope.launch {
            _uiState.value = TourFormUiState.Loading
            getToursUseCase.getTourById(tourId)
                .onSuccess { tour ->
                    _loadedTour.value = tour
                    _uiState.value = TourFormUiState.Idle
                }
                .onFailure { exception ->
                    _uiState.value = TourFormUiState.Error(
                        exception.message ?: "Tur bilgileri yüklenemedi"
                    )
                }
        }
    }

    fun saveTour(
        id: String = "",
        code: String,
        title: String,
        category: TourCategory,
        country: String,
        city: String,
        durationDays: Int,
        basePrice: Double = 0.0,
        childPrice06: Double = 0.0,
        childPrice712: Double = 0.0,
        capacity: Int,
        minParticipants: Int,
        maxParticipants: Int,
        description: String?,
        cancellationPolicy: String?,
        insuranceDetails: String?
    ) {
        viewModelScope.launch {
            _uiState.value = TourFormUiState.Loading

            val currentUser = getCurrentUserUseCase()
            val tenantId = currentUser?.tenantId?.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"

            val tour = Tour(
                id = id,
                code = code.trim(),
                title = title.trim(),
                category = category,
                country = country.trim(),
                city = city.trim(),
                durationDays = durationDays,
                basePrice = basePrice,
                childPrice06 = childPrice06,
                childPrice712 = childPrice712,
                capacity = capacity,
                minParticipants = minParticipants,
                maxParticipants = maxParticipants,
                description = description?.trim()?.ifBlank { null },
                cancellationPolicy = cancellationPolicy?.trim()?.ifBlank { null },
                insuranceDetails = insuranceDetails?.trim()?.ifBlank { null },
                tenantId = tenantId
            )

            createOrUpdateTourUseCase(tour)
                .onSuccess {
                    _uiState.value = TourFormUiState.Success(
                        if (id.isBlank()) "Tur başarıyla kaydedildi" else "Tur başarıyla güncellendi"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = TourFormUiState.Error(
                        exception.message ?: "Tur kaydedilirken hata oluştu"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = TourFormUiState.Idle
    }
}
