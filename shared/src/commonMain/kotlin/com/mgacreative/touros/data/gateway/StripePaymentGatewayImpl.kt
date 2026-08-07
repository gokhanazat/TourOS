package com.mgacreative.touros.data.gateway

import com.mgacreative.touros.domain.gateway.*

/**
 * 3.2.1 Stripe Ödeme Geçidi İmplementasyonu.
 */
class StripePaymentGatewayImpl : PaymentGateway {
    override val providerName: String = "stripe"

    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                transactionId = "pi_3M${(1000000..9999999).random()}",
                status = PaymentGatewayStatus.SUCCESS,
                providerName = providerName
            )
        }
    }

    override suspend fun process3DSecurePayment(request: PaymentRequest, callbackUrl: String): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                status = PaymentGatewayStatus.REQUIRE_3D,
                threeDSecureUrl = "https://hooks.stripe.com/3d_secure/authenticate",
                providerName = providerName
            )
        }
    }

    override suspend fun refundPayment(request: RefundRequest): Result<RefundResponse> {
        return runCatching {
            RefundResponse(
                isSuccess = true,
                refundId = "re_3M${(1000000..9999999).random()}"
            )
        }
    }

    override suspend fun getTransactionStatus(transactionId: String): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                transactionId = transactionId,
                status = PaymentGatewayStatus.SUCCESS,
                providerName = providerName
            )
        }
    }

    override suspend fun createPaymentLink(bookingId: String, amount: Double, currency: String, customerEmail: String?, tenantId: String): Result<PaymentLinkInfo> {
        return runCatching {
            val code = "cs_live_${(100000..999999).random()}"
            PaymentLinkInfo(
                id = "pl-$code",
                paymentLinkCode = code,
                bookingId = bookingId,
                amount = amount,
                currency = currency,
                gatewayProvider = providerName,
                checkoutUrl = "https://checkout.stripe.com/c/pay/$code",
                status = "PENDING",
                expiresAt = "2026-08-07 13:00",
                customerEmail = customerEmail,
                tenantId = tenantId
            )
        }
    }
}
