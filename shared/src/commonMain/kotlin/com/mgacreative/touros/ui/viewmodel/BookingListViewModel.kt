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
            fetchBookings()
        }
    }

    private suspend fun fetchBookings() {
        getBookingsUseCase.getBookings(
            tenantId = tenantId,
            statusFilter = currentStatus,
            tourIdFilter = currentTourId,
            startDate = currentStartDate,
            endDate = currentEndDate,
            searchQuery = currentSearch
        ).onSuccess { list ->
            _uiState.value = BookingListUiState.Success(
                bookings = list,
                tours = toursList,
                selectedStatusFilter = currentStatus,
                selectedTourFilterId = currentTourId,
                startDateFilter = currentStartDate,
                endDateFilter = currentEndDate,
                searchQuery = currentSearch
            )
        }.onFailure { err ->
            _uiState.value = BookingListUiState.Error(
                err.message ?: "Rezervasyonlar yüklenirken hata oluştu"
            )
        }
    }

    fun onStatusFilterSelected(status: BookingStatus?) {
        currentStatus = status
        viewModelScope.launch { fetchBookings() }
    }

    fun onTourFilterSelected(tourId: String?) {
        currentTourId = tourId
        viewModelScope.launch { fetchBookings() }
    }

    fun onDateRangeChanged(startDate: String?, endDate: String?) {
        currentStartDate = startDate
        currentEndDate = endDate
        viewModelScope.launch { fetchBookings() }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        viewModelScope.launch { fetchBookings() }
    }

    fun updateBookingStatus(bookingId: String, currentStatus: BookingStatus, targetStatus: BookingStatus) {
        viewModelScope.launch {
            updateBookingStatusUseCase(bookingId, currentStatus, targetStatus)
                .onSuccess { fetchBookings() }
        }
    }
}
