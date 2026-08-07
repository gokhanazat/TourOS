package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.UserEntity
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
                User(
                    id = entity.id,
                    email = entity.email,
                    fullName = entity.fullName ?: "",
                    role = UserRole.fromString(entity.roleId ?: "CUSTOMER"),
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
        return runCatching {
            val payload = buildJsonObject {
                put("role_id", newRole.name)
            }
            supabaseClient.postgrest.from("users").update(payload) {
                filter {
                    eq("id", userId)
                }
            }
        }
    }
}
