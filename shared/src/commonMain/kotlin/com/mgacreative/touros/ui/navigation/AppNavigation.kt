package com.mgacreative.touros.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.ui.screens.CompanySettingsScreen
import com.mgacreative.touros.ui.screens.DashboardScreen
import com.mgacreative.touros.ui.screens.EmailVerificationScreen
import com.mgacreative.touros.ui.screens.ForgotPasswordScreen
import com.mgacreative.touros.ui.screens.LoginScreen
import com.mgacreative.touros.ui.screens.RegisterScreen
import com.mgacreative.touros.ui.screens.SplashScreen

/**
 * Ana uygulama navigasyon grafiği.
 * 
 * Akış: Splash → Login / Register / ForgotPassword / EmailVerification → Dashboard (rol bazlı)
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { role ->
                    val destination = getStartDestinationForRole(role)
                    navController.navigate(destination) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = getStartDestinationForRole(role)
                    navController.navigate(destination) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(ForgotPasswordRoute)
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    val destination = getStartDestinationForRole(role)
                    navController.navigate(destination) {
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(ForgotPasswordRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<EmailVerificationRoute> { backStackEntry ->
            val route: EmailVerificationRoute = backStackEntry.toRoute()
            EmailVerificationScreen(
                userEmail = route.email,
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(EmailVerificationRoute(route.email)) { inclusive = true }
                    }
                }
            )
        }

        composable<InviteUserRoute> {
            com.mgacreative.touros.ui.screens.InviteUserScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<UserListRoute> {
            com.mgacreative.touros.ui.screens.UserListScreen(
                onNavigateToInviteUser = { navController.navigate(InviteUserRoute) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<PermissionMatrixRoute> {
            com.mgacreative.touros.ui.screens.PermissionMatrixScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AssignedTasksRoute> {
            com.mgacreative.touros.ui.screens.AssignedTasksScreen(
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(TaskDetailRoute(taskId))
                }
            )
        }

        composable<TourFormRoute> { backStackEntry ->
            val route: TourFormRoute = backStackEntry.toRoute()
            com.mgacreative.touros.ui.screens.TourFormScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ToursRoute> {
            com.mgacreative.touros.ui.screens.TourListScreen(
                onNavigateToCreateTour = { navController.navigate(TourFormRoute()) },
                onNavigateToEditTour = { id -> navController.navigate(TourDetailRoute(id)) }
            )
        }

        composable<TourDetailRoute> { backStackEntry ->
            val route: TourDetailRoute = backStackEntry.toRoute()
            com.mgacreative.touros.ui.screens.TourDetailScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(TourFormRoute(id)) },
                onNavigateToMediaGallery = { id -> navController.navigate(TourMediaGalleryRoute(id)) }
            )
        }

        composable<TourMediaGalleryRoute> { backStackEntry ->
            val route: TourMediaGalleryRoute = backStackEntry.toRoute()
            com.mgacreative.touros.ui.screens.TourMediaGalleryScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(DashboardRoute) { inclusive = true }
                    }
                }
            )
        }



        composable<CreateBookingStep1Route> {
            com.mgacreative.touros.ui.screens.CreateBookingStep1Screen(
                onNavigateBack = { navController.popBackStack() },
                onCompleteStep1 = { navController.navigate(CreateBookingStep2Route) }
            )
        }

        composable<CreateBookingStep2Route> {
            com.mgacreative.touros.ui.screens.CreateBookingStep2Screen(
                onNavigateBack = { navController.popBackStack() },
                onBookingCreatedSuccess = { bookingId ->
                    navController.popBackStack(DashboardRoute, false)
                }
            )
        }

        composable<BookingsRoute> {
            com.mgacreative.touros.ui.screens.BookingListScreen(
                onNavigateToCreateBooking = { navController.navigate(CreateBookingStep1Route) },
                onNavigateToBookingDetail = { bookingId -> navController.navigate(BookingDetailRoute(bookingId)) }
            )
        }

        composable<BookingDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BookingDetailRoute>()
            com.mgacreative.touros.ui.screens.BookingDetailScreen(
                bookingId = route.bookingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CustomersRoute> {
            // CustomersScreen()
        }

        composable<ReportsRoute> {
            // ReportsScreen()
        }

        composable<SettingsRoute> {
            CompanySettingsScreen()
        }
    }
}
