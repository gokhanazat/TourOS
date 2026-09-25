package com.mgacreative.touros.utils

import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.domain.model.TourOperatorConfig
import com.mgacreative.touros.ui.screens.toPublicHotelOffer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 3. Aşama Sıfır-Mock Regresyon Testi:
 * DateUtils Tarih Dönüşümleri, Gelecek Tarih Sınır Filtreleri ve UnifiedProductEntity Veri Dayanıklılığı (Resilience).
 * Hiçbir Mock/Fake kütüphanesi kullanılmadan doğrudan gerçek fonksiyonlar ve veri modelleri üzerinden çalışır.
 */
class DateUtilsAndProductResilienceRegressionTest {

    private val jsonHelper = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // =========================================================================
    // a) TARİH FORMATLAMA VE SINIR KONTROLLERİ (DATEUTILS TESTLERİ)
    // =========================================================================

    @Test
    fun testDateUtilsIsoAndDotFormattingIntegrity() {
        val todayIso = DateUtils.getTodayIso()
        val todayDot = DateUtils.getTodayDot()

        // ISO format kontrolü: YYYY-MM-DD
        val isoRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        assertTrue(
            isoRegex.matches(todayIso),
            "getTodayIso() çıktısı '$todayIso' YYYY-MM-DD formatında olmalıdır."
        )

        // Noktalı format kontrolü: DD.MM.YYYY
        val dotRegex = Regex("""^\d{2}\.\d{2}\.\d{4}$""")
        assertTrue(
            dotRegex.matches(todayDot),
            "getTodayDot() çıktısı '$todayDot' DD.MM.YYYY formatında olmalıdır."
        )

        // Tarih bileşenlerinin saat dilimi veya saat bilgisi içermediğini doğrula
        assertFalse(todayIso.contains("T"), "ISO tarih saat dilimi veya 'T' karakteri içermemelidir.")
        assertFalse(todayIso.contains(":"), "ISO tarih saat/dakika/saniye içermemelidir.")
        assertFalse(todayDot.contains(" "), "Noktalı tarih boşluk içermemelidir.")

        // Gün, Ay, Yıl uyumu
        val todayTriple = DateUtils.getToday()
        val expectedIsoDay = todayTriple.first.toString().padStart(2, '0')
        val expectedIsoMonth = todayTriple.second.toString().padStart(2, '0')
        val expectedIsoYear = todayTriple.third.toString()

        assertEquals("$expectedIsoYear-$expectedIsoMonth-$expectedIsoDay", todayIso)
        assertEquals("$expectedIsoDay.$expectedIsoMonth.$expectedIsoYear", todayDot)
    }

    @Test
    fun testSmartDateInputFormattingResilience() {
        // Çeşitli ayraçlı formatların standart DD.MM.YYYY formatına normalize edilmesi
        assertEquals("13.04.1960", DateUtils.formatDateInput("13/4/1960"))
        assertEquals("13.04.1960", DateUtils.formatDateInput("13.4.1960"))
        assertEquals("01.01.1985", DateUtils.formatDateInput("1/1/1985"))
        assertEquals("15.08.2026", DateUtils.formatDateInput("15-8-2026"))
        assertEquals("20.10.2024", DateUtils.formatDateInput("20 10 2024"))

        // Ayraçsız ham rakam girişleri (otomatik nokta ekleme)
        assertEquals("13.04.1960", DateUtils.formatDateInput("13041960"))
        assertEquals("01.01.1990", DateUtils.formatDateInput("01011990"))

        // Boş veya sınır girişleri
        assertEquals("", DateUtils.formatDateInput(""))
        assertEquals("", DateUtils.formatDateInput("   "))
    }

    @Test
    fun testMonthDaysAndLeapYearBoundaries() {
        // 31 Çeken Aylar
        assertEquals(31, DateUtils.getDaysInMonth(1, 2026))  // Ocak
        assertEquals(31, DateUtils.getDaysInMonth(3, 2026))  // Mart
        assertEquals(31, DateUtils.getDaysInMonth(5, 2026))  // Mayıs
        assertEquals(31, DateUtils.getDaysInMonth(7, 2026))  // Temmuz
        assertEquals(31, DateUtils.getDaysInMonth(8, 2026))  // Ağustos
        assertEquals(31, DateUtils.getDaysInMonth(10, 2026)) // Ekim
        assertEquals(31, DateUtils.getDaysInMonth(12, 2026)) // Aralık

        // 30 Çeken Aylar
        assertEquals(30, DateUtils.getDaysInMonth(4, 2026))  // Nisan
        assertEquals(30, DateUtils.getDaysInMonth(6, 2026))  // Haziran
        assertEquals(30, DateUtils.getDaysInMonth(9, 2026))  // Eylül
        assertEquals(30, DateUtils.getDaysInMonth(11, 2026)) // Kasım

        // Şubat ve Artık Yıl (Leap Year) Sınırları
        assertEquals(29, DateUtils.getDaysInMonth(2, 2024)) // 2024 artık yıl -> 29
        assertEquals(28, DateUtils.getDaysInMonth(2, 2025)) // 2025 normal yıl -> 28
        assertEquals(28, DateUtils.getDaysInMonth(2, 2026)) // 2026 normal yıl -> 28
        assertEquals(29, DateUtils.getDaysInMonth(2, 2000)) // 2000 400'e bölünür -> 29
        assertEquals(28, DateUtils.getDaysInMonth(2, 1900)) // 1900 100'e bölünür 400'e bölünmez -> 28
    }

    // =========================================================================
    // b) GELECEK TARİH FİLTRE MANTIĞI (CURRENT_DATE + 7 GÜN)
    // =========================================================================

    @Test
    fun testFutureDateFilterExcludesPastAndNearTours() {
        // GlobalWebPublicScreen ve B2B Arama mantığında kullanılan CURRENT_DATE + 7 gün eşiği
        val minProductDate = DateUtils.getFutureIso(7)

        val products = listOf(
            UnifiedProductEntity(id = "1", tourName = "Geçmiş Tur 1", departureDate = "2023-05-10"),
            UnifiedProductEntity(id = "2", tourName = "Geçmiş Tur 2", departureDate = "2024-12-31"),
            UnifiedProductEntity(id = "3", tourName = "Bugünkü Tur", departureDate = DateUtils.getTodayIso()),
            UnifiedProductEntity(id = "4", tourName = "3 Gün Sonraki Tur", departureDate = DateUtils.getFutureIso(3)),
            UnifiedProductEntity(id = "5", tourName = "6 Gün Sonraki Tur", departureDate = DateUtils.getFutureIso(6)),
            UnifiedProductEntity(id = "6", tourName = "Tam 7 Gün Sonraki Tur", departureDate = DateUtils.getFutureIso(7)),
            UnifiedProductEntity(id = "7", tourName = "14 Gün Sonraki Tur", departureDate = DateUtils.getFutureIso(14)),
            UnifiedProductEntity(id = "8", tourName = "30 Gün Sonraki Tur", departureDate = DateUtils.getFutureIso(30)),
            UnifiedProductEntity(id = "9", tourName = "Tarihsiz Tur", departureDate = null)
        )

        // Filtre mantığı: departure_date >= minProductDate
        val validFutureTours = products.filter { product ->
            val dep = product.departureDate
            !dep.isNullOrBlank() && dep >= minProductDate
        }

        // 7, 14 ve 30 gün sonraki turlar dahil edilmeli; geçmiş, bugün ve <7 gün turlar elenmeli
        val validIds = validFutureTours.map { it.id }.toSet()
        assertEquals(setOf("6", "7", "8"), validIds)
        assertFalse(validIds.contains("1"), "2023 yılı turu elenmelidir.")
        assertFalse(validIds.contains("2"), "2024 yılı turu elenmelidir.")
        assertFalse(validIds.contains("3"), "Bugünün turu (+0 gün) +7 gün eşiğinde elenmelidir.")
        assertFalse(validIds.contains("4"), "+3 gün turu +7 gün eşiğinde elenmelidir.")
        assertFalse(validIds.contains("5"), "+6 gün turu +7 gün eşiğinde elenmelidir.")
        assertFalse(validIds.contains("9"), "Tarihi null olan tur elenmelidir.")
    }

    // =========================================================================
    // c) VERİ DAYANIKLILIĞI (DATA RESILIENCE VE FALLBACK TESTLERİ)
    // =========================================================================

    @Test
    fun testUnifiedProductEntityNullAndMissingFieldResilience() {
        // Tamamen boş JSON gövdesinden serileştirme dayanıklılığı
        val emptyJson = "{}"
        val parsedFromEmpty = jsonHelper.decodeFromString<UnifiedProductEntity>(emptyJson)

        assertEquals("", parsedFromEmpty.id)
        assertEquals("PACKAGE_TOUR", parsedFromEmpty.safeProductType)
        assertEquals(5, parsedFromEmpty.safeHotelCategory)
        assertEquals(0.0, parsedFromEmpty.safePrice)
        assertEquals("RUB", parsedFromEmpty.safeCurrency)
        assertEquals("", parsedFromEmpty.safePictureUrl)
        assertEquals("", parsedFromEmpty.safeOperatorName)

        // Null görsel ve eksik alan içeren ham model
        val rawCorruptedEntity = UnifiedProductEntity(
            id = "test-corrupt-01",
            tourName = "Dayanıklılık Test Turu",
            price = -10.0, // Geçersiz fiyat
            operatorName = "", // Boş operatör
            hotelName = "", // Boş otel adı
            hotelCategory = 0,
            roomType = "",
            mealType = "",
            pictureUrl = null,
            picture = null,
            country = "",
            countryCode = "",
            region = ""
        )

        // Safe getter'ların NPE vermediğini doğrula
        assertEquals("", rawCorruptedEntity.safePictureUrl)
        assertEquals("", rawCorruptedEntity.safeOperatorName)
        assertEquals("", rawCorruptedEntity.safeHotelName)

        // toPublicHotelOffer() dönüştürücüsünün güvenli varsayılanlarla çalıştığını doğrula
        val offer = rawCorruptedEntity.toPublicHotelOffer()
        assertNotNull(offer)
        assertEquals("test-corrupt-01", offer.id)
        assertEquals(100.0, offer.minPrice, "Negatif fiyat taban 100.0 değerine coerce edilmelidir.")
        assertEquals("TourVisor", offer.operatorName, "Boş operatör 'TourVisor' varsayılanına düşmelidir.")
        assertEquals("standard room", offer.roomType, "Boş oda tipi 'standard room' varsayılanına düşmelidir.")
        assertEquals("Bez pitaniya", offer.mealType, "Boş yemek tipi 'Bez pitaniya' varsayılanına düşmelidir.")
        assertEquals("TR", offer.countryCode, "Tanımsız ülke varsayılan 'TR' olmalıdır.")
        assertTrue(offer.location.contains("Турция"), "Tanımsız ülke adı lokasyonda 'Турция' içermelidir.")
        assertEquals("", offer.imageUrl, "Null görsel güvenli şekilde boş string olmalıdır.")
    }

    @Test
    fun testSerializationRoundTripIntegrity() {
        val originalEntity = UnifiedProductEntity(
            id = "tour-roundtrip-99",
            productType = "PACKAGE_TOUR",
            tourName = "Antalya All Inclusive",
            operatorId = 12,
            operatorName = "Coral Travel",
            price = 45000.0,
            currency = "RUB",
            hotelId = 882,
            hotelName = "Rixos Premium Belek",
            hotelCategory = 5,
            country = "Турция",
            countryCode = "TR",
            region = "Antalya",
            departureDate = "2026-10-15",
            nights = 7,
            adults = 2,
            childs = 1,
            pictureUrl = "https://images.touros.com/hotels/rixos.jpg",
            isInstantConfirmation = true
        )

        val jsonString = jsonHelper.encodeToString(originalEntity)
        assertTrue(jsonString.contains("tour-roundtrip-99"))
        assertTrue(jsonString.contains("Coral Travel"))

        val deserializedEntity = jsonHelper.decodeFromString<UnifiedProductEntity>(jsonString)
        assertEquals(originalEntity.id, deserializedEntity.id)
        assertEquals(originalEntity.operatorName, deserializedEntity.operatorName)
        assertEquals(originalEntity.price, deserializedEntity.price)
        assertEquals(originalEntity.pictureUrl, deserializedEntity.safePictureUrl)
        assertEquals(originalEntity.departureDate, deserializedEntity.departureDate)
    }
}
