package com.mgacreative.touros.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

// ─── Global NavController CompositionLocal (Tüm sayfalarda Geri Dön ve Navigasyon için) ───
val LocalNavController = androidx.compose.runtime.staticCompositionLocalOf<NavHostController?> { null }

// ─── Public Landing & Auth Route'ları (Shell & Rehber gizlenir) ─────────────
private val publicAndAuthRoutePatterns = listOf(
    "SplashRoute", "LoginRoute", "RegisterRoute",
    "ForgotPasswordRoute", "OnboardingRoute", "EmailVerificationRoute",
    "GlobalWebPublicRoute", "B2BTourFlightServiceSelectionRoute", "B2BPassengerCheckoutWizardRoute"
)

private val adminRoutePatterns = listOf(
    "AgencyApprovalRoute", "AgencySearchRoute", "AgencyQuotaReportRoute",
    "AdminAgencyLedgerRoute", "GlobalWebCmsRoute", "AdminProductManagementRoute",
    "SaasCacheManagementRoute", "AdminDeploymentRoute"
)

private fun NavDestination?.isAuthOrPublicRoute(): Boolean =
    publicAndAuthRoutePatterns.any { this?.route?.contains(it) == true }

private fun String?.isAdminOrPublicRoute(): Boolean =
    this == null ||
    publicAndAuthRoutePatterns.any { this.contains(it) } ||
    adminRoutePatterns.any { this.contains(it) }

// ─── Menü Grupları ─────────────────────────────────────────────────────────────
private fun buildNavGroups(currentRoute: String?, isSystemAdmin: Boolean = false): List<TourOSNavGroup> {
    val groups = mutableListOf<TourOSNavGroup>()

    // SAAS ADMİN PANELİ (Sadece Sistem Yöneticisi görebilir)
    if (isSystemAdmin) {
        groups.add(
            TourOSNavGroup(
                categoryTitle = AppLanguageManager.translate("SAAS ADMİN PANELİ"),
                items = listOf(
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Onay Bekleyen Acenteler"),
                        route = AgencyApprovalRoute,
                        isSelected = currentRoute?.contains("AgencyApprovalRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Acente Sorgulama & Lisans"),
                        route = AgencySearchRoute,
                        isSelected = currentRoute?.contains("AgencySearchRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Acente Kota & Tüketim Raporu"),
                        route = AgencyQuotaReportRoute,
                        isSelected = currentRoute?.contains("AgencyQuotaReportRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Acente Cari & Borç Takibi"),
                        route = AdminAgencyLedgerRoute,
                        isSelected = currentRoute?.contains("AdminAgencyLedgerRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Web Yönetimi (CMS)"),
                        route = GlobalWebCmsRoute,
                        isSelected = currentRoute?.contains("GlobalWebCmsRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Ürün & Data Yönetimi"),
                        route = AdminProductManagementRoute,
                        isSelected = currentRoute?.contains("AdminProductManagementRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Önbellek & API Performansı"),
                        route = SaasCacheManagementRoute,
                        isSelected = currentRoute?.contains("SaasCacheManagementRoute") == true
                    ),
                    TourOSNavItem(
                        title = AppLanguageManager.translate("Sürüm & Dağıtım (CI/CD)"),
                        route = AdminDeploymentRoute,
                        isSelected = currentRoute?.contains("AdminDeploymentRoute") == true
                    )
                )
            )
        )
    }

    // NORMAL KULLANICI / ACENTE MENÜSÜ: B2B SATIŞ & REZERVASYON
    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("B2B SATIŞ & REZERVASYON"),
            isCollapsible = true,
            isInitiallyExpanded = true,
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("Rezervasyonlar"),
                    route = BookingsRoute,
                    isSelected = currentRoute?.contains("BookingsRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Yeni Rezervasyon"),
                    route = B2BTourSearchDashboardRoute,
                    isSelected = currentRoute?.contains("B2BTourSearchDashboardRoute") == true
                )
            )
        )
    )

    // TUR OPERATÖRÜ GRUBU
    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("TUR OPERATÖRÜ"),
            isCollapsible = true,
            isInitiallyExpanded = true,
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("TO Ödeme & PNR"),
                    route = OperatorPaymentManagementRoute,
                    isSelected = currentRoute?.contains("OperatorPaymentManagementRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("TO Cari Hesap"),
                    route = OperatorCurrentAccountReportRoute,
                    isSelected = currentRoute?.contains("OperatorCurrentAccountReportRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Operatör Bağlantıları & API"),
                    route = AgencyOperatorConnectionsRoute,
                    isSelected = currentRoute?.contains("AgencyOperatorConnectionsRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("MUHASEBE"),
            isCollapsible = true,
            isInitiallyExpanded = false,
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
            isCollapsible = true,
            isInitiallyExpanded = false,
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

    // PREMIUM AÇILIR MENÜ GRUBU (Tüm acenteler için)
    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("PREMIUM"),
            isCollapsible = true,
            isInitiallyExpanded = false,
            items = listOf(
                TourOSNavItem(
                    title = AppLanguageManager.translate("OTA & Kanal Yöneticisi"),
                    route = OTADashboardRoute,
                    isSelected = currentRoute?.contains("OTADashboardRoute") == true || currentRoute?.contains("OTAConnectionDetailRoute") == true
                ),
                TourOSNavItem(
                    title = AppLanguageManager.translate("Senkronizasyon Logları"),
                    route = SyncLogsRoute(providerIdFilter = "ALL"),
                    isSelected = currentRoute?.contains("SyncLogsRoute") == true
                )
            )
        )
    )

    groups.add(
        TourOSNavGroup(
            categoryTitle = AppLanguageManager.translate("YEREL"),
            isCollapsible = true,
            isInitiallyExpanded = false,
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
            isCollapsible = true,
            isInitiallyExpanded = false,
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
    val isSystemAdmin = currentUser?.email == "gkhnazat@gmail.com" || currentUser?.role?.name == "SYSTEM_ADMIN"

    val isAuthOrPublicRoute = backStackEntry?.destination.isAuthOrPublicRoute()
    val isGuest = currentUser == null
    // Yan sol gezinti menüsü (Sidebar) ve alt menü SADECE giriş yapmış acente / admin kullanıcılarında ve iç yönetim sayfalarında gösterilir.
    // Misafir (giriş yapmamış kullanıcı) veya Auth/Public/Rezervasyon rotalarında acente yan menüsü KESİNLİKLE GİZLENİR!
    val showShell = !isGuest && !isAuthOrPublicRoute
    // Sayfa Rehberi SADECE giriş yapmış kullanıcılarda ve iç sayfalarda gösterilir
    val showHelpAssistant = !isGuest && !currentRoute.isAdminOrPublicRoute()
    var isHelpDrawerOpen by remember { mutableStateOf(false) }

    fun navigate(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    androidx.compose.runtime.CompositionLocalProvider(LocalNavController provides navController) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowWidthClass = com.mgacreative.touros.ui.theme.getWindowWidthClass(maxWidth)
            val isExpanded = windowWidthClass == com.mgacreative.touros.ui.theme.WindowWidthClass.EXPANDED
            val isMedium = windowWidthClass == com.mgacreative.touros.ui.theme.WindowWidthClass.MEDIUM
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
                    navController.navigate(GlobalWebPublicRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            if (isExpanded) {
                // ── Expanded (≥840dp Masaüstü & Yatay Tablet): Kalıcı Sol Sidebar + Sağ İçerik ──
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
                        AppNavHost(navController)
                    }
                }
            } else if (isMedium) {
                // ── Medium (600dp-839dp Tablet Dikey & Katlanabilir): Çekmece + Esnek İçerik ──
                androidx.compose.material3.ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = showShell,
                    drawerContent = {
                        androidx.compose.material3.ModalDrawerSheet(
                            drawerContainerColor = TourOSColors.Surface
                        ) {
                            TourOSSidebar(
                                items = navItems,
                                groups = navGroups,
                                onItemSelect = { item ->
                                    coroutineScope.launch { drawerState.close() }
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
                            AppNavHost(navController)
                        }
                    }
                }
            } else {
                // ── Compact (<600dp Telefon Dikey): Alt Menü Çubuğu (BottomBar) + Drawer ──
                androidx.compose.material3.ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = showShell,
                    drawerContent = {
                        androidx.compose.material3.ModalDrawerSheet(
                            drawerContainerColor = TourOSColors.Surface
                        ) {
                            TourOSSidebar(
                                items = navItems,
                                groups = navGroups,
                                onItemSelect = { item ->
                                    coroutineScope.launch { drawerState.close() }
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
                            AppNavHost(navController)
                        }
                    }
                }
            }

            // ─── Sayfa İçi Akıllı Yardım & Rehber Asistanı (Web, Desktop, Android, iOS) ───
            if (showHelpAssistant) {
                com.mgacreative.touros.ui.components.TourOSHelpAssistantFAB(
                    onClick = { isHelpDrawerOpen = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 24.dp,
                            bottom = if (isExpanded) 28.dp else 84.dp
                        ),
                    isExpandedScreen = isExpanded
                )
            }

            if (isHelpDrawerOpen && showHelpAssistant) {
                com.mgacreative.touros.ui.components.TourOSHelpDrawer(
                    currentRoute = currentRoute,
                    onDismiss = { isHelpDrawerOpen = false },
                    isExpandedScreen = isExpanded
                )
            }
        }
    }
}


@Composable
private fun AppNavHost(navController: NavHostController) {
    val authRepository: AuthRepository = org.koin.compose.koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()

    NavHost(navController = navController, startDestination = GlobalWebPublicRoute) {

        // ─── Auth & Public Landing ──────────────────────────────────────────

        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(GlobalWebPublicRoute) { popUpTo(SplashRoute) { inclusive = true } }
                },
                onNavigateToDashboard = { role ->
                    navController.navigate(GlobalWebPublicRoute) {
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
                    navController.navigate(GlobalWebPublicRoute) { popUpTo(DashboardRoute) { inclusive = true } }
                },
                onNavigateToBookings = { navController.navigate(BookingsRoute) },
                onNavigateToTours = { navController.navigate(ToursRoute) },
                onNavigateToHotels = { navController.navigate(HotelListRoute) },
                onNavigateToReports = { navController.navigate(ReportsRoute) }
            )
        }

        composable<AgencyApprovalRoute> {
            AgencyApprovalScreen()
        }

        composable<AgencySearchRoute> {
            AgencySearchScreen()
        }

        composable<AgencyQuotaReportRoute> {
            AgencyQuotaReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AdminAgencyLedgerRoute> {
            AdminAgencyLedgerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AdminDeploymentRoute> {
            AdminDeploymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<GlobalWebCmsRoute> {
            val isAdmin = currentUser?.email == "gkhnazat@gmail.com" || currentUser?.role?.name == "SYSTEM_ADMIN"
            if (!isAdmin) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(LoginRoute) {
                        popUpTo(GlobalWebCmsRoute) { inclusive = true }
                    }
                }
            } else {
                GlobalWebCmsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable<GlobalWebPublicRoute> {
            GlobalWebPublicScreen(
                onNavigateToB2BSearch = {
                    navController.navigate(B2BTourSearchDashboardRoute)
                },
                onNavigateToBookingDetail = { id -> navController.navigate(BookingDetailRoute(id)) },
                onNavigateToLogin = {
                    if (currentUser != null) {
                        navController.navigate(B2BTourSearchDashboardRoute)
                    } else {
                        navController.navigate(LoginRoute)
                    }
                },
                onNavigateToAdminCms = {
                    navController.navigate(GlobalWebCmsRoute)
                },
                onNavigateToNewBooking = { selectedOffer ->
                    navController.navigate(B2BTourFlightServiceSelectionRoute(productId = selectedOffer.id))
                },
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

        composable<SettingsRoute> {
            CompanySettingsScreen()
        }

        composable<MultiLanguageRoute> {
            MultiLanguageScreen(
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
            B2BTourSearchDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBookings = { navController.navigate(BookingsRoute) }
            )
        }

        composable<CreateBookingStep2Route> {
            CreateBookingStep2Screen(
                onNavigateBack = { navController.popBackStack() },
                onBookingCreatedSuccess = { navController.popBackStack(DashboardRoute, false) }
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

        composable<CurrencyConverterRoute> {
            CurrencyConverterScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        composable<ExchangeRatesRoute> {
            ExchangeRatesScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }

        // ─── Raporlar ─────────────────────────────────────────────────────────

        composable<ReportsRoute> {
            ReportsScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }
        composable<OperatorPaymentManagementRoute> {
            com.mgacreative.touros.ui.screens.OperatorPaymentManagementScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
        }
        composable<OperatorCurrentAccountReportRoute> {
            com.mgacreative.touros.ui.screens.OperatorCurrentAccountReportScreen(viewModel = koinViewModel(), onNavigateBack = { navController.popBackStack() })
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

        // ─── OTA / Gelişmiş ───────────────────────────────────────────────────

        composable<OTADashboardRoute> {
            com.mgacreative.touros.ui.screens.OTADashboardScreen(
                viewModel = koinViewModel(),
                onNavigateToLogs = { providerId -> navController.navigate(SyncLogsRoute(providerIdFilter = providerId)) }
            )
        }

        composable<AgencyOperatorConnectionsRoute> {
            com.mgacreative.touros.ui.screens.AgencyOperatorConnectionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AgencyProductPublishingRoute> {
            com.mgacreative.touros.ui.screens.AgencyProductPublishingScreen(
                onNavigateToSearchWizard = { navController.navigate(B2BTourSearchDashboardRoute) }
            )
        }

        composable<AdminProductManagementRoute> {
            com.mgacreative.touros.ui.screens.AdminProductManagementScreen()
        }

        composable<SaasCacheManagementRoute> {
            com.mgacreative.touros.ui.screens.SaasCacheManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AdminDeploymentRoute> {
            com.mgacreative.touros.ui.screens.AdminDeploymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AgencyQuotaReportRoute> {
            com.mgacreative.touros.ui.screens.AgencyQuotaReportScreen(
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
                onNavigateToTourDetail = { tourId -> navController.navigate(TourDetailRoute(tourId = tourId)) },
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
                productId = route.productId,
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
                    if (currentUser != null) {
                        navController.navigate(DashboardRoute) {
                            popUpTo(DashboardRoute) { inclusive = true }
                        }
                    } else {
                        navController.navigate(GlobalWebPublicRoute) {
                            popUpTo(GlobalWebPublicRoute) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

