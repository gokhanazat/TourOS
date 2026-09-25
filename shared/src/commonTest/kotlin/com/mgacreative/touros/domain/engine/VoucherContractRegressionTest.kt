package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.Passenger
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Rusça Sözleşme ve Belge Şablon Motoru Regresyon Testleri.
 * Gerçek modeller, gerçek parametreler ve gerçek metin çıktıları üzerinden doğrulanır.
 */
class VoucherContractRegressionTest {

    private val templateEngine = VoucherContractTemplateEngine()

    @Test
    fun testBuildRussianContractDocumentContainsEssentialFields() {
        val leadTourist = Passenger(
            fullName = "SMIRNOV ALEKSEI",
            birthDate = "10.02.1980",
            passportSeries = "75",
            passportNo = "9876543",
            isLead = true
        )

        val companion = Passenger(
            fullName = "SMIRNOVA ELENA",
            birthDate = "14.07.1983",
            passportSeries = "75",
            passportNo = "1234567",
            isLead = false
        )

        val booking = Booking(
            id = "d8f1e2c3-4b5a-6789-0123-abcdef456789",
            bookingCode = "TUR-849201",
            productName = "Rixos Premium Belek, 5*",
            operatorName = "Coral Travel",
            bookingType = "PACKAGE_TOUR",
            totalPrice = 285000.0,
            currency = "RUB",
            departureDate = "15.10.2026",
            checkOutDate = "22.10.2026",
            nights = 7,
            roomTypeName = "Deluxe Room Sea View",
            customerName = "SMIRNOV ALEKSEI",
            customerPhone = "+7 (999) 111-22-33",
            customerEmail = "aleksei.smirnov@mail.ru",
            passengers = listOf(leadTourist, companion),
            createdAt = "25.09.2026"
        )

        val htmlOutput = templateEngine.buildRussianContractDocument(
            booking = booking,
            passengers = booking.passengers,
            agencyName = "ООО \"ТУРХАНТЕР\"",
            operatorLegalName = "ООО \"ТО КОРАЛ ТРЕВЕЛ ЦЕНТР\""
        )

        assertNotNull(htmlOutput)
        assertTrue(htmlOutput.isNotBlank(), "Oluşturulan HTML boş olmamalı")

        // Sözleşme numarası ve tarihler
        assertTrue(htmlOutput.contains("TUR-849201"), "Sözleşme kodu HTML içinde bulunmalı")
        assertTrue(htmlOutput.contains("25.09.2026"), "Sözleşme tarihi bulunmalı")

        // Müşteri / Yolcu bilgileri
        assertTrue(htmlOutput.contains("SMIRNOV ALEKSEI"), "Lider yolcu adı HTML içinde bulunmalı")
        assertTrue(htmlOutput.contains("SMIRNOVA ELENA"), "İkinci yolcu adı HTML içinde bulunmalı")
        assertTrue(htmlOutput.contains("9876543"), "Pasaport numarası bulunmalı")

        // Otel ve hizmet bilgileri
        assertTrue(htmlOutput.contains("Rixos Premium Belek"), "Otel adı bulunmalı")
        assertTrue(htmlOutput.contains("Deluxe Room Sea View"), "Oda tipi bulunmalı")

        // Acente ve Operatör
        assertTrue(htmlOutput.contains("ООО \"ТУРХАНТЕР\""), "Acente adı bulunmalı")
        assertTrue(htmlOutput.contains("ООО \"ТО КОРАЛ ТРЕВЕЛ ЦЕНТР\""), "Operatör adı bulunmalı")

        // Fiyat
        assertTrue(htmlOutput.contains("285"), "Toplam tutar yer almalı")
    }
}
