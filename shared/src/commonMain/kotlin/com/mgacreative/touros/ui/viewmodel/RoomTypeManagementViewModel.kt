package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RoomTypeUiState {
    data object Loading : RoomTypeUiState
    data class Success(
        val hotelName: String = "Grand Cave Suites",
        val roomTypes: List<RoomType> = emptyList()
    ) : RoomTypeUiState
    data class Error(val message: String) : RoomTypeUiState
}

data class RoomTypeFormState(
    val id: String? = null,
    val name: String = "",
    val basePricePerNight: String = "1500",
    val currency: String = "TRY",
    val maxOccupancy: Int = 2,
    val totalRooms: String = "10",
    val allotment: String = "5",
    val description: String = ""
)

class RoomTypeManagementViewModel(
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoomTypeUiState>(RoomTypeUiState.Loading)
    val uiState: StateFlow<RoomTypeUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RoomTypeFormState())
    val formState: StateFlow<RoomTypeFormState> = _formState.asStateFlow()

    private var currentHotelId: String = "1"

    fun initForHotel(hotelId: String) {
        currentHotelId = hotelId
        loadRoomTypes()
    }

    fun loadRoomTypes() {
        viewModelScope.launch {
            _uiState.value = RoomTypeUiState.Loading
            val result = hotelRepository.getRoomTypesForHotel(currentHotelId)
            result.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        RoomType("1", currentHotelId, "Standart Kara Manzaralı Oda", "Manzaralı çift kişilik konforlu oda.", 1800.0, "TRY", 2, 12, 6, 2),
                        RoomType("2", currentHotelId, "Deluxe Mağara Suit", "Özel jakuzili ve teraslı süit oda.", 3500.0, "TRY", 3, 5, 4, 3),
                        RoomType("3", currentHotelId, "Aile Odası (2+2)", "Geniş ara kapılı 4 kişilik oda.", 2800.0, "TRY", 4, 8, 4, 1)
                    )
                } else list
                _uiState.value = RoomTypeUiState.Success(roomTypes = fallbackList)
            }.onFailure { err ->
                _uiState.value = RoomTypeUiState.Error(err.message ?: "Oda tipleri yüklenirken hata oluştu")
            }
        }
    }

    fun updateFormName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updateFormPrice(price: String) { _formState.value = _formState.value.copy(basePricePerNight = price) }
    fun updateFormMaxOccupancy(occ: Int) { _formState.value = _formState.value.copy(maxOccupancy = occ) }
    fun updateFormTotalRooms(count: String) { _formState.value = _formState.value.copy(totalRooms = count) }
    fun updateFormAllotment(count: String) { _formState.value = _formState.value.copy(allotment = count) }
    fun updateFormDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }

    fun saveRoomType() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val state = _formState.value

            val roomTypeToSave = RoomType(
                id = state.id ?: "",
                hotelId = currentHotelId,
                name = state.name,
                description = state.description,
                basePricePerNight = state.basePricePerNight.toDoubleOrNull() ?: 1500.0,
                currency = state.currency,
                maxOccupancy = state.maxOccupancy,
                totalRooms = state.totalRooms.toIntOrNull() ?: 10,
                allotment = state.allotment.toIntOrNull() ?: 5,
                tenantId = tenantId
            )

            if (state.id.isNullOrBlank()) {
                hotelRepository.createRoomType(roomTypeToSave)
            } else {
                hotelRepository.updateRoomType(roomTypeToSave)
            }

            _formState.value = RoomTypeFormState()
            loadRoomTypes()
        }
    }
}
