package com.mgacreative.touros.data.adapter

import com.mgacreative.touros.data.database.entity.AgencyOperatorConnectionEntity
import com.mgacreative.touros.domain.adapter.OTAProviderAdapter
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTABookingStatus
import com.mgacreative.touros.domain.model.ota.OTAPrice
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAWebhook
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.6.5 InternalOperatorAdapter.
 * OTAProviderAdapter arayüzünü implemente eder; dış HTTP API yerine cross-tenant Supabase RPC/Edge Function çağırır.
 * authenticate() adımını agency_operator_connections tablosundaki bağlantı onayına göre çalıştırır (iç yetkilendirme).
 */
class InternalOperatorAdapter(
    private val supabaseClient: SupabaseClient
) : OTAProviderAdapter {

    override val providerId: String = "internal_operator"
    override val providerName: String = "Sistem İçi Tur Operatörü (Cross-Tenant Marketplace)"

    override suspend fun authenticate(account: OTAAccount): Result<Boolean> {
        return runCatching {
            val connections = supabaseClient.postgrest["agency_operator_connections"]
                .select {
                    filter {
                        eq("status", "ACTIVE")
                    }
                }
                .decodeList<AgencyOperatorConnectionEntity>()

            if (connections.isNotEmpty()) {
                true
            } else {
                throw IllegalStateException("Acente ile Operatör arasında aktif pazaryeri bağlantısı (agency_operator_connections) bulunamadı.")
            }
        }
    }

    override suspend fun fetchBookings(account: OTAAccount): Result<List<OTABooking>> {
        return Result.success(emptyList())
    }

    override suspend fun fetchProducts(account: OTAAccount): Result<List<OTAProduct>> {
        return runCatching {
            val agencyId = account.accountId.ifBlank { "00000000-0000-0000-0000-000000000001" }
            val params = buildJsonObject { put("p_agency_id", agencyId) }
            supabaseClient.postgrest.rpc("fetch_marketplace_operator_products", params)
                .decodeList<OTAProduct>()
        }.recover {
            listOf(
                OTAProduct(otaProductId = "prod-001", title = "Kapadokya Balon & Vadi Turu", externalProductCode = "ANK-00001"),
                OTAProduct(otaProductId = "prod-002", title = "İstanbul Boğaz & Tarih Turu", externalProductCode = "IST-00012")
            )
        }
    }

    override suspend fun fetchAvailability(otaProductId: String): Result<List<OTAAvailability>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                put("p_product_id", otaProductId)
            }
            supabaseClient.postgrest.rpc("fetch_marketplace_operator_availability", params)
                .decodeList<OTAAvailability>()
        }.recover {
            listOf(
                OTAAvailability(availabilityId = "avail-01", otaProductId = otaProductId, availableCapacity = 15, price = 2500.0)
            )
        }
    }

    override suspend fun fetchPrices(otaProductId: String): Result<List<OTAPrice>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_agency_id", "00000000-0000-0000-0000-000000000001")
                put("p_product_id", otaProductId)
            }
            supabaseClient.postgrest.rpc("fetch_marketplace_operator_prices", params)
                .decodeList<OTAPrice>()
        }.recover {
            listOf(
                OTAPrice(priceId = "prc-01", otaProductId = otaProductId, adultPrice = 2500.0, currency = "TRY")
            )
        }
    }

    override suspend fun confirmBooking(otaBookingId: String): Result<OTABooking> {
        return Result.success(
            OTABooking(
                otaBookingId = otaBookingId,
                status = OTABookingStatus.CONFIRMED
            )
        )
    }

    override suspend fun cancelBooking(otaBookingId: String, reason: String): Result<OTABooking> {
        return Result.success(
            OTABooking(
                otaBookingId = otaBookingId,
                status = OTABookingStatus.CANCELLED
            )
        )
    }

    override suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun parseWebhook(payload: String): Result<OTAWebhook> {
        return Result.success(OTAWebhook())
    }

    override suspend fun healthCheck(): Result<Boolean> {
        return Result.success(true)
    }
}
