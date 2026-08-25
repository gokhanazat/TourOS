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
    val channelSales: List<ChannelSalesData>,
    val totalRevenue: Double = 0.0,
    val totalBookingsCount: Int = 0,
    val totalPaxOrNights: Int = 0,
    val averageBookingValue: Double = 0.0
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
                val totalRevenue = bookings.sumOf { it.totalPrice }
                val totalPax = bookings.sumOf { if (it.nights > 0) it.nights else it.paxCount }
                val avgValue = if (bookings.isNotEmpty()) totalRevenue / bookings.size else 0.0

                // 1. Günlük Satış Analizi
                val dailyMap = bookings.groupBy { b ->
                    b.checkInDate ?: b.departureDate ?: b.createdAt.take(10).ifBlank { "Bugün" }
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

                // 2. 5 Temel Operasyonel Kategori Dağılımı
                val toBookings = bookings.filter {
                    val isExternalOp = !it.operatorName.isNullOrBlank() && !it.operatorName.contains("MGA", ignoreCase = true)
                    val hasOpPnr = !it.operatorPnrCode.isNullOrBlank()
                    (isExternalOp || hasOpPnr) && it.bookingType != "HOTEL"
                }

                val hotelBookings = bookings.filter {
                    it.bookingType == "HOTEL" || !it.hotelId.isNullOrBlank()
                }

                val localTourBookings = bookings.filter {
                    val isOwn = it.operatorName.isNullOrBlank() || it.operatorName.contains("MGA", ignoreCase = true)
                    val noOpPnr = it.operatorPnrCode.isNullOrBlank()
                    it.bookingType == "TOUR" && isOwn && noOpPnr
                }

                val flightBookings = bookings.filter {
                    it.bookingType.equals("FLIGHT", ignoreCase = true)
                }

                val transferBookings = bookings.filter {
                    it.bookingType.equals("TRANSFER", ignoreCase = true) || it.bookingType.equals("EXTRA", ignoreCase = true)
                }

                fun buildCategoryData(code: String, name: String, list: List<com.mgacreative.touros.domain.model.Booking>): CountrySalesData {
                    val rev = list.sumOf { it.totalPrice }
                    val count = list.size
                    val pct = if (totalRevenue > 0) (rev / totalRevenue) * 100 else 0.0
                    return CountrySalesData(
                        countryCode = code,
                        countryName = name,
                        totalAmount = rev,
                        bookingCount = count,
                        percentage = pct
                    )
                }

                val categorySalesList = listOf(
                    buildCategoryData("HTL", "Otel & Konaklama", hotelBookings),
                    buildCategoryData("TO_PKG", "TO Tur Paketleri", toBookings),
                    buildCategoryData("LOCAL_TOUR", "Yerel Tur Paketlerimiz", localTourBookings),
                    buildCategoryData("FLIGHT", "Uçak & Uçuş Operasyonları", flightBookings),
                    buildCategoryData("TRANSFER", "Transfer & Ek Hizmetler", transferBookings)
                ).sortedByDescending { it.totalAmount }

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
                        channelName = "Ofis & Çağrı Merkezi",
                        bookingCount = 0,
                        totalSales = 0.0,
                        percentage = 0.0
                    )
                )

                AnalyticsChartsResult(
                    dailySales = dailySalesList,
                    countrySales = categorySalesList,
                    channelSales = channelSalesList,
                    totalRevenue = totalRevenue,
                    totalBookingsCount = bookings.size,
                    totalPaxOrNights = totalPax,
                    averageBookingValue = avgValue
                )
            } else {
                val emptyCategories = listOf(
                    CountrySalesData("HTL", "Otel & Konaklama", 0.0, 0, 0.0),
                    CountrySalesData("TO_PKG", "TO Tur Paketleri", 0.0, 0, 0.0),
                    CountrySalesData("LOCAL_TOUR", "Yerel Tur Paketlerimiz", 0.0, 0, 0.0),
                    CountrySalesData("FLIGHT", "Uçak & Uçuş Operasyonları", 0.0, 0, 0.0),
                    CountrySalesData("TRANSFER", "Transfer & Ek Hizmetler", 0.0, 0, 0.0)
                )
                val emptyChannels = listOf(
                    ChannelSalesData("B2C Doğrudan Web Satışı", 0, 0.0, 0.0),
                    ChannelSalesData("Acente & B2B Kanalı", 0, 0.0, 0.0),
                    ChannelSalesData("Ofis & Çağrı Merkezi", 0, 0.0, 0.0)
                )
                AnalyticsChartsResult(
                    dailySales = emptyList(),
                    countrySales = emptyCategories,
                    channelSales = emptyChannels,
                    totalRevenue = 0.0,
                    totalBookingsCount = 0,
                    totalPaxOrNights = 0,
                    averageBookingValue = 0.0
                )
            }
        }.recover {
            val emptyCategories = listOf(
                CountrySalesData("HTL", "Otel & Konaklama", 0.0, 0, 0.0),
                CountrySalesData("TO_PKG", "TO Tur Paketleri", 0.0, 0, 0.0),
                CountrySalesData("LOCAL_TOUR", "Yerel Tur Paketlerimiz", 0.0, 0, 0.0),
                CountrySalesData("FLIGHT", "Uçak & Uçuş Operasyonları", 0.0, 0, 0.0),
                CountrySalesData("TRANSFER", "Transfer & Ek Hizmetler", 0.0, 0, 0.0)
            )
            val emptyChannels = listOf(
                ChannelSalesData("B2C Doğrudan Web Satışı", 0, 0.0, 0.0),
                ChannelSalesData("Acente & B2B Kanalı", 0, 0.0, 0.0),
                ChannelSalesData("Ofis & Çağrı Merkezi", 0, 0.0, 0.0)
            )
            AnalyticsChartsResult(
                dailySales = emptyList(),
                countrySales = emptyCategories,
                channelSales = emptyChannels,
                totalRevenue = 0.0,
                totalBookingsCount = 0,
                totalPaxOrNights = 0,
                averageBookingValue = 0.0
            )
        }
    }
}
