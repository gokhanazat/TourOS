package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.usecase.GetBookingsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetToursUseCase
import com.mgacreative.touros.domain.usecase.UpdateBookingStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BookingListUiState {
    data object Loading : BookingListUiState
    data class Success(
        val bookings: List<Booking>,
        val tours: List<Tour> = emptyList(),
        val selectedStatusFilter: BookingStatus? = null,
        val selectedTourFilterId: String? = null,
        val startDateFilter: String? = null,
        val endDateFilter: String? = null,
        val searchQuery: String = ""
    ) : BookingListUiState
    data class Error(val message: String) : BookingListUiState
}

class BookingListViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val updateBookingStatusUseCase: UpdateBookingStatusUseCase,
    private val getToursUseCase: GetToursUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingListUiState>(BookingListUiState.Loading)
    val uiState: StateFlow<BookingListUiState> = _uiState.asStateFlow()

    private var tenantId: String = ""
    private var toursList: List<Tour> = emptyList()
    private var allBookingsList: List<Booking> = emptyList()

    private var currentStatus: BookingStatus? = null
    private var currentTourId: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null
    private var currentSearch: String = ""

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = BookingListUiState.Loading
            val user = getCurrentUserUseCase()
            tenantId = user?.tenantId.orEmpty()

            getToursUseCase.getTours(tenantId).onSuccess { toursList = it }
            fetchBookingsFromRemote()
        }
    }

    private suspend fun fetchBookingsFromRemote() {
        getBookingsUseCase.getBookings(
            tenantId = tenantId,
            statusFilter = null,
            tourIdFilter = null,
            startDate = null,
            endDate = null,
            searchQuery = ""
        ).onSuccess { list ->
            allBookingsList = list
            applyLocalFilters()
        }.onFailure { err ->
            _uiState.value = BookingListUiState.Error(
                err.message ?: "Rezervasyonlar yüklenirken hata oluştu"
            )
        }
    }

    private fun applyLocalFilters() {
        val q = currentSearch.trim()
        val filtered = allBookingsList.filter { booking ->
            val matchesStatus = if (currentStatus != null) {
                booking.status == currentStatus
            } else {
                booking.status != BookingStatus.IPTAL && booking.status != BookingStatus.TAMAMLANDI
            }
            val matchesTour = currentTourId.isNullOrBlank() ||
                    booking.departureId == currentTourId ||
                    booking.hotelId == currentTourId
            val matchesDate = when {
                currentStartDate != null && booking.departureDate.isNotBlank() -> booking.departureDate >= currentStartDate!!
                currentEndDate != null && booking.departureDate.isNotBlank() -> booking.departureDate <= currentEndDate!!
                else -> true
            }
            val matchesSearch = if (q.isBlank()) true else {
                booking.bookingCode.contains(q, ignoreCase = true) ||
                (booking.operatorPnrCode?.contains(q, ignoreCase = true) == true) ||
                booking.customerName.contains(q, ignoreCase = true) ||
                (booking.customerPhone?.contains(q, ignoreCase = true) == true) ||
                (booking.customerEmail?.contains(q, ignoreCase = true) == true) ||
                booking.productName.contains(q, ignoreCase = true) ||
                booking.passengers.any { p ->
                    p.fullName.contains(q, ignoreCase = true) ||
                    (p.tcNo?.contains(q, ignoreCase = true) == true) ||
                    (p.passportNo?.contains(q, ignoreCase = true) == true) ||
                    (p.phone?.contains(q, ignoreCase = true) == true)
                }
            }
            matchesStatus && matchesTour && matchesDate && matchesSearch
        }

        _uiState.value = BookingListUiState.Success(
            bookings = filtered,
            tours = toursList,
            selectedStatusFilter = currentStatus,
            selectedTourFilterId = currentTourId,
            startDateFilter = currentStartDate,
            endDateFilter = currentEndDate,
            searchQuery = currentSearch
        )
    }

    fun onStatusFilterSelected(status: BookingStatus?) {
        currentStatus = status
        applyLocalFilters()
    }

    fun onTourFilterSelected(tourId: String?) {
        currentTourId = tourId
        applyLocalFilters()
    }

    fun onDateRangeChanged(startDate: String?, endDate: String?) {
        currentStartDate = startDate
        currentEndDate = endDate
        applyLocalFilters()
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        applyLocalFilters()
    }

    fun updateBookingStatus(bookingId: String, currentStatus: BookingStatus, targetStatus: BookingStatus) {
        viewModelScope.launch {
            updateBookingStatusUseCase(bookingId, currentStatus, targetStatus)
                .onSuccess { fetchBookingsFromRemote() }
        }
    }
}
