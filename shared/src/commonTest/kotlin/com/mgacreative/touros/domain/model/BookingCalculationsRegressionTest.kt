package com.mgacreative.touros.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Rezervasyon ve Yolcu Gerçek Veri & İş Mantığı Regresyon Testleri.
 * Mock kullanılmadan doğrudan domain modelleri üzerinden çalışır.
 */
class BookingCalculationsRegressionTest {

    @Test
    fun testDisplayOperatorOrAgencyNameForTourOperator() {
        val booking = Booking(
            id = "bk-001",
            bookingCode = "TR-2026-001",
            productName = "Akka Alinda Hotel 5*",
            operatorName = "Coral Travel / B2B Master",
            bookingType = "PACKAGE_TOUR",
            totalPrice = 145000.0,
            currency = "RUB"
        )

        assertEquals("Coral Travel", booking.displayOperatorOrAgencyName)
    }

    @Test
    fun testDisplayOperatorOrAgencyNameForLocalTour() {
        val booking = Booking(
            id = "bk-002",
            bookingCode = "TR-2026-002",
            productName = "Kapadokya Balon Turu Yerel",
            operatorName = "TourOS Merkez / Bozaci Turizm",
            bookingType = "LOCAL_TOUR",
            totalPrice = 25000.0,
            currency = "TRY"
        )

        assertEquals("Bozaci Turizm", booking.displayOperatorOrAgencyName)
    }

    @Test
    fun testBookingStatusTransitions() {
        val booking = Booking(
            id = "bk-003",
            status = BookingStatus.BEKLIYOR
        )

        assertTrue(booking.canTransitionTo(BookingStatus.ONAYLANDI))
        assertTrue(booking.canTransitionTo(BookingStatus.OPSIYON))
        assertTrue(booking.canTransitionTo(BookingStatus.IPTAL))

        val allowed = booking.allowedNextStatuses
        assertTrue(allowed.contains(BookingStatus.ONAYLANDI))
        assertTrue(allowed.contains(BookingStatus.OPSIYON))
        assertTrue(allowed.contains(BookingStatus.IPTAL))
    }

    @Test
    fun testPassengerLeadRoleAndPassportDetails() {
        val leadTourist = Passenger(
            fullName = "IVANOV IVAN",
            birthDate = "15.05.1985",
            passportSeries = "51",
            passportNo = "1234567",
            isLead = true,
            documentType = "PASSPORT",
            phone = "+7 999 123 45 67",
            email = "ivanov@mail.ru"
        )

        val companion = Passenger(
            fullName = "IVANOVA ANNA",
            birthDate = "20.10.1988",
            passportSeries = "51",
            passportNo = "7654321",
            isLead = false,
            documentType = "PASSPORT"
        )

        val booking = Booking(
            id = "bk-004",
            passengers = listOf(leadTourist, companion),
            customerName = leadTourist.fullName
        )

        assertEquals(2, booking.passengers.size)
        assertEquals("IVANOV IVAN", booking.passengers.first { it.isLead }.fullName)
        assertEquals("51", booking.passengers.first { it.isLead }.passportSeries)
        assertEquals("1234567", booking.passengers.first { it.isLead }.passportNo)
        assertEquals("+7 999 123 45 67", booking.passengers.first { it.isLead }.phone)
    }
}
