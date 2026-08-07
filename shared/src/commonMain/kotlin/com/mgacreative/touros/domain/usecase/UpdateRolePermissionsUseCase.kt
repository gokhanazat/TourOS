package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Permission
import com.mgacreative.touros.domain.repository.RolePermissionRepository
import com.mgacreative.touros.domain.security.PermissionGuard

class UpdateRolePermissionsUseCase(
    private val repository: RolePermissionRepository
) {
    suspend operator fun invoke(roleId: String, permissions: List<Permission>): Result<Unit> {
        if (roleId.isBlank()) {
            return Result.failure(IllegalArgumentException("Rol ID boş olamaz"))
        }
        val result = repository.updatePermissionsForRole(roleId, permissions)
        if (result.isSuccess) {
            // PermissionGuard dinamik izinlerini anlık olarak güncelle
            PermissionGuard.updateDynamicPermissions(permissions)
        }
        return result
    }
}
