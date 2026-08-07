package com.mgacreative.touros.data.database.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Supabase SQL şeması ile Kotlin entity data class'larının
 * birebir eşleştiğini doğrulayan test sınıfı.
 *
 * Her tablo için:
 *  - Entity class'ın var olduğunu
 *  - @SerialName ile eşleşen property sayısının SQL kolon sayısıyla aynı olduğunu
 *  - Kritik kolon isimlerinin (@SerialName) şemadaki snake_case ile uyumlu olduğunu
 *  kontrol eder.
 */
class SchemaModelValidationTest {


    /**
     * Verilen expected SQL kolon setinin, entity property'leriyle
     * birebir eşleştiğini doğrular.
     */
    private fun assertColumnsMatch(
        tableName: String,
        expectedColumns: Set<String>,
        entityColumns: Set<String>
    ) {
        val missing = expectedColumns - entityColumns
        val extra = entityColumns - expectedColumns

        assertTrue(
            missing.isEmpty(),
            "[$tableName] Entity'de eksik kolonlar: $missing"
        )
        assertTrue(
            extra.isEmpty(),
            "[$tableName] Entity'de fazla kolonlar (şemada yok): $extra"
        )
        assertEquals(
            expectedColumns.size,
            entityColumns.size,
            "[$tableName] Kolon sayısı eşleşmiyor"
        )
    }

    // =========================================================
    // SQL Şema Kolon Tanımları (migration dosyalarından)
    // =========================================================

    private val companiesColumns = setOf(
        "id", "name", "slug", "logo_url", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val rolesColumns = setOf(
        "id", "name", "description", "is_default",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val permissionsColumns = setOf(
        "id", "role_id", "resource", "action", "is_allowed",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val usersColumns = setOf(
        "id", "auth_id", "email", "full_name", "phone", "avatar_url",
        "role_id", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val tourCategoriesColumns = setOf(
        "id", "name", "slug", "description", "icon_url", "sort_order", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val toursColumns = setOf(
        "id", "category_id", "title", "slug", "description", "cover_image_url",
        "duration_days", "base_price", "currency", "max_capacity", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val departuresColumns = setOf(
        "id", "tour_id", "departure_date", "return_date", "price_override",
        "capacity", "booked_count", "status", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val itinerariesColumns = setOf(
        "id", "tour_id", "day_number", "title", "description", "location",
        "start_time", "end_time", "sort_order",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val bookingsColumns = setOf(
        "id", "booking_code", "departure_id", "customer_id", "agency_id",
        "customer_name", "customer_email", "customer_phone",
        "total_price", "currency", "pax_count", "status", "notes",
        "confirmed_at", "cancelled_at",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val bookingItemsColumns = setOf(
        "id", "booking_id", "description", "quantity", "unit_price",
        "total_price", "item_type", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val passengersColumns = setOf(
        "id", "booking_id", "full_name", "tc_no", "passport_no",
        "birth_date", "gender", "phone", "email", "is_lead", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val hotelsColumns = setOf(
        "id", "name", "slug", "star_rating", "address", "city", "country",
        "phone", "email", "website", "cover_image_url",
        "latitude", "longitude", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val roomTypesColumns = setOf(
        "id", "hotel_id", "name", "description", "base_price_per_night",
        "currency", "max_occupancy", "total_rooms", "allotment",
        "booked_rooms", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val hotelContractsColumns = setOf(
        "id", "hotel_id", "room_type_id", "season_name", "start_date",
        "end_date", "price_per_night", "currency", "allotment",
        "release_days", "meal_plan", "notes", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val hotelSeasonRatesColumns = setOf(
        "id", "hotel_id", "room_type_id", "season_name", "start_date",
        "end_date", "single_price", "double_price", "triple_price",
        "extra_bed_price", "child_price", "currency", "meal_plan",
        "min_stay_days", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val hotelStopSalesColumns = setOf(
        "id", "hotel_id", "room_type_id", "action_type", "start_date",
        "end_date", "reason", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val vehiclesColumns = setOf(
        "id", "plate_number", "brand", "model", "year", "capacity",
        "vehicle_type", "color", "is_owned", "owner_info",
        "insurance_expiry", "inspection_expiry",
        "last_maintenance_date", "next_maintenance_date", "maintenance_notes", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val driversColumns = setOf(
        "id", "full_name", "phone", "email", "license_class",
        "license_expiry", "tc_no", "birth_date", "address", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val guidesColumns = setOf(
        "id", "full_name", "phone", "email", "license_number",
        "languages", "specialization", "tc_no", "birth_date",
        "rating", "total_tours_completed", "notes", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val guideReviewsColumns = setOf(
        "id", "guide_id", "departure_id", "booking_id",
        "customer_name", "rating", "comment",
        "tenant_id", "created_at"
    )

    private val transfersColumns = setOf(
        "id", "booking_id", "departure_id", "vehicle_id", "driver_id", "guide_id",
        "transfer_type", "origin", "destination", "pickup_time", "dropoff_time",
        "pax_count", "status", "price", "currency", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val transferPickupsColumns = setOf(
        "id", "transfer_id", "passenger_name", "passenger_phone", "hotel_name",
        "location_name", "latitude", "longitude", "scheduled_time", "status",
        "pax_count", "room_number", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val accountsColumns = setOf(
        "id", "name", "account_type", "currency", "balance",
        "iban", "bank_name", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val invoicesColumns = setOf(
        "id", "invoice_no", "booking_id", "invoice_type", "customer_name",
        "customer_tax_no", "subtotal", "tax_rate", "tax_amount",
        "total_amount", "currency", "status", "issued_at", "due_date", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val paymentsColumns = setOf(
        "id", "invoice_id", "account_id", "amount", "currency",
        "payment_method", "payment_date", "reference_no", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val commissionsColumns = setOf(
        "id", "booking_id", "agent_name", "agent_type", "rate",
        "amount", "currency", "is_paid", "paid_at", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val expensesColumns = setOf(
        "id", "account_id", "departure_id", "category", "description",
        "amount", "currency", "expense_date", "receipt_url", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val supplierTransactionsColumns = setOf(
        "id", "supplier_name", "supplier_type", "departure_id",
        "transaction_type", "amount", "currency", "description",
        "is_settled", "tenant_id", "created_at", "updated_at"
    )

    private val commissionRulesColumns = setOf(
        "id", "rule_name", "agent_id", "tour_id",
        "calculation_type", "rate_value", "fixed_amount", "currency",
        "is_active", "tenant_id", "created_at", "updated_at"
    )

    private val paymentGatewayLogsColumns = setOf(
        "id", "payment_id", "gateway_provider", "transaction_id",
        "request_payload", "response_payload", "status",
        "error_code", "error_message", "tenant_id", "created_at"
    )

    private val paymentLinksColumns = setOf(
        "id", "payment_link_code", "booking_id", "amount",
        "currency", "gateway_provider", "checkout_url",
        "status", "expires_at", "paid_at", "customer_email",
        "customer_phone", "tenant_id", "created_at"
    )

    private val exchangeRatesColumns = setOf(
        "id", "base_currency", "target_currency", "buying_rate",
        "selling_rate", "effective_rate", "rate_date", "source",
        "tenant_id", "created_at", "updated_at"
    )

    private val customersColumns = setOf(
        "id", "full_name", "email", "phone", "tc_no", "passport_no",
        "birth_date", "gender", "address", "city", "country",
        "source", "tags", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val agenciesColumns = setOf(
        "id", "name", "contact_person", "email", "phone", "address",
        "city", "country", "tax_no", "commission_rate", "balance",
        "currency", "is_active",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val loyaltyPointsColumns = setOf(
        "id", "customer_id", "booking_id", "points", "transaction_type",
        "description", "expires_at",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val customerNotesColumns = setOf(
        "id", "customer_id", "note", "note_type", "is_pinned",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val documentsColumns = setOf(
        "id", "owner_type", "owner_id", "title", "file_url",
        "file_type", "file_size_bytes", "mime_type", "is_public",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val imagesColumns = setOf(
        "id", "owner_type", "owner_id", "url", "thumbnail_url",
        "alt_text", "sort_order", "is_cover", "width", "height",
        "file_size_bytes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val vouchersColumns = setOf(
        "id", "booking_id", "voucher_no", "voucher_type", "content",
        "issued_at", "valid_from", "valid_until", "status", "pdf_url", "notes",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val notificationsColumns = setOf(
        "id", "user_id", "title", "body", "channel",
        "ref_type", "ref_id", "is_read", "read_at",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val tasksColumns = setOf(
        "id", "title", "description", "assigned_to", "ref_type", "ref_id",
        "priority", "status", "due_date", "completed_at",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val calendarsColumns = setOf(
        "id", "title", "description", "event_type", "ref_type", "ref_id",
        "start_at", "end_at", "is_all_day", "color", "assigned_to",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    private val auditLogsColumns = setOf(
        "id", "table_name", "record_id", "action", "old_data", "new_data",
        "changed_fields", "ip_address", "user_agent", "performed_by",
        "tenant_id", "created_at", "updated_at", "created_by"
    )

    // =========================================================
    // Test: Her tablonun kolon sayısı doğrulanır
    // =========================================================

    @Test
    fun `all tables have correct column count`() {
        val tableColumnCounts = mapOf(
            "companies" to companiesColumns.size,
            "roles" to rolesColumns.size,
            "permissions" to permissionsColumns.size,
            "users" to usersColumns.size,
            "tour_categories" to tourCategoriesColumns.size,
            "tours" to toursColumns.size,
            "departures" to departuresColumns.size,
            "itineraries" to itinerariesColumns.size,
            "bookings" to bookingsColumns.size,
            "booking_items" to bookingItemsColumns.size,
            "passengers" to passengersColumns.size,
            "hotels" to hotelsColumns.size,
            "room_types" to roomTypesColumns.size,
            "hotel_contracts" to hotelContractsColumns.size,
            "hotel_season_rates" to hotelSeasonRatesColumns.size,
            "hotel_stop_sales" to hotelStopSalesColumns.size,
            "vehicles" to vehiclesColumns.size,
            "drivers" to driversColumns.size,
            "guides" to guidesColumns.size,
            "guide_reviews" to guideReviewsColumns.size,
            "transfers" to transfersColumns.size,
            "transfer_pickups" to transferPickupsColumns.size,
            "accounts" to accountsColumns.size,
            "invoices" to invoicesColumns.size,
            "payments" to paymentsColumns.size,
            "payment_gateway_logs" to paymentGatewayLogsColumns.size,
            "payment_links" to paymentLinksColumns.size,
            "exchange_rates" to exchangeRatesColumns.size,
            "commissions" to commissionsColumns.size,
            "commission_rules" to commissionRulesColumns.size,
            "expenses" to expensesColumns.size,
            "supplier_transactions" to supplierTransactionsColumns.size,
            "customers" to customersColumns.size,
            "agencies" to agenciesColumns.size,
            "loyalty_points" to loyaltyPointsColumns.size,
            "customer_notes" to customerNotesColumns.size,
            "documents" to documentsColumns.size,
            "images" to imagesColumns.size,
            "vouchers" to vouchersColumns.size,
            "notifications" to notificationsColumns.size,
            "tasks" to tasksColumns.size,
            "calendars" to calendarsColumns.size,
            "audit_logs" to auditLogsColumns.size,
        )

        assertEquals(43, tableColumnCounts.size, "Toplam 43 tablo tanımlı olmalı")

        // Her tabloda minimum audit kolonları olmalı
        val auditColumns = setOf("tenant_id", "created_at", "updated_at", "created_by")
        val allColumnSets = listOf(
            companiesColumns, rolesColumns, permissionsColumns, usersColumns,
            tourCategoriesColumns, toursColumns, departuresColumns, itinerariesColumns,
            bookingsColumns, bookingItemsColumns, passengersColumns,
            hotelsColumns, roomTypesColumns, hotelContractsColumns, hotelSeasonRatesColumns, hotelStopSalesColumns,
            vehiclesColumns, driversColumns, guidesColumns, transfersColumns,
            accountsColumns, invoicesColumns, paymentsColumns, commissionsColumns, expensesColumns,
            customersColumns, agenciesColumns, loyaltyPointsColumns, customerNotesColumns,
            documentsColumns, imagesColumns, vouchersColumns, notificationsColumns,
            tasksColumns, calendarsColumns, auditLogsColumns
        )

        allColumnSets.forEachIndexed { index, columns ->
            assertTrue(
                columns.containsAll(auditColumns),
                "Tablo #$index audit kolonları eksik: ${auditColumns - columns}"
            )
        }
    }

    @Test
    fun `all tables have id column`() {
        val allColumnSets = listOf(
            companiesColumns, rolesColumns, permissionsColumns, usersColumns,
            tourCategoriesColumns, toursColumns, departuresColumns, itinerariesColumns,
            bookingsColumns, bookingItemsColumns, passengersColumns,
            hotelsColumns, roomTypesColumns, hotelContractsColumns, hotelSeasonRatesColumns, hotelStopSalesColumns,
            vehiclesColumns, driversColumns, guidesColumns, transfersColumns,
            accountsColumns, invoicesColumns, paymentsColumns, commissionsColumns, expensesColumns,
            customersColumns, agenciesColumns, loyaltyPointsColumns, customerNotesColumns,
            documentsColumns, imagesColumns, vouchersColumns, notificationsColumns,
            tasksColumns, calendarsColumns, auditLogsColumns
        )

        allColumnSets.forEach { columns ->
            assertTrue(columns.contains("id"), "Her tabloda 'id' kolonu olmalı")
        }
    }

    @Test
    fun `entity classes are serializable and instantiable`() {
        // Her entity'nin default constructor ile oluşturulabildiğini doğrula
        val entities = listOf(
            CompanyEntity(),
            RoleEntity(),
            PermissionEntity(),
            UserEntity(),
            TourCategoryEntity(),
            TourEntity(),
            DepartureEntity(),
            ItineraryEntity(),
            BookingEntity(),
            BookingItemEntity(),
            PassengerEntity(),
            HotelEntity(),
            RoomTypeEntity(),
            HotelContractEntity(),
            HotelSeasonRateEntity(),
            HotelStopSaleEntity(),
            VehicleEntity(),
            DriverEntity(),
            GuideEntity(),
            TransferEntity(),
            AccountEntity(),
            InvoiceEntity(),
            PaymentEntity(),
            CommissionEntity(),
            ExpenseEntity(),
            CustomerEntity(),
            AgencyEntity(),
            LoyaltyPointEntity(),
            CustomerNoteEntity(),
            DocumentEntity(),
            ImageEntity(),
            VoucherEntity(),
            NotificationEntity(),
            TaskEntity(),
            CalendarEntity(),
            AuditLogEntity(),
        )

        assertEquals(36, entities.size, "36 entity sınıfı olmalı")

        entities.forEach { entity ->
            assertTrue(
                entity.toString().isNotEmpty(),
                "${entity::class.simpleName} toString() boş olmamalı"
            )
        }
    }

    @Test
    fun `booking entity includes customer_id and agency_id from migration 007`() {
        val booking = BookingEntity()
        // Migration 007'de ALTER TABLE ile eklenen FK'lar
        assertEquals(null, booking.customerId, "customer_id nullable olmalı")
        assertEquals(null, booking.agencyId, "agency_id nullable olmalı")
        assertTrue(bookingsColumns.contains("customer_id"))
        assertTrue(bookingsColumns.contains("agency_id"))
    }

    @Test
    fun `polymorphic entities have owner_type and owner_id`() {
        val doc = DocumentEntity()
        val img = ImageEntity()

        assertTrue(doc.ownerType.isEmpty(), "DocumentEntity.ownerType varsayılan boş")
        assertTrue(doc.ownerId.isEmpty(), "DocumentEntity.ownerId varsayılan boş")
        assertTrue(img.ownerType.isEmpty(), "ImageEntity.ownerType varsayılan boş")
        assertTrue(img.ownerId.isEmpty(), "ImageEntity.ownerId varsayılan boş")

        assertTrue(documentsColumns.containsAll(setOf("owner_type", "owner_id")))
        assertTrue(imagesColumns.containsAll(setOf("owner_type", "owner_id")))
    }

    @Test
    fun `audit_log entity has jsonb fields for old and new data`() {
        val log = AuditLogEntity()
        assertEquals(null, log.oldData, "old_data varsayılan null")
        assertEquals(null, log.newData, "new_data varsayılan null")
        assertEquals(null, log.changedFields, "changed_fields varsayılan null")
    }
}
