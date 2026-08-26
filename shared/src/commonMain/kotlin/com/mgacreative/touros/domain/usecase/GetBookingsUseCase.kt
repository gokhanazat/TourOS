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
                // Özel olarak "İptal" veya "Tamamlandı (Arşiv)" filtresi seçilmedikçe ana listede gösterme
                val matchesStatus = if (statusFilter != null) {
                    booking.status == statusFilter
                } else {
                    booking.status != BookingStatus.IPTAL && booking.status != BookingStatus.TAMAMLANDI
                }

                val matchesTour = tourIdFilter.isNullOrBlank() || 
                        booking.departureId == tourIdFilter || 
                        booking.hotelId == tourIdFilter

                val matchesSearch = searchQuery.isBlank() ||
                        booking.bookingCode.contains(searchQuery, ignoreCase = true) ||
                        (booking.operatorPnrCode?.contains(searchQuery, ignoreCase = true) == true) ||
                        booking.customerName.contains(searchQuery, ignoreCase = true) ||
                        (booking.customerPhone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (booking.customerEmail?.contains(searchQuery, ignoreCase = true) == true) ||
                        booking.productName.contains(searchQuery, ignoreCase = true)

                val bookingDate = booking.createdAt.take(10).trim()
                val cleanStart = startDate?.take(10)?.trim()
                val cleanEnd = endDate?.take(10)?.trim()

                val matchesStartDate = cleanStart.isNullOrBlank() || bookingDate.isBlank() || bookingDate >= cleanStart
                val matchesEndDate = cleanEnd.isNullOrBlank() || bookingDate.isBlank() || bookingDate <= cleanEnd

                matchesStatus && matchesTour && matchesSearch && matchesStartDate && matchesEndDate
            }
        }
    }
}
