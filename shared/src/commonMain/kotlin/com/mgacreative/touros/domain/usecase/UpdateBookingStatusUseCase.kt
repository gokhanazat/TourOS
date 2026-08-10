package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.BookingStateMachine
import com.mgacreative.touros.domain.repository.BookingRepository

class UpdateBookingStatusUseCase(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String, currentStatus: BookingStatus, targetStatus: BookingStatus): Result<Unit> {
        // Kullanıcı talebi: İptal edildiye basılınca karmaşa yaratmaması için veritabanından tamamen silinsin
        if (targetStatus == BookingStatus.IPTAL) {
            return bookingRepository.deleteBooking(bookingId)
        }

        val transitionCheck = BookingStateMachine.transition(currentStatus, targetStatus)
        if (transitionCheck.isFailure) {
            return Result.failure(transitionCheck.exceptionOrNull() ?: IllegalArgumentException("Geçersiz durum geçişi"))
        }

        return bookingRepository.updateBookingStatus(bookingId, targetStatus.dbValue)
    }
}
