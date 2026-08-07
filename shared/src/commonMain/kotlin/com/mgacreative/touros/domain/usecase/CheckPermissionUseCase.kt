package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.security.PermissionGuard

class CheckPermissionUseCase {
    operator fun invoke(
        user: User?,
        resource: PermissionResource,
        action: PermissionAction
    ): Boolean {
        if (user == null) return false
        return PermissionGuard.hasPermission(user, resource, action)
    }
}
