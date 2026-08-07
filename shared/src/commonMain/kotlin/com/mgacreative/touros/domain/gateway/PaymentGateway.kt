package com.mgacreative.touros.domain.gateway

/**
 * 3.2.1 Sağlayıcı Bağımsız Ödeme Geçidi (PaymentGateway) Soyut Arayüzü.
 * İleride İyzico, Stripe, PayTR, Garanti POS gibi herhangi bir sağlayıcı takılıp çıkarılabilir.
 */
interface PaymentGateway {
    val providerName: String

    suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse>
    suspend fun process3DSecurePayment(request: PaymentRequest, callbackUrl: String): Result<PaymentResponse>
    suspend fun refundPayment(request: RefundRequest): Result<RefundResponse>
    suspend fun getTransactionStatus(transactionId: String): Result<PaymentResponse>
    suspend fun createPaymentLink(bookingId: String, amount: Double, currency: String, customerEmail: String?, tenantId: String): Result<PaymentLinkInfo>
}
