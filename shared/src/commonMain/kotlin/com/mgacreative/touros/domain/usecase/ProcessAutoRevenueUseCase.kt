package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.engine.AutoRevenueEngine
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Invoice

/**
 * 3.1.2 Onaylanan Rezervasyonu Otomatik Gelir/Fatura Kaydına Dönüştürme Use Case.
 */
class ProcessAutoRevenueUseCase(
    private val autoRevenueEngine: AutoRevenueEngine
) {
    suspend operator fun invoke(booking: Booking): Result<Invoice> {
        if (booking.status != BookingStatus.ONAYLANDI) {
            return Result.failure(IllegalArgumentException("Sadece onaylanmış rezervasyonlar muhasebeleştirilebilir."))
        }
        return autoRevenueEngine.processBookingApproval(booking)
    }
}
