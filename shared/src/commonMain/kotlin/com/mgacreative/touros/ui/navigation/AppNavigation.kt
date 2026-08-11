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
import com.mgacreative.touros.domain.repository.AuthRepository
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

import com.mgacreative.touros.ui.components.TourOSNavGroup

// ─── Auth-only route'ları (shell gizlenir) ────────────────────────────────────
private val authRoutePatterns = listOf(
    "SplashRoute", "LoginRoute", "RegisterRoute",
    "ForgotPasswordRoute", "OnboardingRoute", "EmailVerificationRoute"
)

private fun NavDestination?.isAuthRoute(): Boolean =
    authRoutePatterns.any { this?.route?.contains(it) == true }

// ─── Menü Grupları ─────────────────────────────────────────────────────────────
private fun buildNavGroups(currentRoute: String?, isSystemAdmin: Boolean = false): List<TourOSNavGroup> {
    val groups = mutableListOf<TourOSNavGroup>()

    if (isSystemAdmin) {
        groups.add(
            TourOSNavGroup(
                categoryTitle = AppLanguageManager.translate("WEB YÖNETİMİ"),
                items = listOf(
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Web Yönetimi (CMS)"),
                        route = GlobalWebCmsRoute,
                        isSelected = currentRoute?.contains("GlobalWebCmsRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Canlı Web"),
                        route = GlobalWebPublicRoute,
                        isSelected = currentRoute?.contains("GlobalWebPublicRoute") == true
                    )
                )
            )
        )
    }

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("TUR OPERATÖRLERİ"),
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Tur Operatörleri"),
                    route = AgencyOperatorConnectionsRoute,
                    isSelected = currentRoute?.contains("AgencyOperatorConnectionsRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Ürünler"),
                    route = AgencyProductPublishingRoute,
                    isSelected = currentRoute?.contains("AgencyProductPublishingRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Rezervasyonlar"),
                    route = BookingsRoute,
                    isSelected = currentRoute?.contains("BookingsRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("MUHASEBE"),
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Finans"),
                    route = FinancialReportsRoute,
                    isSelected = currentRoute?.contains("FinancialReportsRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Fatura"),
                    route = InvoiceManagementRoute,
                    isSelected = currentRoute?.contains("InvoiceManagementRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Cari Hesap"),
                    route = CurrentAccountRoute,
                    isSelected = currentRoute?.contains("CurrentAccountRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Giderler"),
                    route = SupplierExpenseRoute,
                    isSelected = currentRoute?.contains("SupplierExpenseRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("ANALİTİK"),
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Dashboard"),
                    route = DashboardRoute,
                    isSelected = currentRoute?.contains("DashboardRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Analitik & Trend"),
                    route = AnalyticsChartsRoute,
                    isSelected = currentRoute?.contains("AnalyticsChartsRoute") == true || currentRoute?.contains("ComplaintTrendRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Raporlar"),
                    route = ReportsRoute,
                    isSelected = currentRoute?.contains("ReportsRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Müşteri & CRM"),
                    route = CustomerSegmentationRoute,
                    isSelected = currentRoute?.contains("CustomerSegmentationRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("YEREL"),
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Yerel Tur"),
                    route = ToursRoute,
                    isSelected = currentRoute?.contains("ToursRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Yerel Otel"),
                    route = HotelListRoute,
                    isSelected = currentRoute?.contains("HotelListRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("AYARLAR"),
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Ayarlar & Dil"),
                    route = SettingsRoute,
                    isSelected = currentRoute?.contains("SettingsRoute") == true || currentRoute?.contains("MultiLanguageRoute") == true
                )
            )
        )
    )

    return groups
}

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
    val authRepository: AuthRepository = org.koin.compose.koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()
    val isSystemAdmin = currentUser?.role == com.mgacreative.touros.domain.model.UserRole.SYSTEM_ADMIN || currentUser?.email == "mgazat@gmail.com" || currentUser?.email == "gkhnazat@gmail.com"

    val isUserLoggedIn = currentUser != null
    val isAuthRoute = backStackEntry?.destination.isAuthRoute()
    val isPublicWebRoute = currentRoute?.contains("GlobalWebPublicRoute") == true
    // Yan sol menü dış ziyaretçide PASİF, sadece E-Posta + Şifre + Acente Kodu ile giren oturumlu acentelerde AKTİF
    val showShell = isUserLoggedIn && !isAuthRoute && !isPublicWebRoute

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
        val navGroups = remember(currentRoute, currentLanguage, isSystemAdmin) { buildNavGroups(currentRoute, isSystemAdmin) }
        val navItems = remember(navGroups) { navGroups.flatMap { it.items } }

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

        val displayName = currentUser?.fullName?.ifBlank { currentUser?.email } ?: "Acente Yöneticisi"
        val displayRole = currentUser?.role?.displayName ?: "SaaS Acentesi"

        val handleLogout: () -> Unit = {
            coroutineScope.launch {
                authRepository.signOut()
                if (drawerState.isOpen) drawerState.close()
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        if (isExpanded) {
            // ── Expanded: Sidebar sol, içerik sağda ──────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                if (showShell) {
                    TourOSSidebar(
                        items = navItems,
                        groups = navGroups,
                        onItemSelect = { navigate(it.route) },
                        userName = displayName,
                        userRole = displayRole,
                        onLogoutClick = handleLogout
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
                            userName = displayName,
                            userRole = displayRole,
                            onLogoutClick = handleLogout
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
    NavHost(navController = navController, startDestination = GlobalWebPublicRoute) {

        // ─── Auth & Public Landing ──────────────────────────────────────────

        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(GlobalWebPublicRoute) { popUpTo(SplashRoute) { inclusive = true } }
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

        composable<GlobalWebCmsRoute> {
            GlobalWebCmsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<GlobalWebPublicRoute> {
            GlobalWebPublicScreen(
                onNavigateToBookingDetail = { id -> navController.navigate(BookingDetailRoute(id)) },
                onNavigateToLogin = { navController.navigate(LoginRoute) },
                onNavigateBack = { 
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(DashboardRoute)
                    }
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
            com.mgacreative.touros.ui.screens.AgencyProductPublishingScreen(
                onNavigateToSearchWizard = { navController.navigate(B2BTourSearchDashboardRoute) }
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

        // ─── B2B/B2C Tour Search & Booking Wizard ─────────────────────────────
        composable<B2BTourSearchDashboardRoute> {
            com.mgacreative.touros.ui.screens.B2BTourSearchDashboardScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBookings = { navController.navigate(BookingsRoute) }
            )
        }

        composable<B2BTourFlightServiceSelectionRoute> { back ->
            val route: B2BTourFlightServiceSelectionRoute = back.toRoute()
            com.mgacreative.touros.ui.screens.B2BTourFlightServiceSelectionScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onProceedToPassengerCheckout = {
                    navController.navigate(B2BPassengerCheckoutWizardRoute(productId = route.productId))
                }
            )
        }

        composable<B2BPassengerCheckoutWizardRoute> {
            com.mgacreative.touros.ui.screens.B2BPassengerCheckoutWizardScreen(
                viewModel = koinViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(DashboardRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}

