package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTAConnection
import com.mgacreative.touros.domain.model.ota.OTAPrice
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAReservation
import com.mgacreative.touros.domain.model.ota.OTAWebhook

/**
 * 4.5.2 OTA Entegrasyon Repozitovar Arayüzü (Repository Interface).
 */
interface OTARepository {
    suspend fun connect(account: OTAAccount): Result<OTAConnection>
    suspend fun disconnect(accountId: String): Result<Boolean>
    suspend fun syncBookings(accountId: String, tenantId: String): Result<List<OTABooking>>
    suspend fun syncAvailability(otaProductId: String, tenantId: String): Result<List<OTAAvailability>>
    suspend fun syncPrices(otaProductId: String, tenantId: String): Result<List<OTAPrice>>
    suspend fun syncProducts(accountId: String, tenantId: String): Result<List<OTAProduct>>
    suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String, tenantId: String): Result<Boolean>
    suspend fun confirmBooking(otaBookingId: String, tenantId: String): Result<OTABooking>
    suspend fun cancelBooking(otaBookingId: String, reason: String, tenantId: String): Result<OTABooking>
    suspend fun getReservations(otaBookingId: String, tenantId: String): Result<List<OTAReservation>>
    suspend fun processWebhook(payload: OTAWebhook, tenantId: String): Result<Boolean>
}
