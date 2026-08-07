package com.mgacreative.touros.data.adapter

import com.mgacreative.touros.domain.adapter.OTAProviderAdapter
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAAvailability
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTABookingStatus
import com.mgacreative.touros.domain.model.ota.OTAPrice
import com.mgacreative.touros.domain.model.ota.OTAProduct
import com.mgacreative.touros.domain.model.ota.OTAWebhook

/**
 * 4.5.3 HotelBeds Adaptör Taslağı (Stub Implementation).
 */
class HotelBedsAdapter : OTAProviderAdapter {
    override val providerId: String = "hotelbeds"
    override val providerName: String = "HotelBeds"

    override suspend fun authenticate(account: OTAAccount): Result<Boolean> = Result.success(true)
    override suspend fun fetchBookings(account: OTAAccount): Result<List<OTABooking>> = Result.success(emptyList())
    override suspend fun fetchProducts(account: OTAAccount): Result<List<OTAProduct>> = Result.success(emptyList())
    override suspend fun fetchAvailability(otaProductId: String): Result<List<OTAAvailability>> = Result.success(emptyList())
    override suspend fun fetchPrices(otaProductId: String): Result<List<OTAPrice>> = Result.success(emptyList())
    override suspend fun confirmBooking(otaBookingId: String): Result<OTABooking> = Result.success(OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CONFIRMED))
    override suspend fun cancelBooking(otaBookingId: String, reason: String): Result<OTABooking> = Result.success(OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CANCELLED))
    override suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String): Result<Boolean> = Result.success(true)
    override suspend fun parseWebhook(payload: String): Result<OTAWebhook> = Result.success(OTAWebhook())
    override suspend fun healthCheck(): Result<Boolean> = Result.success(true)
}
