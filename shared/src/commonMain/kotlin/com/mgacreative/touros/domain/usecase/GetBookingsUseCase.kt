package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.repository.BookingRepository

class GetBookingsUseCase(
    private val bookingRepository: BookingRepository
) {
    suspend fun getBookings(
        tenantId: String,
        statusFilter: BookingStatus? = null,
        tourIdFilter: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        searchQuery: String = ""
    ): Result<List<Booking>> {
        return bookingRepository.getBookings(tenantId).map { list ->
            list.filter { booking ->
                val matchesStatus = statusFilter == null || booking.status == statusFilter
                val matchesTour = tourIdFilter.isNullOrBlank() || booking.departureId == tourIdFilter
                val matchesSearch = searchQuery.isBlank() ||
                        booking.bookingCode.contains(searchQuery, ignoreCase = true) ||
                        (booking.operatorPnrCode?.contains(searchQuery, ignoreCase = true) == true) ||
                        booking.customerName.contains(searchQuery, ignoreCase = true) ||
                        (booking.customerPhone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (booking.customerEmail?.contains(searchQuery, ignoreCase = true) == true)

                val matchesStartDate = startDate.isNullOrBlank() || booking.createdAt.isBlank() || booking.createdAt >= startDate
                val matchesEndDate = endDate.isNullOrBlank() || booking.createdAt.isBlank() || booking.createdAt <= endDate

                matchesStatus && matchesTour && matchesSearch && matchesStartDate && matchesEndDate
            }
        }
    }
}
