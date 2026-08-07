package com.mgacreative.touros.domain.mapper

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Departure
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTABookingStatus
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAReservation

/**
 * 4.5.1 OTA Domain Modelleri Mappers.
 * Network / Supabase bağımlılığı olmadan OTA verilerini TourOS ana domain nesnelerine çevirir.
 */
object OtaMappers {

    fun OTABooking.toBooking(tenantId: String = ""): Booking {
        val mappedStatus = when (this.status) {
            OTABookingStatus.CONFIRMED -> BookingStatus.ONAYLANDI
            OTABookingStatus.CANCELLED -> BookingStatus.IPTAL
            OTABookingStatus.PENDING -> BookingStatus.BEKLIYOR
            OTABookingStatus.MODIFIED -> BookingStatus.ONAYLANDI
            OTABookingStatus.FAILED -> BookingStatus.IPTAL
        }

        return Booking(
            id = this.bookingId ?: this.otaBookingId,
            bookingCode = this.otaReference,
            departureId = "",
            customerId = null,
            agencyId = this.accountId,
            customerName = "OTA Customer (${this.otaReference})",
            totalPrice = this.totalAmount,
            currency = this.currency,
            paxCount = this.paxCount,
            status = mappedStatus,
            notes = "OTA Entegrasyonu Üzerinden Gelen Rezervasyon (${this.otaReference})",
            tenantId = tenantId
        )
    }

    fun OTAProduct.toTour(tenantId: String = ""): Tour {
        return Tour(
            id = this.mappedTourId.ifBlank { this.tourId },
            code = this.externalProductCode,
            title = this.title,
            country = "TÜRKİYE",
            city = "KAPADOKYA",
            tenantId = tenantId
        )
    }

    fun OTAAvailability.toDeparture(): Departure {
        return Departure(
            id = this.departureId,
            tourId = this.otaProductId,
            departureDate = this.date,
            priceOverride = this.price,
            capacity = this.availableCapacity
        )
    }

    fun OTAReservation.toPassenger(): Passenger {
        return Passenger(
            id = this.reservationId,
            bookingId = this.otaBookingId,
            fullName = this.passengerName,
            passportNo = this.passportNo,
            email = this.passengerEmail,
            isLead = true
        )
    }
}
