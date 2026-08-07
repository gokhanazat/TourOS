package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatusLog
import com.mgacreative.touros.domain.repository.BookingRepository

data class BookingDetailResult(
    val booking: Booking,
    val statusLogs: List<BookingStatusLog> = emptyList()
)

class GetBookingDetailUseCase(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Result<BookingDetailResult> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("Rezervasyon ID boş olamaz"))
        }

        return bookingRepository.getBookingById(bookingId).map { booking ->
            val logs = bookingRepository.getBookingStatusLogs(bookingId).getOrDefault(emptyList())
            BookingDetailResult(booking = booking, statusLogs = logs)
        }
    }
}
