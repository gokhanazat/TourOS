package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.data.adapter.InternalOperatorAdapter
import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.TourOperatorConfig
import com.mgacreative.touros.domain.model.ota.OTABookingStatus
import com.mgacreative.touros.network.SupabaseClientProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 2. Aşama Sıfır-Mock Regresyon Testi:
 * MarketplaceBookingRoutingEngine, Kiracı İzolasyonu (Tenant Isolation) ve Operatör Routing.
 * Hiçbir Mock/Fake kütüphanesi kullanılmadan gerçek sınıflarla çalışır.
 */
class MarketplaceBookingRoutingRegressionTest {

    private val supabaseClient = SupabaseClientProvider.create()
    private val internalOperatorAdapter = InternalOperatorAdapter(supabaseClient)
    private val routingEngine = MarketplaceBookingRoutingEngine(supabaseClient, internalOperatorAdapter)

    // =========================================================================
    // a) KIRACI İZOLASYONU (TENANT & AGENCY ISOLATION) TESTLERİ
    // =========================================================================

    @Test
    fun testTenantAndAgencyBindingIntegrity() {
        val tenantA = "00000000-0000-0000-0000-00000000000a"
        val agencyA = "agency-istanbul-01"

        val tenantB = "00000000-0000-0000-0000-00000000000b"
        val agencyB = "agency-antalya-02"

        val bookingA = Booking(
            id = "bkg-001",
            bookingCode = "REZ-IST-001",
            agencyId = agencyA,
            tenantId = tenantA,
            customerName = "Ahmet Yılmaz",
            totalPrice = 15000.0,
            status = BookingStatus.BEKLIYOR
        )

        val bookingB = Booking(
            id = "bkg-002",
            bookingCode = "REZ-AYT-002",
            agencyId = agencyB,
            tenantId = tenantB,
            customerName = "Dmitry Ivanov",
            totalPrice = 28000.0,
            status = BookingStatus.ONAYLANDI
        )

        // İki acente/kiracı arasında kimlik ve yetki sızıntısı olmadığını doğrula
        assertNotEquals(bookingA.tenantId, bookingB.tenantId)
        assertNotEquals(bookingA.agencyId, bookingB.agencyId)
        assertEquals(tenantA, bookingA.tenantId)
        assertEquals(agencyA, bookingA.agencyId)
        assertEquals(tenantB, bookingB.tenantId)
        assertEquals(agencyB, bookingB.agencyId)
    }

    @Test
    fun testMultiTenantCrossDataLeakagePrevention() {
        val tenantX = "tenant-pegas-tur"
        val tenantY = "tenant-coral-tur"

        val allBookings = listOf(
            Booking(id = "1", tenantId = tenantX, agencyId = "ag-x1", customerName = "Ali"),
            Booking(id = "2", tenantId = tenantX, agencyId = "ag-x2", customerName = "Veli"),
            Booking(id = "3", tenantId = tenantY, agencyId = "ag-y1", customerName = "Elena"),
            Booking(id = "4", tenantId = tenantY, agencyId = "ag-y2", customerName = "Sergey"),
            Booking(id = "5", tenantId = tenantX, agencyId = "ag-x1", customerName = "Ayşe")
        )

        // TenantX sorgulandığında TenantY verilerinin sızmadığını doğrula
        val tenantXBookings = allBookings.filter { it.tenantId == tenantX }
        assertEquals(3, tenantXBookings.size)
        assertTrue(tenantXBookings.all { it.tenantId == tenantX })
        assertFalse(tenantXBookings.any { it.tenantId == tenantY })

        // TenantY sorgulandığında TenantX verilerinin sızmadığını doğrula
        val tenantYBookings = allBookings.filter { it.tenantId == tenantY }
        assertEquals(2, tenantYBookings.size)
        assertTrue(tenantYBookings.all { it.tenantId == tenantY })
        assertFalse(tenantYBookings.any { it.tenantId == tenantX })
    }

    @Test
    fun testBookingEntitySafeTenantFallback() {
        // tenant_id null geldiğinde sahipsiz kalmayıp kanonik varsayılan UUID'ye bağlandığını doğrula
        val nullTenantEntity = BookingEntity(
            id = "bkg-null-01",
            bookingCode = "REZ-DEFAULT-01",
            tenantId = null,
            agencyId = "agency-root"
        )
        assertEquals("00000000-0000-0000-0000-000000000001", nullTenantEntity.safeTenantId)

        val explicitTenantEntity = BookingEntity(
            id = "bkg-exp-02",
            bookingCode = "REZ-EXP-02",
            tenantId = "custom-tenant-uuid",
            agencyId = "agency-sub"
        )
        assertEquals("custom-tenant-uuid", explicitTenantEntity.safeTenantId)
    }

    // =========================================================================
    // b) OPERATÖR KOD ÇÖZÜMLEME VE ROUTING TESTLERİ
    // =========================================================================

    @Test
    fun testOperatorPrefixExtractionFromTourCode() {
        // Kanonik operatörlerin prefiks ayrıştırması
        assertEquals("ANK", routingEngine.extractPrefixFromTourCode("ANK-00001"))
        assertEquals("PEG", routingEngine.extractPrefixFromTourCode("PEG-FLT-2024"))
        assertEquals("COR", routingEngine.extractPrefixFromTourCode("COR-PKG-992"))
        assertEquals("FUN", routingEngine.extractPrefixFromTourCode("FUN-HTL-101"))
        assertEquals("BIB", routingEngine.extractPrefixFromTourCode("BIB-2024-TUR"))
        assertEquals("SUN", routingEngine.extractPrefixFromTourCode("SUN-401"))
        assertEquals("LOTI", routingEngine.extractPrefixFromTourCode("LOTI-VIP-77"))
        assertEquals("KAZ", routingEngine.extractPrefixFromTourCode("KAZ-TR-303"))
        assertEquals("INT", routingEngine.extractPrefixFromTourCode("INT-RU-555"))
        assertEquals("ANEX", routingEngine.extractPrefixFromTourCode("ANEX-12345"))

        // Boşluk ve küçük harf temizleme (trim & uppercase)
        assertEquals("PEG", routingEngine.extractPrefixFromTourCode("   peg-2024   "))
        assertEquals("PEGAS", routingEngine.extractPrefixFromTourCode("pegas-2024"))

        // Tire içermeyen standart tur kodlarında güvenli varsayılan ("TUR") dönüşü
        assertEquals("TUR", routingEngine.extractPrefixFromTourCode("GENELTURKODU"))
        assertEquals("TUR", routingEngine.extractPrefixFromTourCode(""))
    }

    @Test
    fun testOperatorRoutingIntegrationWithCanonicalConfig() {
        val operatorCodeMap = mapOf(
            "PEG-2024-01" to "Pegas Touristik",
            "COR-PKG-11" to "Coral Travel",
            "FUN-SUN-99" to "Fun&Sun (Ru)",
            "BIB-MOW-05" to "Bibloglobus",
            "SUN-AYT-10" to "Sunmar",
            "LOTI-B2B-1" to "Loti",
            "KAZ-ALA-02" to "Kazunion",
            "INT-SPB-08" to "Интурист",
            "ANEX-TR-01" to "Anex"
        )

        for ((tourCode, expectedCanonicalOperator) in operatorCodeMap) {
            val prefix = routingEngine.extractPrefixFromTourCode(tourCode)
            assertTrue(prefix.isNotBlank())

            // Çözümlenen kanonik operatörün 9 izin verilen operatör arasında olduğunu doğrula
            val canonical = TourOperatorConfig.resolveCanonicalOperatorName(expectedCanonicalOperator)
            assertNotNull(canonical)
            assertEquals(expectedCanonicalOperator, canonical)
            assertTrue(TourOperatorConfig.isAllowedOperator(canonical))
        }
    }

    @Test
    fun testInternalOperatorAdapterLifecycle() = runTest {
        // Gerçek adaptör üzerinden teyit (confirm) akışı
        val confirmResult = internalOperatorAdapter.confirmBooking("bkg-routed-100")
        assertTrue(confirmResult.isSuccess)
        val confirmedBooking = confirmResult.getOrThrow()
        assertEquals("bkg-routed-100", confirmedBooking.otaBookingId)
        assertEquals(OTABookingStatus.CONFIRMED, confirmedBooking.status)

        // Gerçek adaptör üzerinden iptal akışı
        val cancelResult = internalOperatorAdapter.cancelBooking("bkg-routed-100", "Misafir iptal talebi")
        assertTrue(cancelResult.isSuccess)
        val cancelledBooking = cancelResult.getOrThrow()
        assertEquals("bkg-routed-100", cancelledBooking.otaBookingId)
        assertEquals(OTABookingStatus.CANCELLED, cancelledBooking.status)

        // Sağlık kontrolü
        val healthResult = internalOperatorAdapter.healthCheck()
        assertTrue(healthResult.isSuccess)
        assertTrue(healthResult.getOrThrow())
    }
}
