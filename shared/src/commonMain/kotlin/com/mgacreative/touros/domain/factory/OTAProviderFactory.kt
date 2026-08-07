package com.mgacreative.touros.domain.factory

import com.mgacreative.touros.domain.adapter.OTAProviderAdapter

/**
 * 4.5.3 OTA Kanal Adaptör Fabrikası Arayüzü (Factory Interface).
 */
interface OTAProviderFactory {
    fun getAdapter(providerId: String): OTAProviderAdapter
    fun getSupportedProviders(): List<String>
}
