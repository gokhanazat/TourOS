package com.mgacreative.touros.data.database.entity

import com.mgacreative.touros.domain.model.TourOperatorConfig
import com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * UnifiedProductEntity, calculateMultiplier ve TourOperatorConfig
 * Sıfır Mock Regresyon Testleri.
 * Gerçek sınıflar ve doğrudan mantık çağrıları üzerinden çalışır.
 */
class UnifiedProductSearchRegressionTest {

    // =========================================================================
    // a) Ürün Tipi Ayrıştırma (PACKAGE_TOUR, FLIGHT, HOTEL)
    // =========================================================================
    @Test
    fun testProductTypeSeparationAndFlightExclusionFromPackageTours() {
        val packageTour = UnifiedProductEntity(
            id = "pkg-001",
            productType = "PACKAGE_TOUR",
            tourName = "Antalya Her Şey Dahil 7 Gece Tatili",
            hotelName = "Rixos Sungate",
            hotelCategory = 5,
            departureCity = "Moskova (VKO)",
            flightNumber = "TK-3991",
            operatorName = "Coral Travel",
            price = 120000.0
        )

        val flightOnly = UnifiedProductEntity(
            id = "flt-001",
            productType = "FLIGHT",
            tourName = "Moskova - Antalya Direkt Uçuş Bileti",
            hotelName = "", // Oteli yok
            hotelCategory = 0,
            departureCity = "Moskova (SVO)",
            flightNumber = "SU-2142",
            operatorName = "Pegas Touristik",
            price = 28000.0
        )

        val hotelOnly = UnifiedProductEntity(
            id = "htl-001",
            productType = "HOTEL",
            tourName = "Maxx Royal Kemer Sadece Konaklama",
            hotelName = "Maxx Royal Kemer",
            hotelCategory = 5,
            flightNumber = "", // Uçuşu yok
            departureCity = "",
            operatorName = "Bibloglobus",
            price = 95000.0
        )

        // 1. Ürün tipleri ve güvenli okuyucular doğrulanmalı
        assertEquals("PACKAGE_TOUR", packageTour.safeProductType)
        assertEquals("FLIGHT", flightOnly.safeProductType)
        assertEquals("HOTEL", hotelOnly.safeProductType)

        // 2. Oteli olmayan uçuş verisi asla paket tur veya otel olarak sınıflandırılmamalı
        assertTrue(flightOnly.safeHotelName.isBlank(), "Uçuş kaydının oteli olmamalı")
        assertTrue(flightOnly.safeProductType == "FLIGHT", "Uçuş tipi FLIGHT olmalı")
        assertFalse(flightOnly.safeProductType == "PACKAGE_TOUR", "Uçuş kaydı PACKAGE_TOUR olamaz")

        // 3. Paket turda hem otel adı hem tur adı geçerli olmalı
        assertTrue(packageTour.safeHotelName.isNotBlank(), "Paket turun otel adı bulunmalı")
        assertTrue(packageTour.safeHotelCategory == 5, "Paket turun otel kategorisi 5 yıldız olmalı")
        assertTrue(packageTour.safeFlightNumber.isNotBlank(), "Paket tur uçuş numarası içermeli")

        // 4. Sadece otel konaklamasında uçuş numarası boş olmalı
        assertTrue(hotelOnly.safeFlightNumber.isBlank(), "Sadece otel ürününde uçuş bilgisi olmamalı")
        assertTrue(hotelOnly.safeHotelName.isNotBlank(), "Sadece otel ürününde otel adı bulunmalı")
    }

    // =========================================================================
    // b) Yolcu / Oda Çarpanı (calculateMultiplier)
    // =========================================================================
    @Test
    fun testCalculateMultiplierForPackageTours() {
        // Standart paket tur fiyatı 2 yetişkin baz alınarak hesaplanır (totalWeight / 2.0)

        // 2 Yetişkin (Baz fiyat: 1.0 çarpanı)
        val mult2Adults = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = emptyList(),
            isFlight = false
        )
        assertEquals(1.0, mult2Adults, 0.001)

        // 1 Yetişkin (Tek kişilik oda: 0.5 çarpanı)
        val mult1Adult = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 1,
            childAgesList = emptyList(),
            isFlight = false
        )
        assertEquals(0.5, mult1Adult, 0.001)

        // 3 Yetişkin (3.0 / 2.0 = 1.5 çarpanı)
        val mult3Adults = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 3,
            childAgesList = emptyList(),
            isFlight = false
        )
        assertEquals(1.5, mult3Adults, 0.001)

        // 2 Yetişkin + 1 Bebek (0-2 yaş: %10 vergi -> (2.0 + 0.10) / 2.0 = 1.05)
        val mult2Adults1Infant = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = listOf(1),
            isFlight = false
        )
        assertEquals(1.05, mult2Adults1Infant, 0.001)

        // 2 Yetişkin + 1 Küçük Çocuk (3-6 yaş: %50 -> (2.0 + 0.50) / 2.0 = 1.25)
        val mult2Adults1SmallChild = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = listOf(5),
            isFlight = false
        )
        assertEquals(1.25, mult2Adults1SmallChild, 0.001)

        // 2 Yetişkin + 1 Büyük Çocuk (7-12 yaş: %70 -> (2.0 + 0.70) / 2.0 = 1.35)
        val mult2Adults1OlderChild = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = listOf(10),
            isFlight = false
        )
        assertEquals(1.35, mult2Adults1OlderChild, 0.001)

        // 2 Yetişkin + 1 Genç (13+ yaş: %100 -> (2.0 + 1.00) / 2.0 = 1.50)
        val mult2Adults1Teen = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = listOf(15),
            isFlight = false
        )
        assertEquals(1.50, mult2Adults1Teen, 0.001)
    }

    @Test
    fun testCalculateMultiplierForFlightOnly() {
        // Uçuşlarda çarpan doğrudan bilet adedi ve ağırlığı toplamıdır (isFlight = true)

        // 1 Yetişkin Uçuş
        val flight1Adult = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 1,
            childAgesList = emptyList(),
            isFlight = true
        )
        assertEquals(1.0, flight1Adult, 0.001)

        // 2 Yetişkin Uçuş
        val flight2Adults = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = emptyList(),
            isFlight = true
        )
        assertEquals(2.0, flight2Adults, 0.001)

        // 1 Yetişkin + 1 Bebek (1 yaş: 1.0 + 0.10 = 1.10)
        val flight1Adult1Infant = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 1,
            childAgesList = listOf(1),
            isFlight = true
        )
        assertEquals(1.10, flight1Adult1Infant, 0.001)

        // 2 Yetişkin + 1 Çocuk (8 yaş: 2.0 + 0.70 = 2.70)
        val flight2Adults1Child = B2BTourSearchViewModel.calculateMultiplier(
            adultsCount = 2,
            childAgesList = listOf(8),
            isFlight = true
        )
        assertEquals(2.70, flight2Adults1Child, 0.001)
    }

    // =========================================================================
    // c) 9 Kanonik Operatör Koruması (TourOperatorConfig)
    // =========================================================================
    @Test
    fun testCanonicalOperatorsListAndStrictProtection() {
        // 1. Liste tam olarak 9 operatör içermelidir
        val allowedList = TourOperatorConfig.ALLOWED_OPERATOR_NAMES
        assertEquals(9, allowedList.size, "İzin verilen kanonik operatör sayısı 9 olmalı")

        val expectedOperators = setOf(
            "Bibloglobus",
            "Anex",
            "Coral Travel",
            "Sunmar",
            "Fun&Sun (Ru)",
            "Kazunion",
            "Loti",
            "Pegas Touristik",
            "Интурист"
        )
        assertEquals(expectedOperators, allowedList.toSet())

        // 2. Her 9 operatör geçerli kabul edilmelidir
        for (op in expectedOperators) {
            assertTrue(TourOperatorConfig.isAllowedOperator(op), "Operatör geçerli olmalı: $op")
            assertEquals(op, TourOperatorConfig.resolveCanonicalOperatorName(op))
        }

        // 3. Tanımsız, geçersiz veya yasaklı operatörler elenmelidir
        assertFalse(TourOperatorConfig.isAllowedOperator("Tez Tour"))
        assertFalse(TourOperatorConfig.isAllowedOperator("Mouzenidis"))
        assertFalse(TourOperatorConfig.isAllowedOperator("Lufthansa Holidays"))
        assertFalse(TourOperatorConfig.isAllowedOperator("FakeOperator123"))
        assertFalse(TourOperatorConfig.isAllowedOperator(""))
        assertFalse(TourOperatorConfig.isAllowedOperator(null))

        assertNull(TourOperatorConfig.resolveCanonicalOperatorName("Tez Tour"))
        assertNull(TourOperatorConfig.resolveCanonicalOperatorName("Bilinmeyen Operatör"))

        // 4. Farklı yazım/alias durumları doğru operatöre çözümlenmeli
        assertEquals("Coral Travel", TourOperatorConfig.resolveCanonicalOperatorName("CORAL TRAVEL"))
        assertEquals("Coral Travel", TourOperatorConfig.resolveCanonicalOperatorName("Корал"))
        assertEquals("Pegas Touristik", TourOperatorConfig.resolveCanonicalOperatorName("пегас туристик"))
        assertEquals("Bibloglobus", TourOperatorConfig.resolveCanonicalOperatorName("Библио Глобус"))
        assertEquals("Anex", TourOperatorConfig.resolveCanonicalOperatorName("anex"))
        assertEquals("Fun&Sun (Ru)", TourOperatorConfig.resolveCanonicalOperatorName("Фан Сан"))
        assertEquals("Интурист", TourOperatorConfig.resolveCanonicalOperatorName("Intourist"))
    }
}
