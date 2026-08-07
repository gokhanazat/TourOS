package com.mgacreative.touros.domain.gateway

/**
 * 3.2.1 Sağlayıcı Seçici Factory Arayüzü.
 */
interface PaymentGatewayFactory {
    fun getGateway(providerName: String = "iyzico"): PaymentGateway
    fun getAvailableProviders(): List<String>
}
