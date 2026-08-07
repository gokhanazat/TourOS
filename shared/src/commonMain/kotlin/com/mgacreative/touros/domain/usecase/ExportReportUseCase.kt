package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.engine.ReportExportEngine
import com.mgacreative.touros.domain.model.ReportExportResult
import com.mgacreative.touros.domain.model.ReportFilter
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.3.4 Rapor Filtreleme & PDF/Excel Dışa Aktarma Use Case.
 */
class ExportReportUseCase(
    private val supabaseClient: SupabaseClient,
    private val reportExportEngine: ReportExportEngine
) {
    suspend operator fun invoke(filter: ReportFilter, tenantId: String): Result<ReportExportResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_start_date", filter.startDate)
                put("p_end_date", filter.endDate)
                put("p_currency", filter.currency)
                put("p_company_name", filter.companyName)
                put("p_export_format", filter.exportFormat)
            }

            val list = supabaseClient.postgrest.rpc("export_filtered_report", params)
                .decodeList<ReportExportResult>()

            list.firstOrNull() ?: reportExportEngine.generateExportDocument(filter, tenantId)
        }.recover {
            reportExportEngine.generateExportDocument(filter, tenantId)
        }
    }
}
