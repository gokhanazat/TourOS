package com.mgacreative.touros.data.factory

import com.mgacreative.touros.data.adapter.BookingAdapter
import com.mgacreative.touros.data.adapter.ExpediaAdapter
import com.mgacreative.touros.data.adapter.GetYourGuideAdapter
import com.mgacreative.touros.data.adapter.HotelBedsAdapter
import com.mgacreative.touros.data.adapter.ViatorAdapter
import com.mgacreative.touros.domain.adapter.OTAProviderAdapter
import com.mgacreative.touros.domain.factory.OTAProviderFactory

import com.mgacreative.touros.data.adapter.InternalOperatorAdapter
import io.github.jan.supabase.SupabaseClient

/**
 * 4.5.3 & 4.6.5 OTAProviderFactory Implementasyonu.
 * Viator, GetYourGuide, HotelBeds, Booking.com, Expedia ve InternalOperator adaptörlerini üretir.
 */
class OTAProviderFactoryImpl(
    private val supabaseClient: SupabaseClient? = null
) : OTAProviderFactory {

    private val adapters: Map<String, OTAProviderAdapter> by lazy {
        val map = mutableMapOf<String, OTAProviderAdapter>(
            "viator" to ViatorAdapter(),
            "getyourguide" to GetYourGuideAdapter(),
            "hotelbeds" to HotelBedsAdapter(),
            "booking" to BookingAdapter(),
            "expedia" to ExpediaAdapter()
        )
        supabaseClient?.let { client ->
            map["internal_operator"] = InternalOperatorAdapter(client)
        }
        map
    }

    override fun getAdapter(providerId: String): OTAProviderAdapter {
        return adapters[providerId.lowercase()]
            ?: adapters["internal_operator"]
            ?: ViatorAdapter()
    }

    override fun getSupportedProviders(): List<String> {
        return adapters.keys.toList()
    }
}
