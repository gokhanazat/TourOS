package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.UserEntity
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add

import com.mgacreative.touros.data.util.isValidUuid

class UserRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : UserRepository {

    override suspend fun getUsersForTenant(tenantId: String): Result<List<User>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("users")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<UserEntity>()

            entities.map { entity ->
                val primaryRole = UserRole.fromString(entity.roleId ?: "CUSTOMER")
                val parsedRoles = entity.roles?.mapNotNull { roleStr ->
                    UserRole.entries.firstOrNull { it.name.equals(roleStr, ignoreCase = true) }
                }?.distinct()?.ifEmpty { null } ?: listOf(primaryRole)

                User(
                    id = entity.id,
                    email = entity.email,
                    fullName = entity.fullName ?: "",
                    role = primaryRole,
                    roles = parsedRoles,
                    tenantId = entity.tenantId,
                    avatarUrl = entity.avatarUrl,
                    isActive = entity.isActive
                )
            }
        }
    }

    override suspend fun toggleUserActiveStatus(userId: String, isActive: Boolean): Result<Unit> {
        return runCatching {
            val payload = buildJsonObject {
                put("is_active", isActive)
            }
            supabaseClient.postgrest.from("users").update(payload) {
                filter {
                    eq("id", userId)
                }
            }
        }
    }

    override suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return updateUserRoles(userId, listOf(newRole))
    }

    override suspend fun updateUserRoles(userId: String, newRoles: List<UserRole>): Result<Unit> {
        return runCatching {
            val primaryRole = newRoles.firstOrNull() ?: UserRole.CUSTOMER
            val payload = buildJsonObject {
                put("role_id", primaryRole.name)
                putJsonArray("roles") {
                    newRoles.forEach { add(it.name) }
                }
            }
            supabaseClient.postgrest.from("users").update(payload) {
                filter {
                    eq("id", userId)
                }
            }
        }
    }
}
