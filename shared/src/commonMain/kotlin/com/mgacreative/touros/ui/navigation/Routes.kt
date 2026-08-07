package com.mgacreative.touros.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route tanımları.
 * kotlinx.serialization ile NavHost'ta kullanılır.
 */

@Serializable
object SplashRoute

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object ForgotPasswordRoute

@Serializable
object OnboardingRoute

@Serializable
data class EmailVerificationRoute(val email: String)

@Serializable
object InviteUserRoute

@Serializable
object UserListRoute

@Serializable
object PermissionMatrixRoute

@Serializable
object AssignedTasksRoute

@Serializable
data class TaskDetailRoute(val taskId: String)

@Serializable
object DashboardRoute

// Faz 1'de eklenecek route'lar
@Serializable
object ToursRoute

@Serializable
object BookingsRoute

@Serializable
object CustomersRoute

@Serializable
object ReportsRoute

@Serializable
object SettingsRoute

@Serializable
data class TourDetailRoute(val tourId: String)

@Serializable
data class TourFormRoute(val tourId: String? = null)

@Serializable
data class TourMediaGalleryRoute(val tourId: String)

@Serializable
data class BookingDetailRoute(val bookingId: String)

@Serializable
object CreateBookingStep1Route

@Serializable
object CreateBookingStep2Route


