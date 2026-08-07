package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Booking

/**
 * Rezervasyon Yönetimi Repository Arayüzü.
 */
interface BookingRepository {
    suspend fun getBookings(tenantId: String): Result<List<Booking>>
    suspend fun getBookingById(id: String): Result<Booking>
    suspend fun createBooking(booking: Booking): Result<Booking>
    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit>
    suspend fun getBookingStatusLogs(bookingId: String): Result<List<com.mgacreative.touros.domain.model.BookingStatusLog>>
}
