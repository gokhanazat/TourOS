package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Booking (Rezervasyon) Domain Modeli.
 */
@Serializable
data class Booking(
    val id: String = "",
    val bookingCode: String = "",
    val departureId: String? = null,
    val customerId: String? = null,
    val agencyId: String? = null,
    val customerName: String = "",
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val totalPrice: Double = 0.0,
    val currency: String = "TRY",
    val paxCount: Int = 1,
    val status: BookingStatus = BookingStatus.BEKLIYOR,
    val notes: String? = null,
    val optionExpiration: String? = null,
    val confirmedAt: String? = null,
    val cancelledAt: String? = null,
    val operatorName: String = "MGA Creative",
    val productName: String = "Kapadokya Turu",
    val departureDate: String = "2026-09-01",
    val hotelId: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val roomTypeName: String? = null,
    val nights: Int = 1,
    val bookingType: String = "TOUR",
    val paymentMethod: String? = null,
    val operatorPnrCode: String? = null,
    val operatorStatus: String? = "BEKLİYOR",
    val tenantId: String = "",
    val items: List<BookingItem> = emptyList(),
    val passengers: List<Passenger> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    /**
     * Rezervasyonun hedeflenen duruma geçip geçemeyeceğini kontrol eder.
     */
    fun canTransitionTo(target: BookingStatus): Boolean {
        return BookingStateMachine.canTransition(status, target)
    }

    /**
     * Geçilebilecek izinli durumların listesini verir.
     */
    val allowedNextStatuses: List<BookingStatus>
        get() = BookingStateMachine.getAllowedNextStatuses(status)
}
