package com.mgacreative.touros.data.database

import com.mgacreative.touros.data.database.entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EntitySchemaValidationTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @Test
    fun testTourEntitySerializationMatchesSupabaseDDL() {
        val tour = TourEntity(
            code = "TUR-001",
            title = "Kapadokya Turu",
            category = "Kültür",
            country = "Türkiye",
            city = "Nevşehir",
            durationDays = 3,
            basePrice = 4500.0,
            capacity = 20
        )
        val encoded = json.encodeToString(tour)
        assertTrue(encoded.contains("TUR-001"))
        assertTrue(encoded.contains("base_price"))
        assertTrue(encoded.contains("duration_days"))
    }

    @Test
    fun testCompanyEntitySerializationMatchesSupabaseDDL() {
        val company = CompanyEntity(
            name = "TourOS Travel",
            slug = "touros-travel",
            tenantId = "00000000-0000-0000-0000-000000000001"
        )
        val encoded = json.encodeToString(company)
        assertTrue(encoded.contains("touros-travel"))
        assertTrue(encoded.contains("tenant_id"))
    }

    @Test
    fun testHotelEntitySerializationMatchesSupabaseDDL() {
        val hotel = HotelEntity(
            name = "Grand Cave Hotel",
            city = "Nevşehir",
            tenantId = "00000000-0000-0000-0000-000000000001"
        )
        val encoded = json.encodeToString(hotel)
        assertTrue(encoded.contains("Grand Cave Hotel"))
        assertTrue(encoded.contains("tenant_id"))
    }

    @Test
    fun testBookingEntitySerializationMatchesSupabaseDDL() {
        val booking = BookingEntity(
            bookingCode = "REZ-2026-001",
            customerName = "Ahmet Yılmaz",
            totalPrice = 9000.0,
            status = "CONFIRMED",
            tenantId = "00000000-0000-0000-0000-000000000001"
        )
        val encoded = json.encodeToString(booking)
        assertTrue(encoded.contains("REZ-2026-001"))
        assertTrue(encoded.contains("CONFIRMED"))
    }
}
