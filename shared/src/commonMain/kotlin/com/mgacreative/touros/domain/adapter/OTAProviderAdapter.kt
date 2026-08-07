package com.mgacreative.touros.domain.adapter

import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTAPrice
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAWebhook

/**
 * 4.5.3 OTA Kanal Adaptörü Arayüzü (Adapter Interface).
 */
interface OTAProviderAdapter {
    val providerId: String
    val providerName: String

    suspend fun authenticate(account: OTAAccount): Result<Boolean>
    suspend fun fetchBookings(account: OTAAccount): Result<List<OTABooking>>
    suspend fun fetchProducts(account: OTAAccount): Result<List<OTAProduct>>
    suspend fun fetchAvailability(otaProductId: String): Result<List<OTAAvailability>>
    suspend fun fetchPrices(otaProductId: String): Result<List<OTAPrice>>
    suspend fun confirmBooking(otaBookingId: String): Result<OTABooking>
    suspend fun cancelBooking(otaBookingId: String, reason: String): Result<OTABooking>
    suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String): Result<Boolean>
    suspend fun parseWebhook(payload: String): Result<OTAWebhook>
    suspend fun healthCheck(): Result<Boolean>
}
