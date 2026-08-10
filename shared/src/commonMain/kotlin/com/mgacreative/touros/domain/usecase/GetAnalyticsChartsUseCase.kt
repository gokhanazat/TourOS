package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CountrySalesData
import com.mgacreative.touros.domain.model.DailySalesData
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient

data class ChannelSalesData(
    val channelName: String,
    val bookingCount: Int,
    val totalSales: Double,
    val percentage: Double
)

data class AnalyticsChartsResult(
    val dailySales: List<DailySalesData>,
    val countrySales: List<CountrySalesData>,
    val channelSales: List<ChannelSalesData>
)

/**
 * 📊 Analitik & Trend Grafikleri Use Case — Gerçek Veri Tabanlı Canlı Analiz
 */
class GetAnalyticsChartsUseCase(
    private val supabaseClient: SupabaseClient,
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(tenantId: String, days: Int = 7): Result<AnalyticsChartsResult> {
        return runCatching {
            val bookings = bookingRepository.getBookings(tenantId).getOrDefault(emptyList())

            if (bookings.isNotEmpty()) {
                // 1. Günlük Satış Analizi
                val dailyMap = bookings.groupBy { b ->
                    b.checkInDate ?: b.departureDate ?: b.createdAt?.take(10) ?: "Bugün"
                }

                val dailySalesList = dailyMap.entries.map { (dateStr, bList) ->
                    val totalSales = bList.sumOf { it.totalPrice }
                    val count = bList.size
                    DailySalesData(
                        saleDate = dateStr,
                        totalAmount = totalSales,
                        bookingCount = count
                    )
                }.sortedBy { it.saleDate }

                // 2. Ürün / Kategori Bazlı Satış Dağılımı
                val totalRevenue = bookings.sumOf { it.totalPrice }
                val countryMap = bookings.groupBy { b ->
                    if (b.bookingType == "HOTEL") "Otel Konaklama"
                    else b.productName?.takeIf { it.isNotBlank() } ?: "Tur Operasyonu"
                }

                val countrySalesList = countryMap.entries.map { (categoryName, bList) ->
                    val catRevenue = bList.sumOf { it.totalPrice }
                    val count = bList.size
                    val percentage = if (totalRevenue > 0) (catRevenue / totalRevenue) * 100 else 0.0
                    val code = if (categoryName.contains("Otel")) "HTL" else "TUR"

                    CountrySalesData(
                        countryCode = code,
                        countryName = categoryName,
                        totalAmount = catRevenue,
                        bookingCount = count,
                        percentage = percentage
                    )
                }.sortedByDescending { it.totalAmount }

                // 3. Kanal Bazlı Canlı Gerçek Satış Dağılımı (B2C, B2B, Mobil)
                val totalCount = bookings.size
                val b2bBookings = bookings.filter { !it.agencyId.isNullOrBlank() }
                val b2cBookings = bookings.filter { it.agencyId.isNullOrBlank() }

                val channelSalesList = listOf(
                    ChannelSalesData(
                        channelName = "B2C Doğrudan Web Satışı",
                        bookingCount = b2cBookings.size,
                        totalSales = b2cBookings.sumOf { it.totalPrice },
                        percentage = if (totalCount > 0) (b2cBookings.size.toDouble() / totalCount) * 100 else 0.0
                    ),
                    ChannelSalesData(
                        channelName = "Acente & B2B Kanalı",
                        bookingCount = b2bBookings.size,
                        totalSales = b2bBookings.sumOf { it.totalPrice },
                        percentage = if (totalCount > 0) (b2bBookings.size.toDouble() / totalCount) * 100 else 0.0
                    ),
                    ChannelSalesData(
                        channelName = "Mobil ve Çağrı Merkezi",
                        bookingCount = 0,
                        totalSales = 0.0,
                        percentage = 0.0
                    )
                )

                AnalyticsChartsResult(
                    dailySales = dailySalesList,
                    countrySales = countrySalesList,
                    channelSales = channelSalesList
                )
            } else {
                AnalyticsChartsResult(
                    dailySales = emptyList(),
                    countrySales = emptyList(),
                    channelSales = emptyList()
                )
            }
        }.recover {
            AnalyticsChartsResult(
                dailySales = emptyList(),
                countrySales = emptyList(),
                channelSales = emptyList()
            )
        }
    }
}
