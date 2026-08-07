package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.gateway.PaymentGatewayFactory
import com.mgacreative.touros.domain.gateway.PaymentRequest
import com.mgacreative.touros.domain.gateway.PaymentResponse

/**
 * 3.2.1 Sağlayıcı Bağımsız Ödeme Çekme Use Case.
 */
class ExecutePaymentUseCase(
    private val paymentGatewayFactory: PaymentGatewayFactory
) {
    suspend operator fun invoke(providerName: String, request: PaymentRequest): Result<PaymentResponse> {
        if (request.amount <= 0) {
            return Result.failure(IllegalArgumentException("Ödeme tutarı 0'dan büyük olmalıdır."))
        }
        val gateway = paymentGatewayFactory.getGateway(providerName)
        return gateway.processPayment(request)
    }
}
