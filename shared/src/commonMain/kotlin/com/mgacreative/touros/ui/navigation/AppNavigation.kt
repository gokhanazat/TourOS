package com.mgacreative.touros.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mgacreative.touros.ui.components.TourOSBottomBar
import com.mgacreative.touros.ui.components.TourOSNavItem
import com.mgacreative.touros.ui.components.TourOSSidebar
import com.mgacreative.touros.ui.screens.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.viewmodel.DepartureFormViewModel
import com.mgacreative.touros.ui.viewmodel.HotelFormViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.collectAsState
import com.mgacreative.touros.ui.localization.AppLanguageManager

// ─── Auth-only route'ları (shell gizlenir) ────────────────────────────────────
private val authRoutePatterns = listOf(
    "SplashRoute", "LoginRoute", "RegisterRoute",
    "ForgotPasswordRoute", "OnboardingRoute", "EmailVerificationRoute"
)

private fun NavDestination?.isAuthRoute(): Boolean =
    authRoutePatterns.any { this?.route?.contains(it) == true }

// ─── Menü Öğeleri ─────────────────────────────────────────────────────────────
private fun buildNavItems(currentRoute: String?): List<TourOSNavItem> = listOf(
    TourOSNavItem(AppLanguageManager.translate("Dashboard"),        DashboardRoute,               isSelected = currentRoute?.contains("DashboardRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Turlar"),           ToursRoute,                   isSelected = currentRoute?.contains("ToursRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Rezervasyon"),      BookingsRoute,                isSelected = currentRoute?.contains("BookingsRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Oteller"),          HotelListRoute,               isSelected = currentRoute?.contains("HotelListRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Tur Operatörleri"), AgencyOperatorConnectionsRoute, isSelected = currentRoute?.contains("AgencyOperatorConnectionsRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Ürünler"),           AgencyProductPublishingRoute, isSelected = currentRoute?.contains("AgencyProductPublishingRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Acente Web Sayfası"), AgencyStorefrontRoute,      isSelected = currentRoute?.contains("AgencyStorefrontRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Fiyat & Kampanya"), DynamicPricingRuleEngineRoute, isSelected = currentRoute?.contains("DynamicPricingRuleEngineRoute") == true || currentRoute?.contains("CampaignCouponRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Finans"),           FinancialReportsRoute,        isSelected = currentRoute?.contains("FinancialReportsRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Fatura Yönetimi"),  InvoiceManagementRoute,       isSelected = currentRoute?.contains("InvoiceManagementRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Cari Hesaplar"),    CurrentAccountRoute,          isSelected = currentRoute?.contains("CurrentAccountRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Gider Girişi"),     SupplierExpenseRoute,         isSelected = currentRoute?.contains("SupplierExpenseRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Müşteriler & CRM"),  CustomerSegmentationRoute,    isSelected = currentRoute?.contains("CustomerSegmentationRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Analitik & Trend"), AnalyticsChartsRoute,       isSelected = currentRoute?.contains("AnalyticsChartsRoute") == true || currentRoute?.contains("ComplaintTrendRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Raporlar"),         ReportsRoute,                 isSelected = currentRoute?.contains("ReportsRoute") == true),
    TourOSNavItem(AppLanguageManager.translate("Ayarlar & Dil"),    SettingsRoute,                isSelected = currentRoute?.contains("SettingsRoute") == true || currentRoute?.contains("MultiLanguageRoute") == true)
)


/**
 * Ana uygulama navigasyon grafiği — tüm 76 ekran bağlı.
 * Sidebar (Expanded ≥ 768dp) / BottomBar (Compact) adaptif shell.
 * NavHost her zaman tek instance olarak mount edilir.
 */
@Composable
fun AppNavigation() {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showShell = !backStackEntry?.destination.isAuthRoute()

    fun navigate(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 768.dp
        val navItems = remember(currentRoute, currentLanguage) { buildNavItems(currentRoute) }

        // Mobil Bottom Bar için "☰ Menü" Butonlu Liste
        val menuDummyRoute = "HamburgerMenuOpen"
        val bottomNavItems = remember(navItems, drawerState.isOpen) {
            val main4 = navItems.take(4)
            val menuBtn = TourOSNavItem(
                title = "Menü ☰",
                icon = { androidx.compose.material3.Text("☰") },
                route = menuDummyRoute,
                isSelected = drawerState.isOpen
            )
            main4 + menuBtn
        }

        if (isExpanded) {
            // ── Expanded: Sidebar sol, içerik sağda ──────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                if (showShell) {
                    TourOSSidebar(
                        items = navItems,
                        onItemSelect = { navigate(it.route) },
                        userName = "Admin",
                        userRole = "Tour Operator"
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    AppNavHost(navController)   // ← TEK NavHost
                }
            }
        } else {
            // ── Compact: Mobil Hamburger Drawer + Bottom Bar + İçerik ─────────
            androidx.compose.material3.ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = showShell,
                drawerContent = {
                    androidx.compose.material3.ModalDrawerSheet(
                        drawerContainerColor = TourOSColors.Surface
                    ) {
                        TourOSSidebar(
                            items = navItems,
                            onItemSelect = { item ->
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navigate(item.route)
                            },
                            userName = "Admin",
                            userRole = "Tour Operator"
                        )
                    }
                }
            ) {
                Scaffold(
                    containerColor = TourOSColors.Surface,
                    bottomBar = {
                        if (showShell) {
                            TourOSBottomBar(
                                items = bottomNavItems,
                                onItemSelect = { item ->
                                    if (item.route == menuDummyRoute) {
                                        coroutineScope.launch {
                                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                        }
                                    } else {
                                        navigate(item.route)
                                    }
                                }
                            )
                        }
                    }
                ) { padding ->


                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        AppNavHost(navController)   // ← TEK NavHost
                    }
                }
            }
        }
    }
}


@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = SplashRoute) {

        // ─── Auth ─────────────────────────────────────────────────────────────

        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) { popUpTo(SplashRoute) { inclusive = true } }
                },
                onNavigateToDashboard = { role ->
                    navController.navigate(getStartDestinationForRole(role)) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(getStartDestinationForRole(role)) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    navController.navigate(getStartDestinationForRole(role)) {
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) { popUpTo(RegisterRoute) { inclusive = true } }
                }
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) { popUpTo(ForgotPasswordRoute) { inclusive = true } }
                }
            )
        }

        composable<EmailVerificationRoute> { back ->
            val route: EmailVerificationRoute = back.toRoute()
            EmailVerificationScreen(
                userEmail = route.email,
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(EmailVerificationRoute(route.email)) { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingRoute> {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(DashboardRoute) { popUpTo(OnboardingRoute) { inclusive = true } }
                }
            )
        }

        // ─── Dashboard ────────────────────────────────────────────────────────

        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) { popUpTo(DashboardRoute) { inclusive = true } }
                }
            )
        }

        // ─── Kullanıcı / Yetki ────────────────────────────────────────────────

        composable<UserListRoute> {
            UserListScreen(
                onNavigateToInviteUser = { navController.navigate(InviteUserRoute) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<InviteUserRoute> {
            InviteUserScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<PermissionMatrixRoute> {
            PermissionMatrixScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<AssignedTasksRoute> {
            AssignedTasksScreen(
                onNavigateToTaskDetail = { id -> navController.navigate(TaskDetailRoute(id)) }
            )
        }

        composable<StaffTaskManagementRoute> {
            StaffTaskManagementScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            CompanySettingsScreen()
        }

        composable<MultiLanguageRoute> {
            MultiLanguageScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NotificationHubRoute> {
            NotificationHubScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<DocumentManagementRoute> {
            DocumentManagementScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<FaqSupportChatRoute> {
            FaqSupportChatScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable<SharedMapRoute> {
            SharedMapScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Tur Yönetimi ─────────────────────────────────────────────────────

        composable<ToursRoute> {
            TourListScreen(
                onNavigateToCreateTour = { navController.navigate(TourFormRoute()) },
                onNavigateToEditTour = { id -> navController.navigate(TourDetailRoute(id)) }
            )
        }

        composable<TourDetailRoute> { back ->
            val route: TourDetailRoute = back.toRoute()
            TourDetailScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(TourFormRoute(id)) },
                onNavigateToMediaGallery = { id -> navController.navigate(TourMediaGalleryRoute(id)) },
                onNavigateToDepartureForm = { tourId, departureId -> navController.navigate(DepartureFormRoute(tourId, departureId)) }
            )
        }

        composable<TourFormRoute> { back ->
            val route: TourFormRoute = back.toRoute()
            TourFormScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<TourMediaGalleryRoute> { back ->
            val route: TourMediaGalleryRoute = back.toRoute()
            TourMediaGalleryScreen(
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<TourCalendarRoute> {
            TourCalendarScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<DepartureFormRoute> { back ->
            val route: DepartureFormRoute = back.toRoute()
            val viewModel: DepartureFormViewModel = koinViewModel()
            androidx.compose.runtime.LaunchedEffect(route.tourId, route.departureId) {
                if (route.tourId.isNotBlank()) {
                    viewModel.updateTourId(route.tourId)
                }
                if (!route.departureId.isNullOrBlank()) {
                    viewModel.loadDeparture(route.departureId)
                }
            }
            DepartureFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Rezervasyon ──────────────────────────────────────────────────────

        composable<BookingsRoute> {
            BookingListScreen(
                onNavigateToCreateBooking = { navController.navigate(CreateBookingStep1Route) },
                onNavigateToBookingDetail = { id -> navController.navigate(BookingDetailRoute(id)) }
            )
        }

        composable<BookingDetailRoute> { back ->
            val route: BookingDetailRoute = back.toRoute()
            BookingDetailScreen(
                bookingId = route.bookingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CreateBookingStep1Route> {
            CreateBookingStep1Screen(
                onNavigateBack = { navController.popBackStack() },
                onCompleteStep1 = { navController.navigate(CreateBookingStep2Route) }
            )
        }

        composable<CreateBookingStep2Route> {
            CreateBookingStep2Screen(
                onNavigateBack = { navController.popBackStack() },
                onBookingCreatedSuccess = { navController.popBackStack(DashboardRoute, false) }
            )
        }

        composable<BookingPaymentRoute> {
            BookingPaymentScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QRTicketRoute> {
            QRTicketScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Otel Yönetimi ────────────────────────────────────────────────────

        composable<HotelListRoute> {
            HotelListScreen(
                viewModel = koinViewModel(),
                onAddHotelClick = { navController.navigate(HotelFormRoute()) },
                onEditHotelClick = { id -> navController.navigate(HotelFormRoute(hotelId = id)) }
            )
        }

        composable<HotelFormRoute> { back ->
            val route: HotelFormRoute = back.toRoute()
            val viewModel: HotelFormViewModel = koinViewModel()
            androidx.compose.runtime.LaunchedEffect(route.hotelId) {
                route.hotelId?.let { viewModel.loadHotelForEdit(it) }
            }
            HotelFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HotelContractRoute> { back ->
            val route: HotelContractRoute = back.toRoute()
            HotelContractScreen(
                viewModel = koinViewModel(),
                hotelId = route.hotelId,
                hotelName = route.hotelName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<RoomTypeRoute> { back ->
            val route: RoomTypeRoute = back.toRoute()
            RoomTypeManagementScreen(
                viewModel = koinViewModel(),
                hotelId = route.hotelId,
                hotelName = route.hotelName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Operasyon (Devre Dışı / URL ile Erişilemez) ────────────────────

        composable<GuideManagementRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<GuideAssignmentRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<GuideMobileRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<GuideRatingRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<GuidePerformanceReportRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<VehicleManagementRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<VehicleAlertsRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<TransferAssignmentRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        composable<DriverPickupListRoute> {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(DashboardRoute) { popUpTo(0) }
            }
        }

        // ─── Fiyatlandırma ────────────────────────────────────────────────────

        composable<SeasonPricingMatrixRoute> {
            SeasonPricingMatrixScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<DynamicPricingRuleEngineRoute> {
            DynamicPricingRuleEngineScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<DynamicPricingRuleFormRoute> {
            DynamicPricingRuleFormScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<StopSaleReleaseRoute> {
            StopSaleReleaseScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<CentralPricingHubRoute> {
            CentralPricingHubScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<AutoRevenueEngineRoute> {
            AutoRevenueEngineScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── Finans ───────────────────────────────────────────────────────────

        composable<FinancialReportsRoute> {
            FinancialReportsScreen(
                viewModel = koinViewModel(),
                onNavigateToInvoice = { navController.navigate(InvoiceManagementRoute) },
                onNavigateToCurrentAccount = { navController.navigate(CurrentAccountRoute) },
                onNavigateToSupplierExpense = { navController.navigate(SupplierExpenseRoute) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<InvoiceManagementRoute> {
            InvoiceManagementScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<CurrentAccountRoute> {
            CurrentAccountScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<SupplierExpenseRoute> {
            SupplierExpenseScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<CommissionRulesRoute> {
            CommissionRulesScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<CurrencyConverterRoute> {
            CurrencyConverterScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<ExchangeRatesRoute> {
            ExchangeRatesScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<PaymentGatewayRoute> {
            PaymentGatewayScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<PaymentLinkRoute> {
            PaymentLinkScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<PaymentWebhookRoute> {
            PaymentWebhookScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<CampaignCouponRoute> {
            CampaignCouponScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── Raporlar ─────────────────────────────────────────────────────────

        composable<ReportsRoute> {
            ReportsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }
        composable<CustomersRoute> { /* Placeholder */ }

        composable<AnalyticsChartsRoute> {
            AnalyticsChartsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<PerformanceReportsRoute> {
            PerformanceReportsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<ReportFilterExportRoute> {
            ReportFilterExportScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── B2B ──────────────────────────────────────────────────────────────

        composable<B2BAgencyAuthRoute> {
            B2BAgencyAuthScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2BAgencyBookingRoute> {
            B2BAgencyBookingScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2BAgencyCommissionsRoute> {
            B2BAgencyCommissionsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2BAgencyPrivateReportsRoute> {
            B2BAgencyPrivateReportsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2BAgencyVouchersRoute> {
            B2BAgencyVouchersScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── B2C ──────────────────────────────────────────────────────────────

        composable<B2CTourSearchRoute> {
            B2CTourSearchScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2CTourDetailCheckoutRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<B2CTourDetailCheckoutRoute>()
            B2CTourDetailCheckoutScreen(
                viewModel = koinViewModel(),
                tourId = route.tourId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<B2CLiveLocationRoute> {
            B2CLiveLocationScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2CNotificationsReviewRoute> {
            B2CNotificationsReviewScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2CReleaseDesignRoute> {
            B2CReleaseDesignScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<B2CVoucherFavoritesRoute> {
            B2CVoucherFavoritesScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── OTA / Gelişmiş ───────────────────────────────────────────────────

        composable<AgencyOperatorConnectionsRoute> {
            com.mgacreative.touros.ui.screens.AgencyOperatorConnectionsScreen()
        }

        composable<AgencyProductPublishingRoute> {
            com.mgacreative.touros.ui.screens.AgencyProductPublishingScreen()
        }

        composable<AgencyStorefrontRoute> {
            com.mgacreative.touros.ui.screens.AgencyStorefrontScreen(
                onNavigateToTourDetail = { tourId -> navController.navigate(B2CTourDetailCheckoutRoute(tourId = tourId)) },
                onNavigateToHotelDetail = { hotelId -> navController.navigate(B2CHotelDetailCheckoutRoute(hotelId = hotelId)) }
            )
        }

        composable<B2CHotelDetailCheckoutRoute> { back ->
            val route: B2CHotelDetailCheckoutRoute = back.toRoute()
            com.mgacreative.touros.ui.screens.B2CHotelDetailCheckoutScreen(
                viewModel = koinViewModel(),
                hotelId = route.hotelId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<OTAConnectionDetailRoute> { back ->
            val route: OTAConnectionDetailRoute = back.toRoute()
            OTAConnectionDetailScreen(
                viewModel = koinViewModel(),
                providerId = route.providerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SyncLogsRoute> { back ->
            val route: SyncLogsRoute = back.toRoute()
            SyncLogsScreen(
                viewModel = koinViewModel(),
                providerIdFilter = route.providerIdFilter,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SmartRecommendationsRoute> {
            SmartRecommendationsScreen(
                onNavigateToTourDetail = { tourId -> navController.navigate(B2CTourDetailCheckoutRoute(tourId = tourId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CustomerSegmentationRoute> {
            CustomerSegmentationScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ComplaintTrendRoute> {
            ComplaintTrendScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<VoucherContractPdfRoute> {
            VoucherContractPdfScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }
    }
}

