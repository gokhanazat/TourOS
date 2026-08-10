package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.data.database.entity.AgencyBrandingEntity
import com.mgacreative.touros.data.service.WhatsAppNotificationServiceImpl
import com.mgacreative.touros.domain.model.B2CCheckoutRequest
import com.mgacreative.touros.domain.model.B2CCheckoutResult
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.NotificationChannel
import com.mgacreative.touros.domain.model.NotificationPayload
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 4.2.2 B2C Müşteri Mobil/Web Rezervasyon ve Ödeme İşleme Use Case.
 * Web veya mobil kanaldan gelen tüm rezervasyonları BookingRepository üzerinden kaydederek
 * acente paneline 'Bekliyor' statüsünde düşmesini ve YALNIZCA WHATSAPP BİLDİRİMİ ATILMASINI SAĞLAR.
 */
class ProcessB2CCheckoutUseCase(
    private val bookingRepository: BookingRepository,
    private val whatsAppNotificationService: WhatsAppNotificationServiceImpl? = null,
    private val supabaseClient: SupabaseClient? = null
) {
    suspend operator fun invoke(
        request: B2CCheckoutRequest,
        tenantId: String,
        customerId: String = "cust-101"
    ): Result<B2CCheckoutResult> {
        if (request.passengerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Yolcu adı boş bırakılamaz."))
        }

        return runCatching {
            val bookingCode = "REZ-2026-${(1000..9999).random()}"
            val paymentRef = "PAY-3DS-${(100000..999999).random()}"

            val booking = Booking(
                bookingCode = bookingCode,
                departureId = request.departureId.ifBlank { "00000000-0000-0000-0000-000000000001" },
                customerId = customerId,
                customerName = request.passengerName,
                customerEmail = request.passengerEmail,
                customerPhone = request.passengerPhone,
                totalPrice = if (request.totalAmount > 0) request.totalAmount else 5000.0 * request.paxCount,
                currency = "TRY",
                paxCount = request.paxCount,
                status = BookingStatus.BEKLIYOR,
                notes = "B2C Web/Mobil Rezervasyon | Ödeme Yöntemi: ${request.cardNumberMasked}",
                tenantId = tenantId
            )

            val created = bookingRepository.createBooking(booking).getOrThrow()

            // Firma / Acente İletişim Telefonunu Çek (Öncelik: agency_branding.whatsapp_number -> contact_phone -> agencies.contact_phone -> default)
            var companyPhone = "+905550000000"

            runCatching {
                if (supabaseClient != null) {
                    val brandingList = supabaseClient.postgrest.from("agency_branding")
                        .select { filter { eq("agency_id", tenantId) } }
                        .decodeList<AgencyBrandingEntity>()
                    val branding = brandingList.firstOrNull()
                    val brandingNum = branding?.whatsappNumber?.takeIf { it.isNotBlank() }
                        ?: branding?.contactPhone?.takeIf { it.isNotBlank() }

                    if (brandingNum != null) {
                        companyPhone = brandingNum
                    } else {
                        val agencyList = supabaseClient.postgrest.from("agencies")
                            .select { filter { eq("tenant_id", tenantId) } }
                            .decodeList<com.mgacreative.touros.domain.model.B2BAgencyProfile>()
                        val agency = agencyList.firstOrNull()
                        if (agency != null && agency.contactPhone.isNotBlank()) {
                            companyPhone = agency.contactPhone
                        }
                    }
                }
            }

            // 💬 1. FİRMA / ACENTE YETKİLİSİNE WHATSAPP BİLDİRİMİ
            runCatching {
                whatsAppNotificationService?.sendNotification(
                    payload = NotificationPayload(
                        recipient = companyPhone,
                        channel = NotificationChannel.WHATSAPP,
                        title = "🚨 Yeni Rezervasyon Düştü!",
                        content = "Sayın Yetkili, Mağazanız üzerinden ${created.customerName} adına ${created.bookingCode} kodlu yeni rezervasyon yapılmıştır. Kişi: ${created.paxCount} Pax, Toplam Tutar: ${created.totalPrice} ${created.currency}."
                    ),
                    tenantId = tenantId
                )
            }

            // 💬 2. MÜŞTERİYE (YOLCUYA) WHATSAPP BİLDİRİMİ
            if (request.passengerPhone.isNotBlank()) {
                runCatching {
                    whatsAppNotificationService?.sendNotification(
                        payload = NotificationPayload(
                            recipient = request.passengerPhone,
                            channel = NotificationChannel.WHATSAPP,
                            title = "🔔 Rezervasyonunuz Alındı",
                            content = "Sayın ${created.customerName}, TourOS üzerinden yaptığınız ${created.bookingCode} kodlu rezervasyonunuz başarıyla alınmıştır. Toplam Tutar: ${created.totalPrice} ${created.currency}."
                        ),
                        tenantId = tenantId
                    )
                }
            }

            val cleanPhone = companyPhone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").replace("+", "").trim()
            val formattedPhone = when {
                cleanPhone.startsWith("90") -> cleanPhone
                cleanPhone.startsWith("0") -> "90${cleanPhone.removePrefix("0")}"
                cleanPhone.length == 10 -> "90$cleanPhone"
                else -> cleanPhone
            }
            val directUrl = "https://wa.me/$formattedPhone?text=Yeni%20Rezervasyon!%20Kod:%20${created.bookingCode}%20M%C3%BC%C5%9Fteri:%20${created.customerName}%20Pax:%20${created.paxCount}%20Tutar:%20${created.totalPrice}%20TRY"

            var customerDirectUrl = ""
            if (request.passengerPhone.isNotBlank()) {
                val cleanCustPhone = request.passengerPhone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").replace("+", "").trim()
                val formattedCustPhone = when {
                    cleanCustPhone.startsWith("90") -> cleanCustPhone
                    cleanCustPhone.startsWith("0") -> "90${cleanCustPhone.removePrefix("0")}"
                    cleanCustPhone.length == 10 -> "90$cleanCustPhone"
                    else -> cleanCustPhone
                }
                customerDirectUrl = "https://wa.me/$formattedCustPhone?text=Say%C4%B1n%20${created.customerName},%20TourOS%20%C3%BCzerinden%20${created.bookingCode}%20kodlu%20rezervasyonunuz%20al%C4%B1nm%C4%B1%C5%9Ft%C4%B1r.%20Pax:%20${created.paxCount},%20Tutar:%20${created.totalPrice}%20TRY"
            }

            B2CCheckoutResult(
                bookingId = created.id,
                bookingCode = created.bookingCode,
                paymentReference = paymentRef,
                totalAmount = created.totalPrice,
                paymentStatus = "SUCCESS",
                createdAt = created.createdAt.ifBlank { "2026-08-09 12:30" },
                whatsappDirectUrl = directUrl,
                whatsappCustomerDirectUrl = customerDirectUrl
            )
        }
    }
}
