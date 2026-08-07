package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.PermissionEntity
import com.mgacreative.touros.data.database.entity.RoleEntity
import com.mgacreative.touros.domain.model.Permission
import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.Role
import com.mgacreative.touros.domain.repository.RolePermissionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

class RolePermissionRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : RolePermissionRepository {

    override suspend fun getRolesForTenant(tenantId: String): Result<List<Role>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("roles")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<RoleEntity>()

            entities.map { entity ->
                Role(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    isDefault = entity.isDefault,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getPermissionsForRole(roleId: String): Result<List<Permission>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("permissions")
                .select {
                    filter {
                        eq("role_id", roleId)
                    }
                }
                .decodeList<PermissionEntity>()

            entities.map { entity ->
                Permission(
                    id = entity.id,
                    roleId = entity.roleId,
                    resource = PermissionResource.fromKey(entity.resource),
                    action = PermissionAction.fromKey(entity.action),
                    isAllowed = entity.isAllowed,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun saveRole(role: Role): Result<Role> {
        return runCatching {
            val entity = RoleEntity(
                id = role.id,
                name = role.name,
                description = role.description,
                isDefault = role.isDefault,
                tenantId = role.tenantId
            )
            supabaseClient.postgrest.from("roles").upsert(entity)
            role
        }
    }

    override suspend fun savePermission(permission: Permission): Result<Permission> {
        return runCatching {
            val entity = PermissionEntity(
                id = permission.id,
                roleId = permission.roleId,
                resource = permission.resource.key,
                action = permission.action.key,
                isAllowed = permission.isAllowed,
                tenantId = permission.tenantId
            )
            supabaseClient.postgrest.from("permissions").upsert(entity)
            permission
        }
    }

    override suspend fun updatePermissionsForRole(
        roleId: String,
        permissions: List<Permission>
    ): Result<Unit> {
        return runCatching {
            val entities = permissions.map { perm ->
                PermissionEntity(
                    id = perm.id,
                    roleId = roleId,
                    resource = perm.resource.key,
                    action = perm.action.key,
                    isAllowed = perm.isAllowed,
                    tenantId = perm.tenantId
                )
            }
            supabaseClient.postgrest.from("permissions").upsert(entities)
        }
    }
}
