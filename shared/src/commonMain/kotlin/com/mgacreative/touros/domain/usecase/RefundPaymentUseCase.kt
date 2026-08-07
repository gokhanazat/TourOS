package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.gateway.PaymentGatewayFactory
import com.mgacreative.touros.domain.gateway.RefundRequest
import com.mgacreative.touros.domain.gateway.RefundResponse

/**
 * 3.2.1 Ödeme İadesi Çekme Use Case.
 */
class RefundPaymentUseCase(
    private val paymentGatewayFactory: PaymentGatewayFactory
) {
    suspend operator fun invoke(providerName: String, request: RefundRequest): Result<RefundResponse> {
        val gateway = paymentGatewayFactory.getGateway(providerName)
        return gateway.refundPayment(request)
    }
}
