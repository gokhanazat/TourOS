package com.mgacreative.touros.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 9 Kanonik Tur Operatörü Kuralı ve Alias Çözümleme Regresyon Testleri.
 * Gerçek sınıflar ve verilerle, Mock kullanılmadan çalışır.
 */
class TourOperatorConfigRegressionTest {

    @Test
    fun testExactAllowedOperatorNamesAreValid() {
        assertEquals(9, TourOperatorConfig.ALLOWED_OPERATOR_NAMES.size)

        for (operator in TourOperatorConfig.ALLOWED_OPERATOR_NAMES) {
            assertTrue(TourOperatorConfig.isAllowedOperator(operator), "Operatör izinli olmalı: $operator")
            assertEquals(operator, TourOperatorConfig.resolveCanonicalOperatorName(operator))
        }
    }

    @Test
    fun testRussianAndLatinAliasesResolution() {
        // Coral Travel
        assertEquals("Coral Travel", TourOperatorConfig.resolveCanonicalOperatorName("coral travel"))
        assertEquals("Coral Travel", TourOperatorConfig.resolveCanonicalOperatorName("ООО ТО Корал Тревел"))

        // Pegas Touristik
        assertEquals("Pegas Touristik", TourOperatorConfig.resolveCanonicalOperatorName("pegas"))
        assertEquals("Pegas Touristik", TourOperatorConfig.resolveCanonicalOperatorName("Пегас Туристик"))

        // Bibloglobus
        assertEquals("Bibloglobus", TourOperatorConfig.resolveCanonicalOperatorName("библио глобус"))
        assertEquals("Bibloglobus", TourOperatorConfig.resolveCanonicalOperatorName("biblio-globus"))

        // Anex
        assertEquals("Anex", TourOperatorConfig.resolveCanonicalOperatorName("anex tour"))
        assertEquals("Anex", TourOperatorConfig.resolveCanonicalOperatorName("Анекс"))

        // Fun&Sun
        assertEquals("Fun&Sun (Ru)", TourOperatorConfig.resolveCanonicalOperatorName("funsun"))
        assertEquals("Fun&Sun (Ru)", TourOperatorConfig.resolveCanonicalOperatorName("TUI Russia"))
        assertEquals("Fun&Sun (Ru)", TourOperatorConfig.resolveCanonicalOperatorName("Фан Сан"))

        // Intourist
        assertEquals("Интурист", TourOperatorConfig.resolveCanonicalOperatorName("intourist"))
        assertEquals("Интурист", TourOperatorConfig.resolveCanonicalOperatorName("НТК Интурист"))

        // Kazunion & Loti & Sunmar
        assertEquals("Kazunion", TourOperatorConfig.resolveCanonicalOperatorName("Казунион"))
        assertEquals("Loti", TourOperatorConfig.resolveCanonicalOperatorName("loti premium"))
        assertEquals("Sunmar", TourOperatorConfig.resolveCanonicalOperatorName("Санмар"))
    }

    @Test
    fun testDisallowedOperatorsAreFilteredOut() {
        assertFalse(TourOperatorConfig.isAllowedOperator(null))
        assertFalse(TourOperatorConfig.isAllowedOperator(""))
        assertFalse(TourOperatorConfig.isAllowedOperator("   "))
        assertFalse(TourOperatorConfig.isAllowedOperator("Tez Tour"))
        assertFalse(TourOperatorConfig.isAllowedOperator("Mouzenidis Travel"))
        assertFalse(TourOperatorConfig.isAllowedOperator("Bozaci Travel Agency"))

        assertNull(TourOperatorConfig.resolveCanonicalOperatorName("Unknown Operator"))
    }
}
