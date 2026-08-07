package com.mgacreative.touros.data.gateway

import com.mgacreative.touros.domain.gateway.*

/**
 * 3.2.1 İyzico Ödeme Geçidi İmplementasyonu.
 */
class IyzicoPaymentGatewayImpl : PaymentGateway {
    override val providerName: String = "iyzico"

    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        return runCatching {
            // İyzico API Entegrasyon Simülasyonu
            if (request.cardNumber.endsWith("0000")) {
                PaymentResponse(
                    isSuccess = false,
                    status = PaymentGatewayStatus.FAILED,
                    errorCode = "ERR_CARD_LIMIT",
                    errorMessage = "İyzico: Kart bakiyesi yetersiz.",
                    providerName = providerName
                )
            } else {
                PaymentResponse(
                    isSuccess = true,
                    transactionId = "IYZI-TX-${(100000..999999).random()}",
                    status = PaymentGatewayStatus.SUCCESS,
                    providerName = providerName
                )
            }
        }
    }

    override suspend fun process3DSecurePayment(request: PaymentRequest, callbackUrl: String): Result<PaymentResponse> {
        return runCatching {
            PaymentResponse(
                isSuccess = true,
                status = PaymentGatewayStatus.REQUIRE_3D,
                threeDSecureUrl = "https://sandbox-pp.iyzipay.com/auth/3d/initialize",
                providerName = providerName
            )
        }
    }

    override suspend fun refundPayment(request: RefundRequest): Result<RefundResponse> {
        return runCatching {
            RefundResponse(
                isSuccess = true,
                refundId = "IYZI-REF-${(100000..999999).random()}"
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
            val code = "iyzi_link_${(100000..999999).random()}"
            PaymentLinkInfo(
                id = "pl-$code",
                paymentLinkCode = code,
                bookingId = bookingId,
                amount = amount,
                currency = currency,
                gatewayProvider = providerName,
                checkoutUrl = "https://pay.iyzipay.com/link/$code",
                status = "PENDING",
                expiresAt = "2026-08-07 13:00",
                customerEmail = customerEmail,
                tenantId = tenantId
            )
        }
    }
}
