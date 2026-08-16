package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratedDocument(
    @SerialName("id") val id: String,
    @SerialName("document_type") val documentType: String = "voucher",
    @SerialName("title") val title: String = "",
    @SerialName("file_path") val filePath: String = "",
    @SerialName("file_size") val fileSize: Long = 0L,
    @SerialName("mime_type") val mimeType: String = "application/pdf",
    @SerialName("storage_bucket") val storageBucket: String = "documents",
    @SerialName("public_url") val publicUrl: String? = null,
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)
