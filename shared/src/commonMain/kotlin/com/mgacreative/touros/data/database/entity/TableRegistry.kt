package com.mgacreative.touros.data.database.entity

/**
 * Supabase tablo adı ↔ Kotlin Entity class eşleşme registry'si.
 * Postgrest sorgularında tablo adı olarak kullanılır.
 */
object TableRegistry {

    /**
     * Tüm tablo adı → entity class eşleşmeleri.
     * Supabase Postgrest `from("tablo_adı")` çağrılarında kullanılır.
     */
    val tables: Map<String, String> = mapOf(
        // Core
        "companies"        to "CompanyEntity",
        "roles"            to "RoleEntity",
        "permissions"      to "PermissionEntity",
        "users"            to "UserEntity",

        // Tour
        "tour_categories"  to "TourCategoryEntity",
        "tours"            to "TourEntity",
        "departures"       to "DepartureEntity",
        "itineraries"      to "ItineraryEntity",

        // Booking
        "bookings"         to "BookingEntity",
        "booking_items"    to "BookingItemEntity",
        "passengers"       to "PassengerEntity",

        // Hotel
        "hotels"           to "HotelEntity",
        "room_types"       to "RoomTypeEntity",
        "hotel_contracts"  to "HotelContractEntity",

        // Operations
        "vehicles"         to "VehicleEntity",
        "drivers"          to "DriverEntity",
        "guides"           to "GuideEntity",
        "transfers"        to "TransferEntity",

        // Finance
        "accounts"         to "AccountEntity",
        "invoices"         to "InvoiceEntity",
        "payments"         to "PaymentEntity",
        "commissions"      to "CommissionEntity",
        "expenses"         to "ExpenseEntity",

        // CRM
        "customers"        to "CustomerEntity",
        "agencies"         to "AgencyEntity",
        "loyalty_points"   to "LoyaltyPointEntity",
        "customer_notes"   to "CustomerNoteEntity",

        // Support
        "documents"        to "DocumentEntity",
        "images"           to "ImageEntity",
        "vouchers"         to "VoucherEntity",
        "notifications"    to "NotificationEntity",
        "tasks"            to "TaskEntity",
        "calendars"        to "CalendarEntity",
        "audit_logs"       to "AuditLogEntity",
    )

    /** Toplam tablo sayısı */
    val totalCount: Int get() = tables.size
}
