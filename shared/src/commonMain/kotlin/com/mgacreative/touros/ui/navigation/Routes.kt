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
@Serializable object SettingsRoute
@Serializable object UserListRoute
@Serializable object InviteUserRoute
@Serializable object PermissionMatrixRoute
@Serializable object AssignedTasksRoute
@Serializable data class TaskDetailRoute(val taskId: String)
@Serializable object StaffTaskManagementRoute
@Serializable object MultiLanguageRoute
@Serializable object NotificationHubRoute
@Serializable object DocumentManagementRoute
@Serializable object FaqSupportChatRoute
@Serializable object SharedMapRoute

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
@Serializable object BookingPaymentRoute
@Serializable object QRTicketRoute

// ─── Otel Yönetimi ────────────────────────────────────────────────────────────
@Serializable object HotelListRoute
@Serializable data class HotelFormRoute(val hotelId: String? = null)
@Serializable data class HotelContractRoute(val hotelId: String = "00000000-0000-0000-0000-000000000001", val hotelName: String = "Otel")
@Serializable data class RoomTypeRoute(val hotelId: String = "00000000-0000-0000-0000-000000000001", val hotelName: String = "Otel")
@Serializable data class B2CHotelDetailCheckoutRoute(val hotelId: String)


// ─── Operasyon ────────────────────────────────────────────────────────────────
@Serializable object GuideManagementRoute
@Serializable object GuideAssignmentRoute
@Serializable object GuideMobileRoute
@Serializable object GuideRatingRoute
@Serializable object GuidePerformanceReportRoute
@Serializable object VehicleManagementRoute
@Serializable object VehicleAlertsRoute
@Serializable object TransferAssignmentRoute
@Serializable object DriverPickupListRoute

// ─── Fiyatlandırma ────────────────────────────────────────────────────────────
@Serializable object SeasonPricingMatrixRoute
@Serializable object DynamicPricingRuleEngineRoute
@Serializable object DynamicPricingRuleFormRoute
@Serializable object StopSaleReleaseRoute
@Serializable object CentralPricingHubRoute
@Serializable object AutoRevenueEngineRoute

// ─── Finans ───────────────────────────────────────────────────────────────────
@Serializable object FinancialReportsRoute
@Serializable object InvoiceManagementRoute
@Serializable object CurrentAccountRoute
@Serializable object SupplierExpenseRoute
@Serializable object CommissionRulesRoute
@Serializable object CurrencyConverterRoute
@Serializable object ExchangeRatesRoute
@Serializable object PaymentGatewayRoute
@Serializable object PaymentLinkRoute
@Serializable object PaymentWebhookRoute
@Serializable object CampaignCouponRoute

// ─── Raporlar ─────────────────────────────────────────────────────────────────
@Serializable object ReportsRoute
@Serializable object CustomersRoute
@Serializable object AnalyticsChartsRoute
@Serializable object PerformanceReportsRoute
@Serializable object ReportFilterExportRoute

// ─── B2B ──────────────────────────────────────────────────────────────────────
@Serializable object B2BAgencyAuthRoute
@Serializable object B2BAgencyBookingRoute
@Serializable object B2BAgencyCommissionsRoute
@Serializable object B2BAgencyPrivateReportsRoute
@Serializable object B2BAgencyVouchersRoute

// ─── B2C ──────────────────────────────────────────────────────────────────────
@Serializable object B2CTourSearchRoute
@Serializable data class B2CTourDetailCheckoutRoute(val tourId: String = "")
@Serializable object B2CLiveLocationRoute
@Serializable object B2CNotificationsReviewRoute
@Serializable object B2CReleaseDesignRoute
@Serializable object B2CVoucherFavoritesRoute

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
@Serializable object AgencyStorefrontRoute

