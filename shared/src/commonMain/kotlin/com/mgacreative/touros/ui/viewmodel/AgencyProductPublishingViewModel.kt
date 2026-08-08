package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyPublishedTourEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface AgencyProductPublishingUiState {
    data object Loading : AgencyProductPublishingUiState
    data class Success(val tours: List<AgencyPublishedTourEntity>) : AgencyProductPublishingUiState
    data class Error(val message: String) : AgencyProductPublishingUiState
}

class AgencyProductPublishingViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgencyProductPublishingUiState>(AgencyProductPublishingUiState.Loading)
    val uiState: StateFlow<AgencyProductPublishingUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = AgencyProductPublishingUiState.Loading
            runCatching {
                val params = buildJsonObject {
                    put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                }
                supabaseClient.postgrest.rpc("get_agency_catalog_tours", params)
                    .decodeList<AgencyPublishedTourEntity>()
            }.onSuccess { list ->
                _uiState.value = AgencyProductPublishingUiState.Success(list)
            }.onFailure {
                // Fallback mock catalog for testing
                _uiState.value = AgencyProductPublishingUiState.Success(
                    listOf(
                        AgencyPublishedTourEntity(
                            id = "pub-01",
                            tourId = "t-001",
                            tourTitle = "Kapadokya Balon & Vadi Turu",
                            tourCode = "ANK-00001",
                            operatorName = "Ankara Turizm A.Ş.",
                            basePrice = 2000.0,
                            calculatedPrice = 2300.0,
                            isPublished = true
                        ),
                        AgencyPublishedTourEntity(
                            id = "pub-02",
                            tourId = "t-002",
                            tourTitle = "İstanbul Boğaz & Tarih Turu",
                            tourCode = "IST-00012",
                            operatorName = "İstanbul Travel Ltd.",
                            basePrice = 1500.0,
                            calculatedPrice = 1725.0,
                            isPublished = false
                        )
                    )
                )
            }
        }
    }

    fun togglePublishStatus(tourId: String, newPublishedState: Boolean, customPrice: Double? = null) {
        viewModelScope.launch {
            val currentList = (uiState.value as? AgencyProductPublishingUiState.Success)?.tours ?: return@launch
            val updated = currentList.map { item ->
                if (item.tourId == tourId) {
                    item.copy(isPublished = newPublishedState, customPriceOverride = customPrice)
                } else item
            }
            _uiState.value = AgencyProductPublishingUiState.Success(updated)

            runCatching {
                val params = buildJsonObject {
                    put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                    put("p_tour_id", tourId)
                    put("p_is_published", newPublishedState)
                    customPrice?.let { put("p_custom_price", it) }
                }
                supabaseClient.postgrest.rpc("toggle_agency_tour_publishing", params)
            }
        }
    }
}
