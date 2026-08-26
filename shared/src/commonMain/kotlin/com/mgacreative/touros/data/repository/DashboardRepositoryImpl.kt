package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.data.database.entity.DashboardSummaryEntity
import com.mgacreative.touros.data.database.entity.DepartureEntity
import com.mgacreative.touros.data.database.entity.DriverEntity
import com.mgacreative.touros.data.database.entity.GuideEntity
import com.mgacreative.touros.data.database.entity.TourEntity
import com.mgacreative.touros.data.database.entity.TransferEntity
import com.mgacreative.touros.data.database.entity.VehicleEntity
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.domain.model.ChannelSalesItem
import com.mgacreative.touros.domain.model.CountrySalesItem
import com.mgacreative.touros.domain.model.DashboardAnalyticsCharts
import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.GuideStatusInfo
import com.mgacreative.touros.domain.model.MonthlyTrendItem
import com.mgacreative.touros.domain.model.TourRevenueItem
import com.mgacreative.touros.domain.model.UpcomingTour
import com.mgacreative.touros.domain.model.VehicleOccupancy
import com.mgacreative.touros.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DashboardRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : DashboardRepository {

    override suspend fun getDashboardSummary(tenantId: String): Result<DashboardSummary> {
        return runCatching {
            val rpcParams = buildJsonObject {
                if (tenantId != "ALL" && tenantId.isValidUuid()) {
                    put("p_tenant_id", tenantId)
                }
            }
            val rpcResult = runCatching {
                supabaseClient.postgrest.rpc(
                    function = "get_dashboard_summary_v2",
                    parameters = rpcParams
                ).decodeSingle<DashboardSummaryEntity>()
            }.getOrNull()

            if (rpcResult != null) {
                DashboardSummary(
                    dailySales = rpcResult.dailySales,
                    monthlySales = rpcResult.monthlySales,
                    occupancyRate = rpcResult.occupancyRate,
                    cancellationCount = rpcResult.cancellationCount,
                    pendingPaymentsAmount = rpcResult.pendingPaymentsAmount
                )
            } else {
                if (tenantId != "ALL" && !tenantId.isValidUuid()) {
                    return@runCatching DashboardSummary(0.0, 0.0, 0.0, 0, 0.0)
                }
                val bookings = runCatching {
                    supabaseClient.postgrest.from("bookings")
                        .select {
                            filter {
                                if (tenantId.isValidUuid()) {
                                    eq("tenant_id", tenantId)
                                }
                            }
                        }
                        .decodeList<BookingEntity>()
                }.getOrDefault(emptyList())

                val departures = runCatching {
                    supabaseClient.postgrest.from("departures")
                        .select {
                            filter {
                                if (tenantId.isValidUuid()) {
                                    eq("tenant_id", tenantId)
                                }
                            }
                        }
                        .decodeList<DepartureEntity>()
                }.getOrDefault(emptyList())

                val activeBookings = bookings.filter { it.safeStatus != "İptal" && it.safeStatus != "Cancelled" }
                val cancelBookings = bookings.filter { it.safeStatus == "İptal" || it.safeStatus == "Cancelled" }
                val pendingBookings = bookings.filter { it.safeStatus == "Bekliyor" || it.safeStatus == "Opsiyon" || it.safeStatus == "Pending" }

                val monthlyTotal = activeBookings.sumOf { it.safeTotalPrice }
                val pendingTotal = pendingBookings.sumOf { it.safeTotalPrice }

                val totalCap = departures.sumOf { it.capacity ?: 0 }
                val totalBooked = departures.sumOf { it.bookedCount }
                val calculatedOccupancy = if (totalCap > 0) (totalBooked.toDouble() * 100.0 / totalCap.toDouble()) else 0.0

                DashboardSummary(
                    dailySales = if (monthlyTotal > 0) monthlyTotal * 0.1 else 0.0,
                    monthlySales = monthlyTotal,
                    occupancyRate = (calculatedOccupancy * 10).toInt() / 10.0,
                    cancellationCount = cancelBookings.size,
                    pendingPaymentsAmount = pendingTotal
                )
            }
        }
    }

    override suspend fun getUpcomingTours(tenantId: String): Result<List<UpcomingTour>> {
        return runCatching {
            val tours = runCatching {
                supabaseClient.postgrest.from("tours")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<TourEntity>()
            }.getOrDefault(emptyList())

            val departures = runCatching {
                supabaseClient.postgrest.from("departures")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<DepartureEntity>()
            }.getOrDefault(emptyList())

            if (departures.isNotEmpty()) {
                departures.take(5).map { dep ->
                    val matchedTour = tours.find { it.id == dep.tourId }
                    UpcomingTour(
                        id = dep.id,
                        tourTitle = matchedTour?.title ?: "Tur Paket #${dep.tourId.take(4)}",
                        departureDate = dep.departureDate,
                        bookedCount = dep.bookedCount,
                        capacity = dep.capacity ?: matchedTour?.capacity ?: 30,
                        status = dep.status
                    )
                }
            } else if (tours.isNotEmpty()) {
                tours.take(5).mapIndexed { idx, tour ->
                    UpcomingTour(
                        id = tour.id ?: "$idx",
                        tourTitle = tour.title,
                        departureDate = "Yakında",
                        bookedCount = 0,
                        capacity = tour.capacity,
                        status = if (tour.isActive) "Aktif" else "Planlandı"
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getVehicleOccupancies(tenantId: String): Result<List<VehicleOccupancy>> {
        return runCatching {
            val vehicles = runCatching {
                supabaseClient.postgrest.from("vehicles")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<VehicleEntity>()
            }.getOrDefault(emptyList())

            val transfers = runCatching {
                supabaseClient.postgrest.from("transfers")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<TransferEntity>()
            }.getOrDefault(emptyList())

            val drivers = runCatching {
                supabaseClient.postgrest.from("drivers")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<DriverEntity>()
            }.getOrDefault(emptyList())

            if (vehicles.isNotEmpty()) {
                vehicles.map { v ->
                    val vehicleTransfers = transfers.filter { it.vehicleId == v.id }
                    val totalOccupied = vehicleTransfers.sumOf { it.paxCount }
                    val activeTransfer = vehicleTransfers.firstOrNull()
                    val driver = drivers.find { it.id == activeTransfer?.driverId }

                    val safeCapacity = if (v.capacity > 0) v.capacity else 20
                    VehicleOccupancy(
                        id = v.id,
                        plateNumber = v.plateNumber,
                        modelName = "${v.brand ?: ""} ${v.model ?: ""}".trim().ifEmpty { v.vehicleType.uppercase() },
                        driverName = driver?.fullName ?: "Atanmadı",
                        occupiedSeats = totalOccupied.coerceAtMost(safeCapacity),
                        totalCapacity = safeCapacity,
                        assignedTourTitle = activeTransfer?.destination ?: "Transfer Görevi"
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getGuideStatuses(tenantId: String): Result<List<GuideStatusInfo>> {
        return runCatching {
            val guides = runCatching {
                supabaseClient.postgrest.from("guides")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<GuideEntity>()
            }.getOrDefault(emptyList())

            if (guides.isNotEmpty()) {
                guides.map { g ->
                    GuideStatusInfo(
                        id = g.id,
                        fullName = g.fullName,
                        phone = g.phone ?: "-",
                        languages = g.languages ?: listOf("Türkçe"),
                        status = if (g.isActive) "Müsait" else "Görevde",
                        assignedTourTitle = null
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getAnalyticsCharts(tenantId: String): Result<DashboardAnalyticsCharts> {
        return runCatching {
            val bookings = runCatching {
                supabaseClient.postgrest.from("bookings")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<BookingEntity>()
            }.getOrDefault(emptyList())

            val tours = runCatching {
                supabaseClient.postgrest.from("tours")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<TourEntity>()
            }.getOrDefault(emptyList())

            if (bookings.isNotEmpty()) {
                val totalRevenue = bookings.sumOf { it.safeTotalPrice }.coerceAtLeast(1.0)

                val countryGroup = tours.groupBy { it.country.ifBlank { "Türkiye" } }
                val countryItems = countryGroup.map { (country, tourList) ->
                    val rev = tourList.sumOf { t -> bookings.filter { b -> b.productName == t.title }.sumOf { it.safeTotalPrice } }
                    val pct = (rev * 100 / totalRevenue).toFloat()
                    CountrySalesItem(countryName = country, revenue = rev, percentage = pct)
                }.sortedByDescending { it.revenue }

                val tourItems = tours.map { tour ->
                    val rev = bookings.filter { it.productName == tour.title }.sumOf { it.safeTotalPrice }
                    TourRevenueItem(tourTitle = tour.title, revenue = rev)
                }.sortedByDescending { it.revenue }.take(4)

                val channelGroup = bookings.groupBy { it.operatorName ?: "B2C Web Mobil" }
                val channelItems = channelGroup.map { (channel, bList) ->
                    val amount = bList.sumOf { it.safeTotalPrice }
                    val pct = (amount * 100 / totalRevenue).toFloat()
                    ChannelSalesItem(channelName = channel, amount = amount, percentage = pct)
                }

                DashboardAnalyticsCharts(
                    monthlyTrends = listOf(
                        MonthlyTrendItem("Son Dönem", totalRevenue)
                    ),
                    countrySales = countryItems.ifEmpty {
                        listOf(CountrySalesItem("Türkiye", totalRevenue, 100f))
                    },
                    tourRevenues = tourItems.ifEmpty {
                        tours.take(4).map { TourRevenueItem(it.title, it.basePrice) }
                    },
                    channelSales = channelItems.ifEmpty {
                        listOf(ChannelSalesItem("Doğrudan Web", totalRevenue, 100f))
                    }
                )
            } else {
                DashboardAnalyticsCharts(
                    monthlyTrends = emptyList(),
                    countrySales = emptyList(),
                    tourRevenues = emptyList(),
                    channelSales = emptyList()
                )
            }
        }
    }
}
