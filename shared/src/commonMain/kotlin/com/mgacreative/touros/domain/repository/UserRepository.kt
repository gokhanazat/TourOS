package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole

/**
 * Kullanıcı listeleme ve durum yönetimi repository arayüzü.
 */
interface UserRepository {
    suspend fun getUsersForTenant(tenantId: String): Result<List<User>>
    suspend fun toggleUserActiveStatus(userId: String, isActive: Boolean): Result<Unit>
    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit>
}
