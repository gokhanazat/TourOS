package com.mgacreative.touros.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingStateMachineTest {

    @Test
    fun testBekliyorValidTransitions() {
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.OPSIYON))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.ONAYLANDI))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.IPTAL))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.BEKLIYOR, BookingStatus.TAMAMLANDI))
    }

    @Test
    fun testOpsiyonValidTransitions() {
        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.ONAYLANDI))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.IPTAL))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.BEKLIYOR))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.OPSIYON, BookingStatus.TAMAMLANDI))
    }

    @Test
    fun testOnaylandiValidTransitions() {
        assertTrue(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.TAMAMLANDI))
        assertTrue(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.IPTAL))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.BEKLIYOR))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.ONAYLANDI, BookingStatus.OPSIYON))
    }

    @Test
    fun testTerminalStates() {
        // İptal edilen rezervasyondan başka duruma geçilemez
        assertFalse(BookingStateMachine.canTransition(BookingStatus.IPTAL, BookingStatus.ONAYLANDI))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.IPTAL, BookingStatus.BEKLIYOR))

        // Tamamlanan rezervasyondan başka duruma geçilemez
        assertFalse(BookingStateMachine.canTransition(BookingStatus.TAMAMLANDI, BookingStatus.IPTAL))
        assertFalse(BookingStateMachine.canTransition(BookingStatus.TAMAMLANDI, BookingStatus.ONAYLANDI))
    }

    @Test
    fun testTransitionMethod() {
        val result = BookingStateMachine.transition(BookingStatus.BEKLIYOR, BookingStatus.ONAYLANDI)
        assertTrue(result.isSuccess)
        assertEquals(BookingStatus.ONAYLANDI, result.getOrNull())

        val invalidResult = BookingStateMachine.transition(BookingStatus.IPTAL, BookingStatus.ONAYLANDI)
        assertTrue(invalidResult.isFailure)
    }
}
