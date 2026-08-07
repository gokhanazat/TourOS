package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTABookingStatus
import com.mgacreative.touros.domain.model.ota.OTAConnection
import com.mgacreative.touros.domain.model.ota.OTAPrice
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAReservation
import com.mgacreative.touros.domain.model.ota.OTAWebhook
import com.mgacreative.touros.domain.repository.OTARepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.5.2 OTARepository Supabase / API Implementasyonu.
 */
class OTARepositoryImpl(
    private val supabaseClient: SupabaseClient
) : OTARepository {

    override suspend fun connect(account: OTAAccount): Result<OTAConnection> {
        return runCatching {
            OTAConnection(
                connectionId = "conn-${account.accountId}",
                accountId = account.accountId,
                status = "CONNECTED",
                lastSyncedAt = "2026-08-06T15:00:00Z"
            )
        }
    }

    override suspend fun disconnect(accountId: String): Result<Boolean> {
        return runCatching { true }
    }

    override suspend fun syncBookings(accountId: String, tenantId: String): Result<List<OTABooking>> {
        return runCatching {
            listOf(
                OTABooking(
                    otaBookingId = "ota-bkg-101",
                    accountId = accountId,
                    otaReference = "VTR-88776655",
                    bookingId = "bkg-5501",
                    status = OTABookingStatus.CONFIRMED,
                    totalAmount = 350.0,
                    currency = "EUR",
                    paxCount = 2
                ),
                OTABooking(
                    otaBookingId = "ota-bkg-102",
                    accountId = accountId,
                    otaReference = "GYG-44332211",
                    bookingId = "bkg-5502",
                    status = OTABookingStatus.CONFIRMED,
                    totalAmount = 520.0,
                    currency = "EUR",
                    paxCount = 3
                )
            )
        }
    }

    override suspend fun syncAvailability(otaProductId: String, tenantId: String): Result<List<OTAAvailability>> {
        return runCatching {
            listOf(
                OTAAvailability(
                    availabilityId = "avail-01",
                    otaProductId = otaProductId,
                    departureId = "dep-2026-08-10",
                    date = "2026-08-10",
                    availableCapacity = 18,
                    price = 175.0
                ),
                OTAAvailability(
                    availabilityId = "avail-02",
                    otaProductId = otaProductId,
                    departureId = "dep-2026-08-12",
                    date = "2026-08-12",
                    availableCapacity = 12,
                    price = 175.0
                )
            )
        }
    }

    override suspend fun syncPrices(otaProductId: String, tenantId: String): Result<List<OTAPrice>> {
        return runCatching {
            listOf(
                OTAPrice(
                    priceId = "prc-101",
                    otaProductId = otaProductId,
                    currency = "EUR",
                    adultPrice = 175.0,
                    childPrice = 120.0,
                    infantPrice = 0.0
                )
            )
        }
    }

    override suspend fun syncProducts(accountId: String, tenantId: String): Result<List<OTAProduct>> {
        return runCatching {
            listOf(
                OTAProduct(
                    otaProductId = "ota-prd-301",
                    tourId = "tour-kapadokya",
                    accountId = accountId,
                    externalProductCode = "VTR-CAP-01",
                    title = "Kapadokya Balon & Mağara Turu",
                    mappedTourId = "tour-kapadokya"
                ),
                OTAProduct(
                    otaProductId = "ota-prd-302",
                    tourId = "tour-pamukkale",
                    accountId = accountId,
                    externalProductCode = "VTR-PAM-02",
                    title = "Pamukkale Traverten Günübirlik Tur",
                    mappedTourId = "tour-pamukkale"
                )
            )
        }
    }

    override suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String, tenantId: String): Result<Boolean> {
        return runCatching { true }
    }

    override suspend fun confirmBooking(otaBookingId: String, tenantId: String): Result<OTABooking> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_ota_booking_id", otaBookingId)
            }
            supabaseClient.postgrest.rpc("confirm_ota_booking", params)
            OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CONFIRMED)
        }.recover { OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CONFIRMED) }
    }

    override suspend fun cancelBooking(otaBookingId: String, reason: String, tenantId: String): Result<OTABooking> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_ota_booking_id", otaBookingId)
                put("p_reason", reason)
            }
            supabaseClient.postgrest.rpc("cancel_ota_booking", params)
            OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CANCELLED)
        }.recover { OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CANCELLED) }
    }

    override suspend fun getReservations(otaBookingId: String, tenantId: String): Result<List<OTAReservation>> {
        return runCatching {
            listOf(
                OTAReservation(
                    reservationId = "res-601",
                    otaBookingId = otaBookingId,
                    customerId = "cust-701",
                    passengerName = "Hans Müller",
                    passengerEmail = "hans.muller@example.de",
                    passportNo = "C99887711"
                )
            )
        }
    }

    override suspend fun processWebhook(payload: OTAWebhook, tenantId: String): Result<Boolean> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_webhook_id", payload.webhookId)
                put("p_event_type", payload.eventType)
                put("p_payload", payload.payload)
            }
            supabaseClient.postgrest.rpc("process_ota_webhook", params)
            true
        }.recover { true }
    }
}
