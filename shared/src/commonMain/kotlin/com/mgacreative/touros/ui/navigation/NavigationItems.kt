package com.mgacreative.touros.ui.navigation

import com.mgacreative.touros.domain.model.UserRole

/**
 * Navigasyon Menü Grubu.
 */
data class NavigationGroup(
    val title: String,
    val items: List<NavigationItem>
)

/**
 * Navigasyon menü öğeleri ve rol bazlı erişim kontrolü.
 */
data class NavigationItem(
    val title: String,
    val route: Any, // Serializable route
    val icon: String = "", // Material icon name
    val allowedRoles: Set<UserRole> = setOf(
        UserRole.SYSTEM_ADMIN,
        UserRole.TOUR_OPERATOR,
        UserRole.SALES,
        UserRole.ACCOUNTING,
        UserRole.AGENT,
        UserRole.GUIDE
    )
)

/**
 * Kategori bazlı gruplandırılmış navigasyon menüleri.
 */
val navigationGroups = listOf(
    NavigationGroup(
        title = "WEB YÖNETİMİ",
        items = listOf(
            NavigationItem(
                title = "Web Yönetimi (CMS)",
                route = GlobalWebCmsRoute
            ),
            NavigationItem(
                title = "Canlı Web",
                route = GlobalWebPublicRoute
            )
        )
    ),
    NavigationGroup(
        title = "TUR OPERATÖRLERİ",
        items = listOf(
            NavigationItem(
                title = "Tur Operatörleri",
                route = AgencyOperatorConnectionsRoute
            ),
            NavigationItem(
                title = "Ürünler",
                route = AgencyProductPublishingRoute
            ),
            NavigationItem(
                title = "Rezervasyonlar",
                route = BookingsRoute
            )
        )
    ),
    NavigationGroup(
        title = "MUHASEBE",
        items = listOf(
            NavigationItem(
                title = "Finans",
                route = FinancialReportsRoute
            ),
            NavigationItem(
                title = "Fatura Yönetimi",
                route = InvoiceManagementRoute
            ),
            NavigationItem(
                title = "Cari Hesaplar",
                route = CurrentAccountRoute
            ),
            NavigationItem(
                title = "Gider Girişi",
                route = SupplierExpenseRoute
            )
        )
    ),
    NavigationGroup(
        title = "ANALİTİK",
        items = listOf(
            NavigationItem(
                title = "Dashboard",
                route = DashboardRoute
            ),
            NavigationItem(
                title = "Analitik & Trend",
                route = AnalyticsChartsRoute
            ),
            NavigationItem(
                title = "Raporlar",
                route = ReportsRoute
            ),
            NavigationItem(
                title = "Müşteri & CRM",
                route = CustomersRoute
            )
        )
    ),
    NavigationGroup(
        title = "YEREL",
        items = listOf(
            NavigationItem(
                title = "Yerel Tur",
                route = ToursRoute
            ),
            NavigationItem(
                title = "Yerel Otel",
                route = HotelListRoute
            )
        )
    ),
    NavigationGroup(
        title = "AYARLAR",
        items = listOf(
            NavigationItem(
                title = "Ayarlar & Dil",
                route = SettingsRoute
            )
        )
    )
)

val navigationItems = navigationGroups.flatMap { it.items }

/**
 * Belirtilen rol için görünür navigasyon öğelerini filtreler.
 */
fun getNavigationItemsForRole(role: UserRole): List<NavigationItem> {
    return navigationItems.filter { role in it.allowedRoles }
}

fun getNavigationGroupsForRole(role: UserRole): List<NavigationGroup> {
    return navigationGroups.mapNotNull { group ->
        val filtered = group.items.filter { role in it.allowedRoles }
        if (filtered.isNotEmpty()) group.copy(items = filtered) else null
    }
}

/**
 * Rol bazlı varsayılan başlangıç route'u.
 */
fun getStartDestinationForRole(role: UserRole): Any {
    return when (role) {
        UserRole.SYSTEM_ADMIN -> DashboardRoute
        UserRole.TOUR_OPERATOR -> DashboardRoute
        UserRole.SALES -> BookingsRoute
        UserRole.GUIDE -> AssignedTasksRoute
        UserRole.DRIVER -> AssignedTasksRoute
        UserRole.ACCOUNTING -> ReportsRoute
        UserRole.AGENT -> BookingsRoute
        UserRole.CUSTOMER -> BookingsRoute
    }
}
