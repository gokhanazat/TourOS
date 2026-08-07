package com.mgacreative.touros.domain.security

import com.mgacreative.touros.domain.model.Permission
import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RBAC Permission Checking Guard Mekanizması.
 * 8 Varsayılan rol için yetki matrisi ve anlık dinamik veritabanı izin kontrolü sağlar.
 */
object PermissionGuard {

    private val _dynamicPermissions = MutableStateFlow<List<Permission>>(emptyList())
    val dynamicPermissions: StateFlow<List<Permission>> = _dynamicPermissions.asStateFlow()

    fun updateDynamicPermissions(newPermissions: List<Permission>) {
        _dynamicPermissions.value = newPermissions
    }

    /**
     * Verilen rol, kaynak ve işlem için varsayılan yetki kontrolü yapar.
     */
    fun hasPermission(
        role: UserRole,
        resource: PermissionResource,
        action: PermissionAction
    ): Boolean {
        // Dinamik izinlerde tanım varsa öncelikli olarak uygula
        val override = _dynamicPermissions.value.firstOrNull { it.resource == resource && it.action == action }
        if (override != null) {
            return override.isAllowed
        }

        return when (role) {
            UserRole.SYSTEM_ADMIN -> true // Sistem yöneticisi tam yetkili
            UserRole.TOUR_OPERATOR -> when (resource) {
                PermissionResource.TOURS,
                PermissionResource.BOOKINGS,
                PermissionResource.CUSTOMERS,
                PermissionResource.VEHICLES,
                PermissionResource.HOTELS,
                PermissionResource.REPORTS,
                PermissionResource.SETTINGS -> action != PermissionAction.DELETE || role == UserRole.SYSTEM_ADMIN
                PermissionResource.USERS -> action == PermissionAction.READ
                PermissionResource.FINANCE -> action == PermissionAction.READ
            }
            UserRole.SALES -> when (resource) {
                PermissionResource.BOOKINGS,
                PermissionResource.CUSTOMERS -> action != PermissionAction.DELETE
                PermissionResource.TOURS -> action == PermissionAction.READ
                PermissionResource.REPORTS -> action == PermissionAction.READ
                else -> false
            }
            UserRole.GUIDE -> when (resource) {
                PermissionResource.TOURS,
                PermissionResource.BOOKINGS -> action == PermissionAction.READ
                else -> false
            }
            UserRole.DRIVER -> when (resource) {
                PermissionResource.VEHICLES,
                PermissionResource.TOURS -> action == PermissionAction.READ
                else -> false
            }
            UserRole.ACCOUNTING -> when (resource) {
                PermissionResource.FINANCE,
                PermissionResource.REPORTS -> true
                PermissionResource.BOOKINGS -> action == PermissionAction.READ
                else -> false
            }
            UserRole.AGENT -> when (resource) {
                PermissionResource.BOOKINGS -> action == PermissionAction.CREATE || action == PermissionAction.READ
                PermissionResource.TOURS -> action == PermissionAction.READ
                else -> false
            }
            UserRole.CUSTOMER -> when (resource) {
                PermissionResource.BOOKINGS -> action == PermissionAction.CREATE || action == PermissionAction.READ
                PermissionResource.TOURS -> action == PermissionAction.READ
                else -> false
            }
        }
    }

    /**
     * Kullanıcının özel izin listesini ve varsayılan rol yetkisini harmanlayarak doğrular.
     */
    fun hasPermission(
        user: User,
        resource: PermissionResource,
        action: PermissionAction,
        extraPermissions: List<Permission> = emptyList()
    ): Boolean {
        val override = extraPermissions.firstOrNull { it.resource == resource && it.action == action }
            ?: _dynamicPermissions.value.firstOrNull { it.resource == resource && it.action == action }

        if (override != null) {
            return override.isAllowed
        }

        return hasPermission(user.role, resource, action)
    }
}
