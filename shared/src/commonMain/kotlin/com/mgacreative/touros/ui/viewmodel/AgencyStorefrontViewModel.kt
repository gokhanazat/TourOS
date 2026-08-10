package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyBrandingEntity
import com.mgacreative.touros.data.database.entity.AgencyStorefrontTourItem
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.domain.repository.HotelRepository
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
        val tours: List<AgencyStorefrontTourItem>,
        val hotels: List<Hotel>
    ) : AgencyStorefrontUiState
    data class Error(val message: String) : AgencyStorefrontUiState
}

class AgencyStorefrontViewModel(
    private val supabaseClient: SupabaseClient,
    private val companySettingsRepository: CompanySettingsRepository,
    private val hotelRepository: HotelRepository
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
                val companySettings = companySettingsRepository.getCompanySettings("00000000-0000-0000-0000-000000000001").getOrNull()

                val brandingParams = buildJsonObject { put("p_agency_id", "00000000-0000-0000-0000-000000000001") }
                val rpcBranding = runCatching {
                    supabaseClient.postgrest.rpc("get_agency_branding", brandingParams)
                        .decodeSingleOrNull<AgencyBrandingEntity>()
                }.getOrNull()

                val branding = AgencyBrandingEntity(
                    agencyId = "00000000-0000-0000-0000-000000000001",
                    heroTitle = companySettings?.name?.takeIf { it.isNotBlank() } ?: rpcBranding?.heroTitle ?: "Hayalinizdeki Turu Keşfedin",
                    heroSubtitle = companySettings?.heroSubtitle?.takeIf { it.isNotBlank() } ?: rpcBranding?.heroSubtitle ?: "En iyi tur operatörlerinden karşılaştırmalı teklifler",
                    customLogoUrl = companySettings?.logoUrl ?: rpcBranding?.customLogoUrl,
                    headerImageUrl = companySettings?.headerImageUrl ?: rpcBranding?.headerImageUrl,
                    footerText = companySettings?.footerText?.takeIf { it.isNotBlank() } ?: rpcBranding?.footerText ?: "© 2026 Tüm Hakları Saklıdır",
                    contactEmail = companySettings?.webEmail?.takeIf { it.isNotBlank() } ?: rpcBranding?.contactEmail ?: companySettings?.email,
                    contactPhone = companySettings?.webPhone?.takeIf { it.isNotBlank() } ?: rpcBranding?.contactPhone ?: companySettings?.phone,
                    whatsappNumber = companySettings?.webWhatsapp?.takeIf { it.isNotBlank() } ?: rpcBranding?.whatsappNumber,
                    contactAddress = companySettings?.webAddress?.takeIf { it.isNotBlank() } ?: rpcBranding?.contactAddress ?: companySettings?.address
                )

                val searchParams = buildJsonObject {
                    put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                    put("p_country", countryFilter)
                    put("p_min_nights", minNights)
                    put("p_max_nights", maxNights)
                    put("p_max_budget", maxBudget)
                }
                val loadedTours = runCatching {
                    supabaseClient.postgrest.rpc("search_agency_storefront_tours", searchParams)
                        .decodeList<AgencyStorefrontTourItem>()
                }.getOrDefault(emptyList())

                val toursTableResult = runCatching {
                    supabaseClient.postgrest.from("tours")
                        .select {
                            filter {
                                eq("is_active", true)
                            }
                        }
                        .decodeList<com.mgacreative.touros.data.database.entity.TourEntity>()
                        .map { entity ->
                            AgencyStorefrontTourItem(
                                tourId = entity.id ?: "",
                                title = entity.title,
                                code = entity.code,
                                country = entity.country,
                                city = entity.city,
                                nights = entity.durationDays,
                                basePrice = entity.basePrice,
                                finalPrice = entity.basePrice,
                                operatorName = companySettings?.name?.takeIf { it.isNotBlank() } ?: "TourOS Operatör",
                                comparedOperatorCount = 1,
                                coverImageUrl = entity.coverImageUrl
                            )
                        }
                }.getOrDefault(emptyList())

                val combinedTours = (loadedTours + toursTableResult).distinctBy { it.tourId }

                val registeredHotels = hotelRepository.getHotels("00000000-0000-0000-0000-000000000001")
                    .getOrDefault(emptyList())
                    .filter { it.isActive }

                AgencyStorefrontUiState.Success(branding = branding, tours = combinedTours, hotels = registeredHotels)
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure {
                val companySettings = companySettingsRepository.getCompanySettings("00000000-0000-0000-0000-000000000001").getOrNull()
                val registeredHotels = hotelRepository.getHotels("00000000-0000-0000-0000-000000000001")
                    .getOrDefault(emptyList())
                    .filter { it.isActive }

                _uiState.value = AgencyStorefrontUiState.Success(
                    branding = AgencyBrandingEntity(
                        heroTitle = companySettings?.name?.takeIf { it.isNotBlank() } ?: "Hayalinizdeki Turu Keşfedin",
                        heroSubtitle = companySettings?.heroSubtitle?.takeIf { it.isNotBlank() } ?: "80+ Tur Operatöründen Karşılaştırmalı Fiyat Garantisi",
                        customLogoUrl = companySettings?.logoUrl,
                        headerImageUrl = companySettings?.headerImageUrl,
                        footerText = companySettings?.footerText?.takeIf { it.isNotBlank() } ?: "© 2026 Tüm Hakları Saklıdır",
                        contactEmail = companySettings?.webEmail?.takeIf { it.isNotBlank() } ?: companySettings?.email,
                        contactPhone = companySettings?.webPhone?.takeIf { it.isNotBlank() } ?: companySettings?.phone,
                        whatsappNumber = companySettings?.webWhatsapp,
                        contactAddress = companySettings?.webAddress?.takeIf { it.isNotBlank() } ?: companySettings?.address
                    ),
                    tours = emptyList(),
                    hotels = registeredHotels
                )
            }
        }
    }
}
