package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.data.database.entity.CommissionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.6.11 Komisyon Hesaplama ve Acente Cari Entegrasyonu (MarketplaceCommissionEngine).
 * Her satıştan sonra agency_operator_connections'daki komisyon oranına göre
 * otomatik komisyon hesaplar ve Acente'nin cari hesabına ile commissions tablosuna işler.
 */
class MarketplaceCommissionEngine(
    private val supabaseClient: SupabaseClient
) {

    /**
     * Tutar ve oran üzerinden komisyon hesaplar (ör. 10.000 ₺ x %15 = 1.500 ₺)
     */
    fun calculateCommission(totalPrice: Double, commissionRate: Double): Double {
        if (totalPrice <= 0 || commissionRate <= 0) return 0.0
        return (totalPrice * (commissionRate / 100.0))
    }

    /**
     * Rezervasyon onaylandığında komisyon kaydı oluşturup acente cari hesabına işler.
     */
    suspend fun processBookingCommission(
        bookingId: String,
        agencyId: String,
        operatorCompanyId: String,
        totalPrice: Double
    ): Result<CommissionEntity> {
        return runCatching {
            val rpcParams = buildJsonObject {
                put("p_booking_id", bookingId)
                put("p_agency_id", agencyId)
                put("p_operator_company_id", operatorCompanyId)
                put("p_total_price", totalPrice)
            }

            val commission = supabaseClient.postgrest.rpc("process_marketplace_booking_commission", rpcParams)
                .decodeSingleOrNull<CommissionEntity>()

            commission ?: CommissionEntity(
                bookingId = bookingId,
                rate = 10.0,
                amount = calculateCommission(totalPrice, 10.0),
                isPaid = false
            )
        }
    }
}
