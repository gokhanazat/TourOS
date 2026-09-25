package com.mgacreative.touros.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 2. Aşama Sıfır-Mock Regresyon Testi:
 * BookingStateMachine ve Rezervasyon Yaşam Döngüsü Durum Geçişleri.
 * Hiçbir Mock/Fake kütüphanesi kullanılmadan saf durum modelleri ve gerçek mantık ile çalışır.
 */
class BookingStateMachinePhase2RegressionTest {

    // =========================================================================
    // 1. İZİNLİ DURUM GEÇİŞLERİ (ALLOWED STATE TRANSITIONS)
    // =========================================================================

    @Test
    fun testCompleteStandardLifecycleTransitions() {
        // Standart Rezervasyon Akışı: Bekliyor -> Opsiyon -> Onaylandı -> Tamamlandı
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.OPSIYON))
        val step1 = BookingStateMachine.transition(BookingStatus.BEKLIYOR, BookingStatus.OPSIYON)
        assertTrue(step1.isSuccess)
        assertEquals(BookingStatus.OPSIYON, step1.getOrNull())

        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.ONAYLANDI))
        val step2 = BookingStateMachine.transition(BookingStatus.OPSIYON, BookingStatus.ONAYLANDI)
        assertTrue(step2.isSuccess)
        assertEquals(BookingStatus.ONAYLANDI, step2.getOrNull())

        assertTrue(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.TAMAMLANDI))
        val step3 = BookingStateMachine.transition(BookingStatus.ONAYLANDI, BookingStatus.TAMAMLANDI)
        assertTrue(step3.isSuccess)
        assertEquals(BookingStatus.TAMAMLANDI, step3.getOrNull())
    }

    @Test
    fun testDirectConfirmationAndRollbackTransitions() {
        // Doğrudan Onay: Bekliyor -> Onaylandı
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.ONAYLANDI))

        // Opsiyondan Geri Çekilme: Opsiyon -> Bekliyor
        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.BEKLIYOR))

        // Onaylanmış rezervasyonun revizyon için Bekliyor'a alınması
        assertTrue(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.BEKLIYOR))

        // İptal akışları
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.IPTAL))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.IPTAL))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.IPTAL))
    }

    @Test
    fun testSelfTransitionsAreAlwaysValid() {
        // Durumun kendisine geçişi her zaman geçerli ve stabil olmalıdır
        for (status in BookingStatus.entries) {
            assertTrue(
                BookingStateMachine.canTransition(status, status),
                "Durum '${status.name}' kendisine geçişte true dönmelidir."
            )
            val result = BookingStateMachine.transition(status, status)
            assertTrue(result.isSuccess)
            assertEquals(status, result.getOrNull())
        }
    }

    // =========================================================================
    // 2. KESİN YASAKLI GEÇİŞLER (FORBIDDEN STATE TRANSITIONS)
    // =========================================================================

    @Test
    fun testStrictlyForbiddenTransitions() {
        // a) İptal edilen bir rezervasyon doğrudan Onaylandı yapılamaz (Reddedilmeli)
        assertFalse(BookingStateMachine.canTransition(BookingStatus.IPTAL, BookingStatus.ONAYLANDI))
        val cancelToConfirmed = BookingStateMachine.transition(BookingStatus.IPTAL, BookingStatus.ONAYLANDI)
        assertTrue(cancelToConfirmed.isFailure)
        assertTrue(cancelToConfirmed.exceptionOrNull() is IllegalStateException)

        // b) İptal edilen bir rezervasyon doğrudan Tamamlandı veya Opsiyon yapılamaz
        assertFalse(BookingStateMachine.canTransition(BookingStatus.IPTAL, BookingStatus.TAMAMLANDI))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.IPTAL, BookingStatus.OPSIYON))

        // c) Tamamlanan bir rezervasyon doğrudan İptal veya Opsiyon yapılamaz (Reddedilmeli)
        assertFalse(BookingStateMachine.canTransition(BookingStatus.TAMAMLANDI, BookingStatus.IPTAL))
        val completedToCancel = BookingStateMachine.transition(BookingStatus.TAMAMLANDI, BookingStatus.IPTAL)
        assertTrue(completedToCancel.isFailure)

        assertFalse(BookingStateMachine.canTransition(BookingStatus.TAMAMLANDI, BookingStatus.OPSIYON))
        val completedToOption = BookingStateMachine.transition(BookingStatus.TAMAMLANDI, BookingStatus.OPSIYON)
        assertTrue(completedToOption.isFailure)

        // d) Onaylanmış bir rezervasyon geriye dönük Opsiyonlu statüsüne düşürülemez
        assertFalse(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.OPSIYON))
        val confirmedToOption = BookingStateMachine.transition(BookingStatus.ONAYLANDI, BookingStatus.OPSIYON)
        assertTrue(confirmedToOption.isFailure)
    }

    // =========================================================================
    // 3. BOOKING DOMAIN MODEL VE DURUM ENTEGRASYONU
    // =========================================================================

    @Test
    fun testBookingModelTransitionAndAllowedStatuses() {
        val activeBooking = Booking(
            id = "bkg-101",
            bookingCode = "REZ-101",
            status = BookingStatus.BEKLIYOR
        )

        // Model üzerinden geçiş yetkisi
        assertTrue(activeBooking.canTransitionTo(BookingStatus.ONAYLANDI))
        assertTrue(activeBooking.canTransitionTo(BookingStatus.OPSIYON))
        assertTrue(activeBooking.canTransitionTo(BookingStatus.IPTAL))

        val allowedList = activeBooking.allowedNextStatuses
        assertTrue(allowedList.contains(BookingStatus.ONAYLANDI))
        assertTrue(allowedList.contains(BookingStatus.OPSIYON))
        assertTrue(allowedList.contains(BookingStatus.IPTAL))

        // İptal edilmiş modelin izinli durumları
        val cancelledBooking = activeBooking.copy(status = BookingStatus.IPTAL)
        assertFalse(cancelledBooking.canTransitionTo(BookingStatus.ONAYLANDI))
        assertFalse(cancelledBooking.canTransitionTo(BookingStatus.TAMAMLANDI))
        assertFalse(cancelledBooking.canTransitionTo(BookingStatus.OPSIYON))
        // İptal edilmiş rezervasyon yalnızca yeniden Bekliyor'a alınabilir
        assertTrue(cancelledBooking.canTransitionTo(BookingStatus.BEKLIYOR))
    }
}
