package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.4.1 Belge Yükleme ve Saklama (Pasaport, Vize, Sözleşme, Voucher, PDF, Fotoğraf) Modeli.
 */
@Serializable
data class DocumentItem(
    val id: String = "",
    @SerialName("document_type") val documentType: String = "passport", // passport, visa, contract, voucher, pdf, photo
    val title: String = "",
    @SerialName("file_path") val filePath: String = "",
    @SerialName("file_size") val fileSize: Long = 0L,
    @SerialName("mime_type") val mimeType: String = "application/pdf",
    @SerialName("storage_bucket") val storageBucket: String = "documents",
    @SerialName("public_url") val publicUrl: String = "",
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
