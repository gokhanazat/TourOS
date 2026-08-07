package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Permission
import com.mgacreative.touros.domain.model.Role

/**
 * Rol ve İzin Yönetimi Repository Arayüzü.
 */
interface RolePermissionRepository {
    suspend fun getRolesForTenant(tenantId: String): Result<List<Role>>
    suspend fun getPermissionsForRole(roleId: String): Result<List<Permission>>
    suspend fun saveRole(role: Role): Result<Role>
    suspend fun savePermission(permission: Permission): Result<Permission>
    suspend fun updatePermissionsForRole(roleId: String, permissions: List<Permission>): Result<Unit>
}
