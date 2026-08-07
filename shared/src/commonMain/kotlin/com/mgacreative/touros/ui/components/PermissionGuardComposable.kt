package com.mgacreative.touros.ui.components

import androidx.compose.runtime.Composable
import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.security.PermissionGuard

/**
 * Compose Multiplatform UI yetki sarmalayıcısı.
 * Kullanıcı belirtilen kaynak ve işleme yetkiliyse içeriği gösterir, aksi halde gizler veya fallback basar.
 */
@Composable
fun PermissionGuard(
    role: UserRole,
    resource: PermissionResource,
    action: PermissionAction,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val isAllowed = PermissionGuard.hasPermission(role, resource, action)
    if (isAllowed) {
        content()
    } else {
        fallback()
    }
}
