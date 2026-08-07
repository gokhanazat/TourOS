package com.mgacreative.touros.domain.model

data class MonthlyTrendItem(val monthName: String, val sales: Double)
data class CountrySalesItem(val countryName: String, val revenue: Double, val percentage: Float)
data class TourRevenueItem(val tourTitle: String, val revenue: Double)
data class ChannelSalesItem(val channelName: String, val amount: Double, val percentage: Float)

data class DashboardAnalyticsCharts(
    val monthlyTrends: List<MonthlyTrendItem> = listOf(
        MonthlyTrendItem("Oca", 120000.0),
        MonthlyTrendItem("Şub", 145000.0),
        MonthlyTrendItem("Mar", 190000.0),
        MonthlyTrendItem("Nis", 240000.0),
        MonthlyTrendItem("May", 310000.0),
        MonthlyTrendItem("Haz", 420000.0)
    ),
    val countrySales: List<CountrySalesItem> = listOf(
        CountrySalesItem("Türkiye", 250000.0, 50f),
        CountrySalesItem("İtalya", 125000.0, 25f),
        CountrySalesItem("İspanya", 75000.0, 15f),
        CountrySalesItem("Mısır", 50000.0, 10f)
    ),
    val tourRevenues: List<TourRevenueItem> = listOf(
        TourRevenueItem("Kapadokya Balon Turu", 210000.0),
        TourRevenueItem("Karadeniz Yaylalar Turu", 160000.0),
        TourRevenueItem("Ege Kıyıları & Pamukkale", 130000.0),
        TourRevenueItem("Büyük İtalya Turu", 95000.0)
    ),
    val channelSales: List<ChannelSalesItem> = listOf(
        ChannelSalesItem("B2B Acente", 300000.0, 60f),
        ChannelSalesItem("B2C Web Mobil", 125000.0, 25f),
        ChannelSalesItem("Doğrudan Ofis", 75000.0, 15f)
    )
)
