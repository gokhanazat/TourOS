package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.QRCheckInResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.3 QR Kod Tarama ve Tur Girişi Kontrolü Use Case.
 */
class ScanValidateQRTicketUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(qrData: String, tenantId: String): Result<QRCheckInResult> {
        if (qrData.isBlank()) {
            return Result.failure(IllegalArgumentException("QR kod verisi okunamadı."))
        }

        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_qr_data", qrData)
            }

            val list = supabaseClient.postgrest.rpc("validate_and_checkin_qr_ticket", params)
                .decodeList<QRCheckInResult>()

            list.firstOrNull() ?: QRCheckInResult()
        }.recover { QRCheckInResult() }
    }
}
