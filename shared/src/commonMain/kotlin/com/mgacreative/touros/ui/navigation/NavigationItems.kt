package com.mgacreative.touros.ui.navigation

import com.mgacreative.touros.domain.model.UserRole

/**
 * Navigasyon menü öğeleri ve rol bazlı erişim kontrolü.
 */
data class NavigationItem(
    val title: String,
    val route: Any, // Serializable route
    val icon: String, // Material icon name
    val allowedRoles: Set<UserRole>
)

/**
 * Rol bazlı navigasyon öğeleri.
 * Her rol sadece kendi yetkili olduğu menü öğelerini görür.
 */
val navigationItems = listOf(
    NavigationItem(
        title = "Ana Sayfa",
        route = DashboardRoute,
        icon = "dashboard",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR,
            UserRole.SALES,
            UserRole.ACCOUNTING,
            UserRole.AGENT,
            UserRole.CUSTOMER
        )
    ),
    NavigationItem(
        title = "Görevlerim",
        route = AssignedTasksRoute,
        icon = "assignment",
        allowedRoles = setOf(
            UserRole.GUIDE,
            UserRole.DRIVER
        )
    ),
    NavigationItem(
        title = "Turlar",
        route = ToursRoute,
        icon = "tour",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR,
            UserRole.SALES,
            UserRole.GUIDE
        )
    ),
    NavigationItem(
        title = "Rezervasyonlar",
        route = BookingsRoute,
        icon = "book_online",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR,
            UserRole.SALES,
            UserRole.AGENT,
            UserRole.CUSTOMER
        )
    ),
    NavigationItem(
        title = "Müşteriler",
        route = CustomersRoute,
        icon = "people",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR,
            UserRole.SALES
        )
    ),
    NavigationItem(
        title = "Raporlar",
        route = ReportsRoute,
        icon = "analytics",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR,
            UserRole.ACCOUNTING
        )
    ),
    NavigationItem(
        title = "Ayarlar",
        route = SettingsRoute,
        icon = "settings",
        allowedRoles = setOf(
            UserRole.SYSTEM_ADMIN,
            UserRole.TOUR_OPERATOR
        )
    )
)

/**
 * Belirtilen rol için görünür navigasyon öğelerini filtreler.
 */
fun getNavigationItemsForRole(role: UserRole): List<NavigationItem> {
    return navigationItems.filter { role in it.allowedRoles }
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
