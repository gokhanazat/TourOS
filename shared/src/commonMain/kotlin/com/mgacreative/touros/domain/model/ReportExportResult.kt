package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.4 Rapor Dışa Aktarma Sonuç Modeli.
 */
@Serializable
data class ReportExportResult(
    @SerialName("export_id") val exportId: String = "",
    @SerialName("document_name") val documentName: String = "",
    @SerialName("export_url") val exportUrl: String = "",
    @SerialName("record_count") val recordCount: Int = 0,
    @SerialName("format_type") val formatType: String = "PDF",
    @SerialName("created_at") val createdAt: String = ""
)
