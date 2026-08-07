package com.mgacreative.touros.data.gateway

import com.mgacreative.touros.domain.gateway.*

/**
 * 3.2.1 Mock Test Ödeme Geçidi İmplementasyonu.
 */
class MockPaymentGatewayImpl : PaymentGateway {
    override val providerName: String = "mock"

    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                transactionId = "MOCK-TX-${(10000..99999).random()}",
                status = PaymentGatewayStatus.SUCCESS,
                providerName = providerName
            )
        }
    }

    override suspend fun process3DSecurePayment(request: PaymentRequest, callbackUrl: String): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                status = PaymentGatewayStatus.SUCCESS,
                providerName = providerName
            )
        }
    }

    override suspend fun refundPayment(request: RefundRequest): Result<RefundResponse> {
        return runCatching {
            RefundResponse(
                isSuccess = true,
                refundId = "MOCK-REF-${(10000..99999).random()}"
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
            val code = "mock_link_${(10000..99999).random()}"
            PaymentLinkInfo(
                id = "pl-$code",
                paymentLinkCode = code,
                bookingId = bookingId,
                amount = amount,
                currency = currency,
                gatewayProvider = providerName,
                checkoutUrl = "https://pay.touros.app/mock/$code",
                status = "PENDING",
                expiresAt = "2026-08-07 13:00",
                customerEmail = customerEmail,
                tenantId = tenantId
            )
        }
    }
}
