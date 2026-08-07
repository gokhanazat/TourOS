package com.mgacreative.touros.data.factory

import com.mgacreative.touros.data.adapter.BookingAdapter
import com.mgacreative.touros.data.adapter.ExpediaAdapter
import com.mgacreative.touros.data.adapter.GetYourGuideAdapter
import com.mgacreative.touros.data.adapter.HotelBedsAdapter
import com.mgacreative.touros.data.adapter.ViatorAdapter
import com.mgacreative.touros.domain.adapter.OTAProviderAdapter
import com.mgacreative.touros.domain.factory.OTAProviderFactory

/**
 * 4.5.3 OTAProviderFactory Implementasyonu.
 * Viator, GetYourGuide, HotelBeds, Booking.com ve Expedia adaptörlerini üretir.
 */
class OTAProviderFactoryImpl : OTAProviderFactory {

    private val adapters: Map<String, OTAProviderAdapter> = mapOf(
        "viator" to ViatorAdapter(),
        "getyourguide" to GetYourGuideAdapter(),
        "hotelbeds" to HotelBedsAdapter(),
        "booking" to BookingAdapter(),
        "expedia" to ExpediaAdapter()
    )

    override fun getAdapter(providerId: String): OTAProviderAdapter {
        return adapters[providerId.lowercase()]
            ?: throw IllegalArgumentException("Desteklenmeyen OTA Kanalı: $providerId")
    }

    override fun getSupportedProviders(): List<String> {
        return adapters.keys.toList()
    }
}
