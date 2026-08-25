package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.DepartureEntity
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.domain.repository.TourRepository
import com.mgacreative.touros.domain.usecase.CreateOrUpdateTourUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetToursUseCase
import com.mgacreative.touros.data.util.generateUuid
import com.mgacreative.touros.data.util.isValidUuid
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DepartureDraft(
    val id: String = "",
    val departureDate: String = "",
    val returnDate: String = "",
    val capacity: String = "30",
    val priceOverride: String = ""
)

data class ItineraryDraft(
    val id: String = "",
    val dayNumber: Int = 1,
    val title: String = "",
    val location: String = "",
    val description: String = ""
)

sealed interface TourFormUiState {
    data object Idle : TourFormUiState
    data object Loading : TourFormUiState
    data class Success(val message: String) : TourFormUiState
    data class Error(val message: String) : TourFormUiState
}

class TourFormViewModel(
    private val createOrUpdateTourUseCase: CreateOrUpdateTourUseCase,
    private val getToursUseCase: GetToursUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val tourRepository: TourRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourFormUiState>(TourFormUiState.Idle)
    val uiState: StateFlow<TourFormUiState> = _uiState.asStateFlow()

    private val _loadedTour = MutableStateFlow<Tour?>(null)
    val loadedTour: StateFlow<Tour?> = _loadedTour.asStateFlow()

    private val _departuresDrafts = MutableStateFlow<List<DepartureDraft>>(emptyList())
    val departuresDrafts: StateFlow<List<DepartureDraft>> = _departuresDrafts.asStateFlow()

    private val _itinerariesDrafts = MutableStateFlow<List<ItineraryDraft>>(emptyList())
    val itinerariesDrafts: StateFlow<List<ItineraryDraft>> = _itinerariesDrafts.asStateFlow()

    fun addDepartureDraft() {
        _departuresDrafts.value = _departuresDrafts.value + DepartureDraft(capacity = "30")
    }

    fun updateDepartureDraft(index: Int, draft: DepartureDraft) {
        val list = _departuresDrafts.value.toMutableList()
        if (index in list.indices) {
            list[index] = draft
            _departuresDrafts.value = list
        }
    }

    fun removeDepartureDraft(index: Int) {
        val list = _departuresDrafts.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _departuresDrafts.value = list
        }
    }

    fun addItineraryDraft() {
        val nextDay = _itinerariesDrafts.value.size + 1
        _itinerariesDrafts.value = _itinerariesDrafts.value + ItineraryDraft(dayNumber = nextDay)
    }

    fun updateItineraryDraft(index: Int, draft: ItineraryDraft) {
        val list = _itinerariesDrafts.value.toMutableList()
        if (index in list.indices) {
            list[index] = draft
            _itinerariesDrafts.value = list
        }
    }

    private val _deletedItineraryIds = mutableListOf<String>()

    fun removeItineraryDraft(index: Int) {
        val list = _itinerariesDrafts.value.toMutableList()
        if (index in list.indices) {
            val removed = list.removeAt(index)
            if (removed.id.isValidUuid()) {
                _deletedItineraryIds.add(removed.id)
            }
            _itinerariesDrafts.value = list.mapIndexed { idx, item -> item.copy(dayNumber = idx + 1) }
        }
    }

    fun loadTourForEdit(tourId: String?) {
        if (tourId == null) return
        viewModelScope.launch {
            _uiState.value = TourFormUiState.Loading
            tourRepository.getTourDetail(tourId)
                .onSuccess { detail ->
                    _loadedTour.value = detail.tour
                    _departuresDrafts.value = detail.departures.map {
                        DepartureDraft(
                            id = it.id,
                            departureDate = it.departureDate,
                            returnDate = it.returnDate ?: "",
                            capacity = (it.capacity ?: 30).toString(),
                            priceOverride = it.priceOverride?.toString() ?: ""
                        )
                    }
                    _itinerariesDrafts.value = detail.itineraries.mapIndexed { idx, it ->
                        ItineraryDraft(
                            id = it.id,
                            dayNumber = idx + 1,
                            title = it.title,
                            location = it.location ?: "",
                            description = it.description ?: ""
                        )
                    }
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
        adultCostPrice: Double = 0.0,
        childCostPrice06: Double = 0.0,
        childCostPrice712: Double = 0.0,
        capacity: Int,
        minParticipants: Int,
        maxParticipants: Int,
        description: String?,
        cancellationPolicy: String?,
        insuranceDetails: String?,
        includedServices: String? = null,
        excludedServices: String? = null,
        coverImageUrl: String? = null
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
                adultCostPrice = adultCostPrice,
                childCostPrice06 = childCostPrice06,
                childCostPrice712 = childCostPrice712,
                capacity = capacity,
                minParticipants = minParticipants,
                maxParticipants = maxParticipants,
                description = description?.trim()?.ifBlank { null },
                cancellationPolicy = cancellationPolicy?.trim()?.ifBlank { null },
                insuranceDetails = insuranceDetails?.trim()?.ifBlank { null },
                includedServices = includedServices?.trim()?.ifBlank { null },
                excludedServices = excludedServices?.trim()?.ifBlank { null },
                coverImageUrl = coverImageUrl?.trim()?.ifBlank { null },
                tenantId = tenantId
            )

            createOrUpdateTourUseCase(tour)
                .onSuccess { savedTour ->

                    // 1. Silinen Tur Programı Günlerini Veritabanından Sil
                    if (_deletedItineraryIds.isNotEmpty()) {
                        runCatching {
                            supabaseClient.postgrest.from("itineraries")
                                .delete {
                                    filter {
                                        isIn("id", _deletedItineraryIds)
                                    }
                                }
                        }
                        _deletedItineraryIds.clear()
                    }

                    // 2. Dolu Olan Gün Programlarını Kaydet
                    val validDrafts = _itinerariesDrafts.value.filter {
                        it.title.isNotBlank() || it.description.isNotBlank() || it.location.isNotBlank()
                    }

                    validDrafts.forEachIndexed { index, itinDraft ->
                        runCatching {
                            val itinEntity = com.mgacreative.touros.data.database.entity.ItineraryEntity(
                                id = if (itinDraft.id.isValidUuid()) itinDraft.id else generateUuid(),
                                tourId = savedTour.id,
                                dayNumber = index + 1,
                                title = itinDraft.title.trim().ifBlank { "${index + 1}. Gün Programı" },
                                location = itinDraft.location.trim().ifBlank { null },
                                description = itinDraft.description.trim().ifBlank { null },
                                sortOrder = index + 1,
                                tenantId = tenantId
                            )
                            supabaseClient.postgrest.from("itineraries").upsert(itinEntity)
                        }
                    }

                    _uiState.value = TourFormUiState.Success(
                        if (id.isBlank()) "Tur, kalkış tarihleri ve tur programı başarıyla kaydedildi" else "Tur bilgileri başarıyla güncellendi"
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
