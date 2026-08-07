package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.*
import com.mgacreative.touros.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BookingWizardStep(val stepNumber: Int, val title: String) {
    SELECT_TOUR(1, "Tur Seçimi"),
    SELECT_DEPARTURE(2, "Tarih Seçimi"),
    SELECT_HOTEL(3, "Otel Seçimi"),
    SELECT_ROOM_TYPE(4, "Oda Tipi"),
    PASSENGERS(5, "Yolcular"),
    EXTRAS_TRANSFER(6, "Transfer & Ekstra"),
    DISCOUNT_COMMISSION(7, "İndirim & Komisyon"),
    CONFIRMATION(8, "Onay & Tamamla")
}

data class CreateBookingWizardUiState(
    val currentStep: BookingWizardStep = BookingWizardStep.SELECT_TOUR,
    val tours: List<Tour> = emptyList(),
    val departures: List<Departure> = emptyList(),
    val hotels: List<Hotel> = emptyList(),
    val roomTypes: List<RoomType> = emptyList(),
    val selectedTour: Tour? = null,
    val selectedDeparture: Departure? = null,
    val selectedHotel: Hotel? = null,
    val selectedRoomType: RoomType? = null,
    val nightCount: Int = 1,
    val adultCount: Int = 1,
    val childCount: Int = 0,
    val infantCount: Int = 0,
    val leadPassengerName: String = "",
    val leadPassengerEmail: String = "",
    val leadPassengerPhone: String = "",
    val leadPassengerTcNo: String = "",
    val passengersList: List<Passenger> = emptyList(),
    val selectedTransfer: String = "Yok",
    val transferPrice: Double = 0.0,
    val selectedExtras: Set<String> = emptySet(),
    val extrasTotalPrice: Double = 0.0,
    val couponCode: String = "",
    val discountAmount: Double = 0.0,
    val agencyCommissionRate: Double = 10.0,
    val bookingStatus: BookingStatus = BookingStatus.BEKLIYOR,
    val notes: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookingCreated: Boolean = false,
    val createdBookingId: String? = null
) {
    val totalPaxCount: Int get() = adultCount + childCount + infantCount
    val paxCount: Int get() = totalPaxCount

    val baseTourPrice: Double
        get() {
            val pricePerAdult = selectedDeparture?.priceOverride ?: selectedTour?.basePrice ?: 0.0
            val pricePerChild = pricePerAdult * 0.70
            return (adultCount * pricePerAdult) + (childCount * pricePerChild)
        }

    val roomTotalPrice: Double
        get() = (selectedRoomType?.basePricePerNight ?: 0.0) * nightCount

    val subtotalPrice: Double
        get() = baseTourPrice + roomTotalPrice + transferPrice + extrasTotalPrice

    val finalTotalPrice: Double
        get() = (subtotalPrice - discountAmount).coerceAtLeast(0.0)

    val totalCalculatedPrice: Double get() = finalTotalPrice

    val agencyCommissionAmount: Double
        get() = (finalTotalPrice * agencyCommissionRate) / 100.0

    val canProceedNext: Boolean
        get() = when (currentStep) {
            BookingWizardStep.SELECT_TOUR -> selectedTour != null
            BookingWizardStep.SELECT_DEPARTURE -> selectedDeparture != null
            BookingWizardStep.SELECT_HOTEL -> selectedHotel != null
            BookingWizardStep.SELECT_ROOM_TYPE -> selectedRoomType != null
            BookingWizardStep.PASSENGERS -> leadPassengerName.isNotBlank() && leadPassengerPhone.isNotBlank()
            BookingWizardStep.EXTRAS_TRANSFER -> true
            BookingWizardStep.DISCOUNT_COMMISSION -> true
            BookingWizardStep.CONFIRMATION -> !isLoading
        }
}

class CreateBookingWizardViewModel(
    private val getToursUseCase: GetToursUseCase,
    private val getTourDetailUseCase: GetTourDetailUseCase,
    private val getHotelsUseCase: GetHotelsUseCase,
    private val getRoomTypesUseCase: GetRoomTypesUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBookingWizardUiState())
    val uiState: StateFlow<CreateBookingWizardUiState> = _uiState.asStateFlow()

    private var tenantId: String = ""

    init {
        loadTours()
    }

    fun loadTours() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val user = getCurrentUserUseCase()
            tenantId = user?.tenantId ?: "tenant_id"

            getToursUseCase.getTours(tenantId = tenantId, statusFilter = true, searchQuery = _uiState.value.searchQuery)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(tours = list, isLoading = false)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadTours()
    }

    fun selectTour(tour: Tour) {
        _uiState.value = _uiState.value.copy(selectedTour = tour, selectedDeparture = null)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getTourDetailUseCase(tour.id)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(departures = detail.departures, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun selectDeparture(departure: Departure) {
        _uiState.value = _uiState.value.copy(selectedDeparture = departure)
        val city = _uiState.value.selectedTour?.city
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getHotelsUseCase(tenantId, city)
                .onSuccess { hotels ->
                    _uiState.value = _uiState.value.copy(hotels = hotels, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun selectHotel(hotel: Hotel) {
        _uiState.value = _uiState.value.copy(selectedHotel = hotel, selectedRoomType = null)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getRoomTypesUseCase(hotel.id)
                .onSuccess { rooms ->
                    _uiState.value = _uiState.value.copy(roomTypes = rooms, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun selectRoomType(roomType: RoomType) {
        _uiState.value = _uiState.value.copy(selectedRoomType = roomType)
    }

    fun setNightCount(count: Int) {
        if (count >= 1) _uiState.value = _uiState.value.copy(nightCount = count)
    }

    fun setPaxCount(count: Int) {
        updatePaxCounts(count, _uiState.value.childCount, _uiState.value.infantCount)
    }

    fun updatePaxCounts(adults: Int, children: Int, infants: Int) {
        val safeAdults = adults.coerceAtLeast(1)
        val safeChildren = children.coerceAtLeast(0)
        val safeInfants = infants.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            adultCount = safeAdults,
            childCount = safeChildren,
            infantCount = safeInfants
        )
    }

    fun updateLeadPassenger(name: String, email: String, phone: String, tcNo: String) {
        _uiState.value = _uiState.value.copy(
            leadPassengerName = name,
            leadPassengerEmail = email,
            leadPassengerPhone = phone,
            leadPassengerTcNo = tcNo
        )
    }

    fun selectTransfer(transferType: String, price: Double) {
        _uiState.value = _uiState.value.copy(
            selectedTransfer = transferType,
            transferPrice = price
        )
    }

    fun toggleExtraService(serviceName: String, price: Double) {
        val currentExtras = _uiState.value.selectedExtras.toMutableSet()
        var currentExtrasPrice = _uiState.value.extrasTotalPrice
        if (currentExtras.contains(serviceName)) {
            currentExtras.remove(serviceName)
            currentExtrasPrice -= price
        } else {
            currentExtras.add(serviceName)
            currentExtrasPrice += price
        }
        _uiState.value = _uiState.value.copy(
            selectedExtras = currentExtras,
            extrasTotalPrice = currentExtrasPrice.coerceAtLeast(0.0)
        )
    }

    fun applyCoupon(code: String) {
        val discount = if (code.equals("PROMO10", ignoreCase = true)) {
            _uiState.value.subtotalPrice * 0.10
        } else if (code.equals("VIP1000", ignoreCase = true)) {
            1000.0
        } else {
            0.0
        }
        _uiState.value = _uiState.value.copy(
            couponCode = code,
            discountAmount = discount
        )
    }

    fun setAgencyCommissionRate(rate: Double) {
        _uiState.value = _uiState.value.copy(agencyCommissionRate = rate)
    }

    fun setBookingStatus(status: BookingStatus) {
        _uiState.value = _uiState.value.copy(bookingStatus = status)
    }

    fun setNotes(notesText: String) {
        _uiState.value = _uiState.value.copy(notes = notesText)
    }

    fun goToStep(step: BookingWizardStep) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    fun goToNextStep() {
        val nextStep = when (_uiState.value.currentStep) {
            BookingWizardStep.SELECT_TOUR -> BookingWizardStep.SELECT_DEPARTURE
            BookingWizardStep.SELECT_DEPARTURE -> BookingWizardStep.SELECT_HOTEL
            BookingWizardStep.SELECT_HOTEL -> BookingWizardStep.SELECT_ROOM_TYPE
            BookingWizardStep.SELECT_ROOM_TYPE -> BookingWizardStep.PASSENGERS
            BookingWizardStep.PASSENGERS -> BookingWizardStep.EXTRAS_TRANSFER
            BookingWizardStep.EXTRAS_TRANSFER -> BookingWizardStep.DISCOUNT_COMMISSION
            BookingWizardStep.DISCOUNT_COMMISSION -> BookingWizardStep.CONFIRMATION
            BookingWizardStep.CONFIRMATION -> BookingWizardStep.CONFIRMATION
        }
        _uiState.value = _uiState.value.copy(currentStep = nextStep)
    }

    fun goToPreviousStep() {
        val prevStep = when (_uiState.value.currentStep) {
            BookingWizardStep.SELECT_TOUR -> BookingWizardStep.SELECT_TOUR
            BookingWizardStep.SELECT_DEPARTURE -> BookingWizardStep.SELECT_TOUR
            BookingWizardStep.SELECT_HOTEL -> BookingWizardStep.SELECT_DEPARTURE
            BookingWizardStep.SELECT_ROOM_TYPE -> BookingWizardStep.SELECT_HOTEL
            BookingWizardStep.PASSENGERS -> BookingWizardStep.SELECT_ROOM_TYPE
            BookingWizardStep.EXTRAS_TRANSFER -> BookingWizardStep.PASSENGERS
            BookingWizardStep.DISCOUNT_COMMISSION -> BookingWizardStep.EXTRAS_TRANSFER
            BookingWizardStep.CONFIRMATION -> BookingWizardStep.DISCOUNT_COMMISSION
        }
        _uiState.value = _uiState.value.copy(currentStep = prevStep)
    }

    fun submitBooking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentState = _uiState.value

            val items = mutableListOf<BookingItem>()
            currentState.selectedRoomType?.let { room ->
                items.add(
                    BookingItem(
                        description = "Otel Konaklama: ${currentState.selectedHotel?.name} (${room.name}) x ${currentState.nightCount} Gece",
                        quantity = currentState.nightCount,
                        unitPrice = room.basePricePerNight,
                        totalPrice = room.basePricePerNight * currentState.nightCount,
                        itemType = "accommodation"
                    )
                )
            }
            if (currentState.transferPrice > 0) {
                items.add(
                    BookingItem(
                        description = "Transfer Hizmeti: ${currentState.selectedTransfer}",
                        quantity = 1,
                        unitPrice = currentState.transferPrice,
                        totalPrice = currentState.transferPrice,
                        itemType = "transfer"
                    )
                )
            }
            currentState.selectedExtras.forEach { extraName ->
                items.add(
                    BookingItem(
                        description = "Ekstra Servis: $extraName",
                        quantity = 1,
                        unitPrice = 500.0,
                        totalPrice = 500.0,
                        itemType = "extra"
                    )
                )
            }

            val leadPassenger = Passenger(
                fullName = currentState.leadPassengerName,
                email = currentState.leadPassengerEmail,
                phone = currentState.leadPassengerPhone,
                tcNo = currentState.leadPassengerTcNo,
                isLead = true
            )

            val booking = Booking(
                departureId = currentState.selectedDeparture?.id ?: "",
                customerName = currentState.leadPassengerName,
                customerEmail = currentState.leadPassengerEmail,
                customerPhone = currentState.leadPassengerPhone,
                totalPrice = currentState.finalTotalPrice,
                currency = "TRY",
                paxCount = currentState.totalPaxCount,
                status = currentState.bookingStatus,
                notes = currentState.notes,
                tenantId = tenantId,
                items = items,
                passengers = listOf(leadPassenger)
            )

            createBookingUseCase(booking)
                .onSuccess { created ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isBookingCreated = true,
                        createdBookingId = created.id
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = err.message ?: "Rezervasyon kaydedilemedi"
                    )
                }
        }
    }
}
