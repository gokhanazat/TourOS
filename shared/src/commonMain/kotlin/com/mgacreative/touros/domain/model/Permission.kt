package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PermissionResource(val key: String) {
    @SerialName("tours") TOURS("tours"),
    @SerialName("bookings") BOOKINGS("bookings"),
    @SerialName("customers") CUSTOMERS("customers"),
    @SerialName("reports") REPORTS("reports"),
    @SerialName("settings") SETTINGS("settings"),
    @SerialName("vehicles") VEHICLES("vehicles"),
    @SerialName("hotels") HOTELS("hotels"),
    @SerialName("finance") FINANCE("finance"),
    @SerialName("users") USERS("users");

    companion object {
        fun fromKey(key: String): PermissionResource =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: TOURS
    }
}

@Serializable
enum class PermissionAction(val key: String) {
    @SerialName("create") CREATE("create"),
    @SerialName("read") READ("read"),
    @SerialName("update") UPDATE("update"),
    @SerialName("delete") DELETE("delete"),
    @SerialName("execute") EXECUTE("execute");

    companion object {
        fun fromKey(key: String): PermissionAction =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: READ
    }
}

/**
 * İzin domain modeli.
 */
@Serializable
data class Permission(
    val id: String = "",
    @SerialName("role_id") val roleId: String = "",
    val resource: PermissionResource,
    val action: PermissionAction,
    @SerialName("is_allowed") val isAllowed: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = ""
)
