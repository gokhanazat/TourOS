package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * documents tablosu – Polymorphic dosya eki entity.
 */
@Serializable
data class DocumentEntity(
    val id: String = "",
    @SerialName("owner_type") val ownerType: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val title: String = "",
    @SerialName("file_url") val fileUrl: String = "",
    @SerialName("file_type") val fileType: String? = null,
    @SerialName("file_size_bytes") val fileSizeBytes: Long? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * images tablosu – Polymorphic görsel entity.
 */
@Serializable
data class ImageEntity(
    val id: String = "",
    @SerialName("owner_type") val ownerType: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val url: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("alt_text") val altText: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_cover") val isCover: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("file_size_bytes") val fileSizeBytes: Long? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * vouchers tablosu – Voucher/kupon entity.
 */
@Serializable
data class VoucherEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    @SerialName("voucher_no") val voucherNo: String = "",
    @SerialName("voucher_type") val voucherType: String = "hotel",
    val content: JsonObject = JsonObject(emptyMap()),
    @SerialName("issued_at") val issuedAt: String = "",
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_until") val validUntil: String? = null,
    val status: String = "active",
    @SerialName("pdf_url") val pdfUrl: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * notifications tablosu – Bildirim entity.
 */
@Serializable
data class NotificationEntity(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String = "",
    val body: String? = null,
    val channel: String = "in_app",
    @SerialName("ref_type") val refType: String? = null,
    @SerialName("ref_id") val refId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * tasks tablosu – Görev entity.
 */
@Serializable
data class TaskEntity(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("ref_type") val refType: String? = null,
    @SerialName("ref_id") val refId: String? = null,
    val priority: String = "medium",
    val status: String = "open",
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * calendars tablosu – Takvim etkinliği entity.
 */
@Serializable
data class CalendarEntity(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    @SerialName("event_type") val eventType: String = "departure",
    @SerialName("ref_type") val refType: String? = null,
    @SerialName("ref_id") val refId: String? = null,
    @SerialName("start_at") val startAt: String = "",
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("is_all_day") val isAllDay: Boolean = false,
    val color: String? = null,
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * audit_logs tablosu – İmmutable log entity.
 */
@Serializable
data class AuditLogEntity(
    val id: String = "",
    @SerialName("table_name") val tableName: String = "",
    @SerialName("record_id") val recordId: String = "",
    val action: String = "",
    @SerialName("old_data") val oldData: JsonObject? = null,
    @SerialName("new_data") val newData: JsonObject? = null,
    @SerialName("changed_fields") val changedFields: List<String>? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("performed_by") val performedBy: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
