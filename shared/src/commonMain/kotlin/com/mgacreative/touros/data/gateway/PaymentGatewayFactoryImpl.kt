package com.mgacreative.touros.data.gateway

import com.mgacreative.touros.domain.gateway.PaymentGateway
import com.mgacreative.touros.domain.gateway.PaymentGatewayFactory

class PaymentGatewayFactoryImpl(
    private val iyzicoGateway: IyzicoPaymentGatewayImpl,
    private val stripeGateway: StripePaymentGatewayImpl,
    private val mockGateway: MockPaymentGatewayImpl
) : PaymentGatewayFactory {

    override fun getGateway(providerName: String): PaymentGateway {
        return when (providerName.lowercase()) {
            "iyzico" -> iyzicoGateway
            "stripe" -> stripeGateway
            "mock" -> mockGateway
            else -> iyzicoGateway
        }
    }

    override fun getAvailableProviders(): List<String> {
        return listOf("iyzico", "stripe", "mock")
    }
}
