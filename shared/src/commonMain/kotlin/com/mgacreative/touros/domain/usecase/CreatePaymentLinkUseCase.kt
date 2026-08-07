package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.gateway.PaymentGatewayFactory
import com.mgacreative.touros.domain.gateway.PaymentLinkInfo

/**
 * 3.2.3 Link ile Ödeme Oluşturma Use Case.
 */
class CreatePaymentLinkUseCase(
    private val paymentGatewayFactory: PaymentGatewayFactory
) {
    suspend operator fun invoke(
        providerName: String,
        bookingId: String,
        amount: Double,
        currency: String = "TRY",
        customerEmail: String? = null,
        tenantId: String
    ): Result<PaymentLinkInfo> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Ödeme link tutarı 0'dan büyük olmalıdır."))
        }
        val gateway = paymentGatewayFactory.getGateway(providerName)
        return gateway.createPaymentLink(bookingId, amount, currency, customerEmail, tenantId)
    }
}
