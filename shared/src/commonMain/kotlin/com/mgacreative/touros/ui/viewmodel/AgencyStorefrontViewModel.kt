package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyBrandingEntity
import com.mgacreative.touros.data.database.entity.AgencyStorefrontTourItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface AgencyStorefrontUiState {
    data object Loading : AgencyStorefrontUiState
    data class Success(
        val branding: AgencyBrandingEntity,
        val tours: List<AgencyStorefrontTourItem>
    ) : AgencyStorefrontUiState
    data class Error(val message: String) : AgencyStorefrontUiState
}

class AgencyStorefrontViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgencyStorefrontUiState>(AgencyStorefrontUiState.Loading)
    val uiState: StateFlow<AgencyStorefrontUiState> = _uiState.asStateFlow()

    init {
        loadStorefront()
    }

    fun loadStorefront(
        countryFilter: String = "",
        minNights: Int = 0,
        maxNights: Int = 30,
        maxBudget: Double = 100000.0
    ) {
        viewModelScope.launch {
            _uiState.value = AgencyStorefrontUiState.Loading
            runCatching {
                val brandingParams = buildJsonObject { put("p_agency_id", "00000000-0000-0000-0000-000000000001") }
                val branding = supabaseClient.postgrest.rpc("get_agency_branding", brandingParams)
                    .decodeSingleOrNull<AgencyBrandingEntity>() ?: AgencyBrandingEntity()

                val searchParams = buildJsonObject {
                    put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                    put("p_country", countryFilter)
                    put("p_min_nights", minNights)
                    put("p_max_nights", maxNights)
                    put("p_max_budget", maxBudget)
                }
                val tours = supabaseClient.postgrest.rpc("search_agency_storefront_tours", searchParams)
                    .decodeList<AgencyStorefrontTourItem>()

                AgencyStorefrontUiState.Success(branding = branding, tours = tours)
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure {
                // Mock Travelata.ru style aggregated fallback data
                _uiState.value = AgencyStorefrontUiState.Success(
                    branding = AgencyBrandingEntity(
                        heroTitle = "Ege & Akdeniz Tatil Fırsatları",
                        heroSubtitle = "5 Farklı Tur Operatöründen Karşılaştırmalı Fiyat Garantisi"
                    ),
                    tours = listOf(
                        AgencyStorefrontTourItem(
                            tourId = "t-001",
                            title = "Kapadokya Balon & Vadi Turu (3 Gece 4 Gün)",
                            code = "ANK-00001",
                            country = "Türkiye",
                            city = "Nevşehir",
                            nights = 3,
                            basePrice = 2000.0,
                            finalPrice = 2300.0,
                            operatorName = "Ankara Turizm A.Ş.",
                            comparedOperatorCount = 3
                        ),
                        AgencyStorefrontTourItem(
                            tourId = "t-002",
                            title = "Mavi Yolculuk Göcek Koyu & Fethiye (5 Gece)",
                            code = "IST-00045",
                            country = "Türkiye",
                            city = "Muğla",
                            nights = 5,
                            basePrice = 4500.0,
                            finalPrice = 5175.0,
                            operatorName = "Ege Maritim Ltd.",
                            comparedOperatorCount = 4
                        )
                    )
                )
            }
        }
    }
}
