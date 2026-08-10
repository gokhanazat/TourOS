package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 4.2.2 B2C Müşteri Mobil Checkout Talep Modeli.
 */
@Serializable
data class B2CCheckoutRequest(
    val tourId: String = "t101",
    val departureId: String = "dep-201",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val passengerEmail: String = "",
    val paxCount: Int = 1,
    val totalAmount: Double = 0.0,
    val cardNumberMasked: String = "**** **** **** 4242",
    val cardHolder: String = "",
    val cardExpiry: String = "",
    val cvv: String = ""
)

/**
 * 4.2.2 B2C Müşteri Mobil Checkout Sonuç Modeli.
 */
@Serializable
data class B2CCheckoutResult(
    val bookingId: String = "",
    val bookingCode: String = "",
    val paymentReference: String = "",
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "SUCCESS",
    val createdAt: String = "",
    val whatsappDirectUrl: String = "",
    val whatsappCustomerDirectUrl: String = ""
)
