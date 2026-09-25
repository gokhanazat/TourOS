package com.mgacreative.touros.utils

import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.domain.model.TourOperatorConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 4. Aşama Sıfır-Mock Regresyon Testi:
 * Sayfalama (Pagination), Limit/Offset Parametre Mantığı ve Yüksek Hacimli Veri Filtreleme Kuralları.
 * Sıfır Mock yaklaşımıyla gerçek veri modelleri ve saf filtreleme/sıralama algoritmaları üzerinden çalışır.
 */
class PaginationAndHighVolumeFilterRegressionTest {

    // =========================================================================
    // a) SAYFALAMA VE LİMİT PARAMETRELERİ (BOUNDARY RESILIENCE)
    // =========================================================================

    /**
     * UI ve API'de kullanılan güvenli sayfalama hesaplayıcısı
     */
    private data class PaginationResult<T>(
        val totalItems: Int,
        val totalPages: Int,
        val safeCurrentPage: Int,
        val pageSize: Int,
        val fromIndex: Int,
        val toIndex: Int,
        val pagedItems: List<T>
    )

    private fun <T> calculateSafePagination(
        items: List<T>,
        requestedPage: Int,
        requestedPageSize: Int
    ): PaginationResult<T> {
        val total = items.size
        // Negatif veya 0 pageSize durumunda güvenli taban 15'e çekilir (Zero-Division önlemi)
        val safePageSize = if (requestedPageSize > 0) requestedPageSize else 15
        val totalPages = maxOf(1, (total + safePageSize - 1) / safePageSize)
        val safePage = requestedPage.coerceIn(1, totalPages)

        val fromIdx = if (total == 0) 0 else (safePage - 1) * safePageSize + 1
        val toIdx = minOf(safePage * safePageSize, total)
        val paged = items.drop((safePage - 1) * safePageSize).take(safePageSize)

        return PaginationResult(
            totalItems = total,
            totalPages = totalPages,
            safeCurrentPage = safePage,
            pageSize = safePageSize,
            fromIndex = fromIdx,
            toIndex = toIdx,
            pagedItems = paged
        )
    }

    private fun calculateSafePostgrestRange(page: Int, limit: Int): Pair<Int, Int> {
        val safeLimit = maxOf(1, limit)
        val safePage = maxOf(1, page)
        val start = (safePage - 1) * safeLimit
        val end = start + safeLimit - 1
        return Pair(start, end)
    }

    @Test
    fun testPaginationStandardFlow() {
        val dummyItems = (1..100).toList()

        // 100 eleman, her sayfada 15 kayıt -> toplam 7 sayfa (15*6 + 10 = 100)
        val page1 = calculateSafePagination(dummyItems, requestedPage = 1, requestedPageSize = 15)
        assertEquals(100, page1.totalItems)
        assertEquals(7, page1.totalPages)
        assertEquals(1, page1.safeCurrentPage)
        assertEquals(1, page1.fromIndex)
        assertEquals(15, page1.toIndex)
        assertEquals(15, page1.pagedItems.size)
        assertEquals(1, page1.pagedItems.first())
        assertEquals(15, page1.pagedItems.last())

        // Son sayfa (7. sayfa): 91'den 100'e kadar (10 eleman)
        val page7 = calculateSafePagination(dummyItems, requestedPage = 7, requestedPageSize = 15)
        assertEquals(7, page7.safeCurrentPage)
        assertEquals(91, page7.fromIndex)
        assertEquals(100, page7.toIndex)
        assertEquals(10, page7.pagedItems.size)
        assertEquals(91, page7.pagedItems.first())
        assertEquals(100, page7.pagedItems.last())
    }

    @Test
    fun testPaginationBoundaryAndNegativeInputs() {
        val items = (1..50).toList()

        // a) Negatif ve 0 sayfa boyutu (Zero division önleme)
        val zeroLimit = calculateSafePagination(items, requestedPage = 1, requestedPageSize = 0)
        assertEquals(15, zeroLimit.pageSize, "0 limit verildiğinde varsayılan 15'e coerce edilmelidir.")
        assertEquals(4, zeroLimit.totalPages)

        val negativeLimit = calculateSafePagination(items, requestedPage = 1, requestedPageSize = -10)
        assertEquals(15, negativeLimit.pageSize, "Negatif limit verildiğinde varsayılan 15'e coerce edilmelidir.")

        // b) Sınır aşımı (Sayfa taşması - Overflow)
        val overflowPage = calculateSafePagination(items, requestedPage = 999, requestedPageSize = 10)
        assertEquals(5, overflowPage.totalPages)
        assertEquals(5, overflowPage.safeCurrentPage, "Mevcut sayfa sayısından büyük talep son sayfaya kilitlenmelidir.")
        assertEquals(10, overflowPage.pagedItems.size)

        // c) Negatif veya 0 sayfa numarası (Underflow)
        val underflowPage = calculateSafePagination(items, requestedPage = -5, requestedPageSize = 10)
        assertEquals(1, underflowPage.safeCurrentPage, "0 veya negatif sayfa 1. sayfaya kilitlenmelidir.")
        assertEquals(1, underflowPage.fromIndex)

        // d) Boş liste sayfalama (Empty list)
        val emptyPagination = calculateSafePagination(emptyList<String>(), requestedPage = 1, requestedPageSize = 20)
        assertEquals(0, emptyPagination.totalItems)
        assertEquals(1, emptyPagination.totalPages)
        assertEquals(1, emptyPagination.safeCurrentPage)
        assertEquals(0, emptyPagination.fromIndex)
        assertEquals(0, emptyPagination.toIndex)
        assertTrue(emptyPagination.pagedItems.isEmpty())
    }

    @Test
    fun testPostgrestRangeCalculations() {
        // PostgREST 0-tabanlı aralık hesaplaması
        val (s1, e1) = calculateSafePostgrestRange(page = 1, limit = 20)
        assertEquals(0, s1)
        assertEquals(19, e1)

        val (s2, e2) = calculateSafePostgrestRange(page = 2, limit = 20)
        assertEquals(20, s2)
        assertEquals(39, e2)

        val (s5, e5) = calculateSafePostgrestRange(page = 5, limit = 50)
        assertEquals(200, s5)
        assertEquals(249, e5)

        // Negatif değer toleransı
        val (sn, en) = calculateSafePostgrestRange(page = -1, limit = -10)
        assertEquals(0, sn)
        assertEquals(0, en)
    }

    // =========================================================================
    // b) ÇOKLU VERİ FİLTRELEME VE YÜKSEK HACİMLİ VERİ RESILIENCE (10,000 ITEM)
    // =========================================================================

    @Test
    fun testHighVolume10000ProductsFilteringAndSorting() {
        val canonicalOperators = TourOperatorConfig.ALLOWED_OPERATOR_NAMES // 9 Kanonik Operatör
        val nonCanonicalOperators = listOf("Tez Tour", "Marmara Tur", "Lufthansa Holidays", "Global Travel Inc")
        val allOperators = canonicalOperators + nonCanonicalOperators

        val countries = listOf("TR", "EG", "TH", "AE", "RU", "MV")
        val productTypes = listOf("PACKAGE_TOUR", "FLIGHT", "HOTEL")

        // 10.000 adet gerçek UnifiedProductEntity nesnesi simüle et (bellekte sıfır mock)
        val highVolumeProducts = ArrayList<UnifiedProductEntity>(10000)
        for (i in 1..10000) {
            val op = allOperators[i % allOperators.size]
            val cCode = countries[i % countries.size]
            val pType = productTypes[i % productTypes.size]
            val basePrice = 10000.0 + (i % 500) * 100.0 // 10,000 - 60,000 RUB arası

            highVolumeProducts.add(
                UnifiedProductEntity(
                    id = "prod-$i",
                    tourName = "Tur Seçeneği #$i",
                    productType = pType,
                    operatorName = op,
                    countryCode = cCode,
                    country = if (cCode == "TR") "Турция" else "Mısır",
                    price = basePrice,
                    hotelName = if (pType == "FLIGHT") "" else "Otel $i",
                    hotelCategory = ((i / 3) % 3) + 3, // 3, 4 veya 5 yıldız (ürün tipi modülosundan bağımsız)
                    isInstantConfirmation = (i % 2 == 0),
                    departureDate = "2026-10-${(i % 28) + 1}".let { if (it.length == 9) it.replace("-", "-0") else it }
                )
            )
        }

        assertEquals(10000, highVolumeProducts.size)

        // 1. KESİN KURAL FİLTRESİ: Yalnızca 9 Kanonik Tur Operatörü kabul edilmeli
        val canonicalFiltered = highVolumeProducts.filter {
            TourOperatorConfig.isAllowedOperator(it.operatorName)
        }
        assertTrue(canonicalFiltered.isNotEmpty())
        assertTrue(canonicalFiltered.all { TourOperatorConfig.isAllowedOperator(it.operatorName) })
        assertFalse(canonicalFiltered.any { it.operatorName in nonCanonicalOperators }, "Yasaklı operatörler sızmamalıdır.")

        // 2. ÜRÜN TİPİ FİLTRESİ: Yalnızca Paket Turlar (PACKAGE_TOUR)
        val packageTours = canonicalFiltered.filter { it.productType == "PACKAGE_TOUR" }
        assertTrue(packageTours.all { it.productType == "PACKAGE_TOUR" })
        assertFalse(packageTours.any { it.productType == "FLIGHT" })

        // 3. ÇOKLU BİLEŞİK FİLTRE: Türkiye (TR) + 5 Yıldız + Anında Onay (Instant Confirmation)
        val compositeFiltered = packageTours.filter {
            it.countryCode == "TR" && it.hotelCategory == 5 && it.isInstantConfirmation
        }
        assertTrue(compositeFiltered.isNotEmpty())
        assertTrue(compositeFiltered.all { it.countryCode == "TR" })
        assertTrue(compositeFiltered.all { it.hotelCategory == 5 })
        assertTrue(compositeFiltered.all { it.isInstantConfirmation })

        // 4. SIRALAMA VE VERİ BÜTÜNLÜĞÜ: Fiyata göre artan (ASC) sıralama
        val sortedAsc = compositeFiltered.sortedBy { it.price }
        assertEquals(compositeFiltered.size, sortedAsc.size, "Sıralama esnasında hiçbir veri kaybolmamalıdır.")
        for (k in 0 until sortedAsc.size - 1) {
            assertTrue(
                sortedAsc[k].price <= sortedAsc[k + 1].price,
                "Fiyat artan düzende olmalıdır: ${sortedAsc[k].price} <= ${sortedAsc[k + 1].price}"
            )
        }

        // 5. YÜKSEK HACİMLİ SAYFALAMA TESTİ: Sayfaları baştan sona tüketerek eleman sayısını doğrula
        val pageSize = 25
        val paginationInfo = calculateSafePagination(sortedAsc, requestedPage = 1, requestedPageSize = pageSize)
        var totalConsumedItems = 0
        val seenIds = HashSet<String>()

        for (page in 1..paginationInfo.totalPages) {
            val pageResult = calculateSafePagination(sortedAsc, requestedPage = page, requestedPageSize = pageSize)
            totalConsumedItems += pageResult.pagedItems.size
            for (item in pageResult.pagedItems) {
                val isNew = seenIds.add(item.id)
                assertTrue(isNew, "Sayfalar arasında tekrarlanan (duplicate) kayıt olmamalıdır: ${item.id}")
            }
        }

        assertEquals(
            sortedAsc.size,
            totalConsumedItems,
            "Tüm sayfaların toplamı filtrelenmiş liste boyutuna birebir eşit olmalıdır."
        )
        assertEquals(sortedAsc.size, seenIds.size)
    }
}
