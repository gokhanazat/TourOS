package com.mgacreative.touros.domain.gateway

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentGatewayStatus {
    SUCCESS,
    FAILED,
    REQUIRE_3D,
    PENDING,
    REFUNDED
}

@Serializable
data class PaymentRequest(
    val bookingId: String = "",
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expireMonth: String = "",
    val expireYear: String = "",
    val cvc: String = "",
    val installment: Int = 1,
    val customerEmail: String = "",
    val customerIp: String = "127.0.0.1",
    val tenantId: String = ""
)

@Serializable
data class PaymentResponse(
    val isSuccess: Boolean = false,
    val transactionId: String? = null,
    val status: PaymentGatewayStatus = PaymentGatewayStatus.PENDING,
    val threeDSecureUrl: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val providerName: String = "Mock",
    val rawResponse: String? = null
)

@Serializable
data class RefundRequest(
    val transactionId: String = "",
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val reason: String = "Müşteri Talebi"
)

@Serializable
data class RefundResponse(
    val isSuccess: Boolean = false,
    val refundId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
