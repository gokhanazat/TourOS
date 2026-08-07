package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HotelListUiState {
    data object Loading : HotelListUiState
    data class Success(val hotels: List<Hotel> = emptyList()) : HotelListUiState
    data class Error(val message: String) : HotelListUiState
}

class HotelListViewModel(
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HotelListUiState>(HotelListUiState.Loading)
    val uiState: StateFlow<HotelListUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = HotelListUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val result = hotelRepository.getHotels(tenantId)
            result.onSuccess { hotels ->
                val fallbackHotels = if (hotels.isEmpty()) {
                    listOf(
                        Hotel("1", "Grand Cave Suites", "grand-cave-suites", 5, "Göreme Mah. No:12", "Nevşehir", "Türkiye", "0384 271 2000", "info@grandcave.com", "https://grandcave.com", "Kapadokya vadilerine bakan lüks mağara otel.", "https://images.unsplash.com/photo-1566073771259-6a8506099945"),
                        Hotel("2", "Ramada Resort Kapadokya", "ramada-resort", 4, "Ürgüp Cad. No:45", "Nevşehir", "Türkiye", "0384 341 8000", "info@ramadacapadocia.com", "https://ramada.com", "Açık/kapalı havuzlu ve konforlu kongre oteli.", "https://images.unsplash.com/photo-1582719508461-905c673771fd"),
                        Hotel("3", "Bodrum Sunset Boutique Hotel", "bodrum-sunset", 4, "Turgutreis Mah. No:8", "Muğla", "Türkiye", "0252 382 1000", "contact@bodrumsunset.com", "https://bodrumsunset.com", "Denize sıfır butik konaklama tesisi.", "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4")
                    )
                } else hotels

                _uiState.value = HotelListUiState.Success(fallbackHotels)
            }.onFailure { err ->
                _uiState.value = HotelListUiState.Error(err.message ?: "Oteller yüklenirken hata oluştu")
            }
        }
    }
}
