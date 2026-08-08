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
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon

// ─── Auth-only route'ları (shell gizlenir) ────────────────────────────────────
private val authRoutePatterns = listOf(
    "SplashRoute", "LoginRoute", "RegisterRoute",
    "ForgotPasswordRoute", "OnboardingRoute", "EmailVerificationRoute"
)

private fun NavDestination?.isAuthRoute(): Boolean =
    authRoutePatterns.any { this?.route?.contains(it) == true }

// ─── Menü Öğeleri ─────────────────────────────────────────────────────────────
private fun buildNavItems(currentRoute: String?): List<TourOSNavItem> = listOf(
    TourOSNavItem("Dashboard",        { Icon(Icons.Default.Dashboard, contentDescription = null, tint = TourOSColors.Primary) }, DashboardRoute,               isSelected = currentRoute?.contains("DashboardRoute") == true),
    TourOSNavItem("Turlar",           { Icon(Icons.Default.Map, contentDescription = null, tint = TourOSColors.Primary) }, ToursRoute,                   isSelected = currentRoute?.contains("ToursRoute") == true),
    TourOSNavItem("Rezervasyon",      { Icon(Icons.Default.Assignment, contentDescription = null, tint = TourOSColors.Primary) }, BookingsRoute,                isSelected = currentRoute?.contains("BookingsRoute") == true),
    TourOSNavItem("Oteller",          { Icon(Icons.Default.Hotel, contentDescription = null, tint = TourOSColors.Primary) }, HotelListRoute,               isSelected = currentRoute?.contains("HotelListRoute") == true),
    TourOSNavItem("Pazaryeri Bağlantı", { Icon(Icons.Default.Storefront, contentDescription = null, tint = TourOSColors.Primary) }, AgencyOperatorConnectionsRoute, isSelected = currentRoute?.contains("AgencyOperatorConnectionsRoute") == true),
    TourOSNavItem("Ürün Yayınlama",   { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TourOSColors.Primary) }, AgencyProductPublishingRoute, isSelected = currentRoute?.contains("AgencyProductPublishingRoute") == true),
    TourOSNavItem("Acente Web Sayfası", { Icon(Icons.Default.Computer, contentDescription = null, tint = TourOSColors.Primary) }, AgencyStorefrontRoute,      isSelected = currentRoute?.contains("AgencyStorefrontRoute") == true),
    TourOSNavItem("Fiyat & Kampanya", { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = TourOSColors.Primary) }, DynamicPricingRuleEngineRoute, isSelected = currentRoute?.contains("DynamicPricingRuleEngineRoute") == true || currentRoute?.contains("CampaignCouponRoute") == true),
    TourOSNavItem("OTA Hub",          { Icon(Icons.Default.Public, contentDescription = null, tint = TourOSColors.Primary) }, OTADashboardRoute,            isSelected = currentRoute?.contains("OTADashboardRoute") == true || currentRoute?.contains("OTAConnectionDetailRoute") == true),
    TourOSNavItem("Finans",           { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TourOSColors.Primary) }, FinancialReportsRoute,        isSelected = currentRoute?.contains("FinancialReportsRoute") == true),
    TourOSNavItem("Müşteriler & CRM",  { Icon(Icons.Default.Group, contentDescription = null, tint = TourOSColors.Primary) }, CustomerSegmentationRoute,    isSelected = currentRoute?.contains("CustomerSegmentationRoute") == true),
    TourOSNavItem("Analitik & Trend", { Icon(Icons.Default.BarChart, contentDescription = null, tint = TourOSColors.Primary) }, AnalyticsChartsRoute,       isSelected = currentRoute?.contains("AnalyticsChartsRoute") == true || currentRoute?.contains("ComplaintTrendRoute") == true),
    TourOSNavItem("Destek & SSS",     { Icon(Icons.Default.Chat, contentDescription = null, tint = TourOSColors.Primary) }, FaqSupportChatRoute,          isSelected = currentRoute?.contains("FaqSupportChatRoute") == true),
    TourOSNavItem("Ayarlar & Dil",    { Icon(Icons.Default.Settings, contentDescription = null, tint = TourOSColors.Primary) }, SettingsRoute,                isSelected = currentRoute?.contains("SettingsRoute") == true || currentRoute?.contains("MultiLanguageRoute") == true)
)


/**
 * Ana uygulama navigasyon grafiği — tüm 76 ekran bağlı.
 * Sidebar (Expanded ≥ 768dp) / BottomBar (Compact) adaptif shell.
 * NavHost her zaman tek instance olarak mount edilir.
 */
@Composable
fun AppNavigation() {
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
        val navItems = buildNavItems(currentRoute)

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
                onNavigateToMediaGallery = { id -> navController.navigate(TourMediaGalleryRoute(id)) }
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

        composable<DepartureFormRoute> {
            DepartureFormScreen(
                viewModel = koinViewModel(),
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
                onEditHotelClick = { id -> navController.navigate(HotelContractRoute(hotelId = id)) }
            )
        }

        composable<HotelFormRoute> {
            HotelFormScreen(
                viewModel = koinViewModel(),
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
            FinancialReportsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
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

        composable<ReportsRoute> { /* Placeholder */ }
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

        composable<B2CTourDetailCheckoutRoute> {
            B2CTourDetailCheckoutScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
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

        composable<OTADashboardRoute> {
            OTADashboardScreen(
                viewModel = koinViewModel(),
                onNavigateToLogs = { providerId -> navController.navigate(SyncLogsRoute(providerIdFilter = providerId)) }
            )
        }

        composable<AgencyOperatorConnectionsRoute> {
            com.mgacreative.touros.ui.screens.AgencyOperatorConnectionsScreen()
        }

        composable<AgencyProductPublishingRoute> {
            com.mgacreative.touros.ui.screens.AgencyProductPublishingScreen()
        }

        composable<AgencyStorefrontRoute> {
            com.mgacreative.touros.ui.screens.AgencyStorefrontScreen(
                onNavigateToTourDetail = { tourId -> navController.navigate(B2CTourDetailCheckoutRoute(tourId = tourId)) }
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

