package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.repository.BookingRepository

class CreateBookingUseCase(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(booking: Booking): Result<Booking> {
        if (booking.departureId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kalkış tarihi seçilmelidir"))
        }
        return bookingRepository.createBooking(booking)
    }
}
