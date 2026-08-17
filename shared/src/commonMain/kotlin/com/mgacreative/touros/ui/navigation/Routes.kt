package com.mgacreative.touros.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route tanımları.
 * kotlinx.serialization ile NavHost'ta kullanılır.
 */

// ─── Auth ────────────────────────────────────────────────────────────────────
@Serializable object SplashRoute
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object ForgotPasswordRoute
@Serializable object OnboardingRoute
@Serializable data class EmailVerificationRoute(val email: String)

// ─── Core / Yönetim ──────────────────────────────────────────────────────────
@Serializable object DashboardRoute
@Serializable object AgencyApprovalRoute
@Serializable object AgencySearchRoute
@Serializable object AgencyQuotaReportRoute
@Serializable object GlobalWebCmsRoute
@Serializable object GlobalWebPublicRoute
@Serializable object SettingsRoute
@Serializable object UserListRoute
@Serializable object InviteUserRoute
@Serializable object PermissionMatrixRoute
@Serializable object MultiLanguageRoute

// ─── Tur Yönetimi ─────────────────────────────────────────────────────────────
@Serializable object ToursRoute
@Serializable data class TourDetailRoute(val tourId: String)
@Serializable data class TourFormRoute(val tourId: String? = null)
@Serializable data class TourMediaGalleryRoute(val tourId: String)
@Serializable object TourCalendarRoute
@Serializable data class DepartureFormRoute(val tourId: String = "", val departureId: String? = null)

// ─── Rezervasyon ──────────────────────────────────────────────────────────────
@Serializable object BookingsRoute
@Serializable data class BookingDetailRoute(val bookingId: String)
@Serializable object CreateBookingStep1Route
@Serializable object CreateBookingStep2Route

// ─── Otel Yönetimi ────────────────────────────────────────────────────────────
@Serializable object HotelListRoute
@Serializable data class HotelFormRoute(val hotelId: String? = null)
@Serializable data class HotelContractRoute(val hotelId: String = "00000000-0000-0000-0000-000000000001", val hotelName: String = "Otel")
@Serializable data class RoomTypeRoute(val hotelId: String = "00000000-0000-0000-0000-000000000001", val hotelName: String = "Otel")


// ─── Finans ───────────────────────────────────────────────────────────────────
@Serializable object FinancialReportsRoute
@Serializable object InvoiceManagementRoute
@Serializable object CurrentAccountRoute
@Serializable object SupplierExpenseRoute
@Serializable object CurrencyConverterRoute
@Serializable object ExchangeRatesRoute

// ─── Raporlar ─────────────────────────────────────────────────────────────────
@Serializable object ReportsRoute
@Serializable object CustomersRoute
@Serializable object AnalyticsChartsRoute
@Serializable object PerformanceReportsRoute
@Serializable object OperatorCurrentAccountReportRoute
@Serializable object ReportFilterExportRoute

// ─── OTA / Gelişmiş ───────────────────────────────────────────────────────────
@Serializable object OTADashboardRoute
@Serializable data class OTAConnectionDetailRoute(val providerId: String = "viator")
@Serializable data class SyncLogsRoute(val providerIdFilter: String = "ALL")
@Serializable object SmartRecommendationsRoute
@Serializable object CustomerSegmentationRoute
@Serializable object ComplaintTrendRoute
@Serializable object VoucherContractPdfRoute
@Serializable object AgencyOperatorConnectionsRoute
@Serializable object AgencyProductPublishingRoute
@Serializable object AdminProductManagementRoute
@Serializable object SaasCacheManagementRoute

// ─── B2B/B2C Tour Search & Booking Wizard ─────────────────────────────────────
@Serializable object B2BTourSearchDashboardRoute
@Serializable data class B2BTourFlightServiceSelectionRoute(val productId: String = "")
@Serializable data class B2BPassengerCheckoutWizardRoute(val productId: String = "", val flightOptionId: String = "")

