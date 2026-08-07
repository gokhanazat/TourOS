package com.mgacreative.touros.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 4.5.8 Ktor tabanlı network katmanı.
 * Her OTA adaptörü (Viator, GYG, HotelBeds, Booking, Expedia) kendi HttpClient / Base URL / Auth başlıklarını bu fabrika ile yapılandırır.
 */
object OtaHttpClientFactory {

    fun createClient(baseUrl: String, apiKey: String, apiSecret: String? = null): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            defaultRequest {
                url(baseUrl)
                header("X-API-KEY", apiKey)
                header("Accept", "application/json")
                header("Content-Type", "application/json")
                if (!apiSecret.isNullOrEmpty()) {
                    header("X-API-SECRET", apiSecret)
                }
            }
        }
    }
}
