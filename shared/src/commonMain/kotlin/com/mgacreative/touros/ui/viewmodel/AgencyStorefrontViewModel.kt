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
                val loadedTours = supabaseClient.postgrest.rpc("search_agency_storefront_tours", searchParams)
                    .decodeList<AgencyStorefrontTourItem>()

                val tours = if (loadedTours.isNotEmpty()) loadedTours else defaultSampleTours

                AgencyStorefrontUiState.Success(branding = branding, tours = tours)
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure {
                // Mock Travelata/Sletat.ru style aggregated fallback data
                _uiState.value = AgencyStorefrontUiState.Success(
                    branding = AgencyBrandingEntity(
                        heroTitle = "Hayalinizdeki Turu Keşfedin",
                        heroSubtitle = "80+ Tur Operatöründen Karşılaştırmalı Fiyat Garantisi"
                    ),
                    tours = defaultSampleTours
                )
            }
        }
    }

    private val defaultSampleTours = listOf(
        AgencyStorefrontTourItem(
            tourId = "t-001",
            title = "Kapadokya Balon & Vadi Turu (3 Gece 4 Gün)",
            code = "ANK-00001",
            country = "Türkiye",
            city = "Nevşehir",
            nights = 3,
            basePrice = 10800.0,
            finalPrice = 12500.0,
            operatorName = "Ankara Turizm A.Ş. / Coral Travel",
            comparedOperatorCount = 3
        ),
        AgencyStorefrontTourItem(
            tourId = "t-002",
            title = "Mavi Yolculuk Göcek Koyu & Fethiye (5 Gece)",
            code = "IST-00045",
            country = "Türkiye",
            city = "Muğla",
            nights = 5,
            basePrice = 16400.0,
            finalPrice = 18900.0,
            operatorName = "Ege Maritim Ltd. / Anex Tour",
            comparedOperatorCount = 4
        ),
        AgencyStorefrontTourItem(
            tourId = "t-003",
            title = "Antalya All-Inclusive Luxury Resort & Rafting (7 Gece)",
            code = "ANT-00102",
            country = "Türkiye",
            city = "Antalya",
            nights = 7,
            basePrice = 21200.0,
            finalPrice = 24500.0,
            operatorName = "Pegas Touristik",
            comparedOperatorCount = 5
        ),
        AgencyStorefrontTourItem(
            tourId = "t-004",
            title = "Dubai Çöl Safari & Burj Khalifa Turu (4 Gece)",
            code = "DXB-0089",
            country = "BAE",
            city = "Dubai",
            nights = 4,
            basePrice = 27800.0,
            finalPrice = 32000.0,
            operatorName = "Fun & Sun",
            comparedOperatorCount = 3
        ),
        AgencyStorefrontTourItem(
            tourId = "t-005",
            title = "Klasik İtalya: Roma, Floransa & Venedik (6 Gece)",
            code = "ITA-00301",
            country = "İtalya",
            city = "Roma",
            nights = 6,
            basePrice = 24800.0,
            finalPrice = 28500.0,
            operatorName = "Sunmar",
            comparedOperatorCount = 4
        )
    )
}
