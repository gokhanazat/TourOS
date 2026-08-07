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

data class HotelFormUiState(
    val hotelId: String? = null,
    val name: String = "",
    val city: String = "",
    val country: String = "Türkiye",
    val starRating: Int = 4,
    val description: String = "",
    val coverImageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

class HotelFormViewModel(
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotelFormUiState())
    val uiState: StateFlow<HotelFormUiState> = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateCity(city: String) { _uiState.value = _uiState.value.copy(city = city) }
    fun updateCountry(country: String) { _uiState.value = _uiState.value.copy(country = country) }
    fun updateStarRating(rating: Int) { _uiState.value = _uiState.value.copy(starRating = rating) }
    fun updateDescription(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun updateCoverImageUrl(url: String) { _uiState.value = _uiState.value.copy(coverImageUrl = url) }
    fun updateAddress(addr: String) { _uiState.value = _uiState.value.copy(address = addr) }
    fun updatePhone(phone: String) { _uiState.value = _uiState.value.copy(phone = phone) }
    fun updateEmail(email: String) { _uiState.value = _uiState.value.copy(email = email) }

    fun loadHotelForEdit(hotelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            hotelRepository.getHotelById(hotelId).onSuccess { hotel ->
                _uiState.value = HotelFormUiState(
                    hotelId = hotel.id,
                    name = hotel.name,
                    city = hotel.city ?: "",
                    country = hotel.country,
                    starRating = hotel.starRating ?: 4,
                    description = hotel.description ?: "",
                    coverImageUrl = hotel.coverImageUrl ?: "",
                    address = hotel.address ?: "",
                    phone = hotel.phone ?: "",
                    email = hotel.email ?: ""
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Otel bilgisi getirilemedi")
            }
        }
    }

    fun saveHotel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val state = _uiState.value
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val hotelToSave = Hotel(
                id = state.hotelId ?: "",
                name = state.name,
                city = state.city,
                country = state.country,
                starRating = state.starRating,
                description = state.description,
                coverImageUrl = state.coverImageUrl,
                address = state.address,
                phone = state.phone,
                email = state.email,
                tenantId = tenantId
            )

            val result = if (state.hotelId.isNullOrBlank()) {
                hotelRepository.createHotel(hotelToSave)
            } else {
                hotelRepository.updateHotel(hotelToSave)
            }

            result.onSuccess {
                _uiState.value = state.copy(isLoading = false, isSavedSuccess = true)
            }.onFailure { err ->
                _uiState.value = state.copy(isLoading = false, errorMessage = err.message ?: "Kayıt işlemi başarısız")
            }
        }
    }
}
