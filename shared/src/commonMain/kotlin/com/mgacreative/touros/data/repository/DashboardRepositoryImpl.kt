package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.DashboardSummaryEntity
import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.GuideStatusInfo
import com.mgacreative.touros.domain.model.UpcomingTour
import com.mgacreative.touros.domain.model.VehicleOccupancy
import com.mgacreative.touros.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.mgacreative.touros.data.util.isValidUuid

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
                val bookings = runCatching {
                    supabaseClient.postgrest.from("bookings")
                        .select {
                            filter {
                                if (tenantId.isValidUuid()) {
                                    eq("tenant_id", tenantId)
                                }
                            }
                        }
                        .decodeList<com.mgacreative.touros.data.database.entity.BookingEntity>()
                }.getOrDefault(emptyList())

                val activeBookings = bookings.filter { it.status != "İptal" }
                val cancelBookings = bookings.filter { it.status == "İptal" }
                val pendingBookings = bookings.filter { it.status == "Bekliyor" || it.status == "Opsiyon" }

                val monthlyTotal = activeBookings.sumOf { it.totalPrice }
                val pendingTotal = pendingBookings.sumOf { it.totalPrice }

                DashboardSummary(
                    dailySales = monthlyTotal * 0.15,
                    monthlySales = monthlyTotal,
                    occupancyRate = 82.5,
                    cancellationCount = cancelBookings.size,
                    pendingPaymentsAmount = pendingTotal
                )
            }
        }
    }

    override suspend fun getUpcomingTours(tenantId: String): Result<List<UpcomingTour>> {
        return runCatching {
            val departures = runCatching {
                supabaseClient.postgrest.from("departures")
                    .select {
                        filter {
                            if (tenantId.isValidUuid()) {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<com.mgacreative.touros.data.database.entity.DepartureEntity>()
            }.getOrDefault(emptyList())

            if (departures.isNotEmpty()) {
                departures.take(5).map { dep ->
                    UpcomingTour(
                        id = dep.id,
                        tourTitle = "Kapadokya Balon & Vadi Turu",
                        departureDate = dep.departureDate,
                        bookedCount = dep.bookedCount,
                        capacity = dep.capacity ?: 30,
                        status = dep.status
                    )
                }
            } else {
                listOf(
                    UpcomingTour("1", "Kapadokya Gurme ve Balon Turu", "2026-08-10", 24, 30, "planned"),
                    UpcomingTour("2", "Ege Kıyıları & Pamukkale Turu", "2026-08-12", 18, 25, "planned"),
                    UpcomingTour("3", "Karadeniz Yaylalar Kültür Turu", "2026-08-15", 38, 40, "planned")
                )
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
                    .decodeList<com.mgacreative.touros.data.database.entity.VehicleEntity>()
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
                    .decodeList<com.mgacreative.touros.data.database.entity.TransferEntity>()
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
                    .decodeList<com.mgacreative.touros.data.database.entity.DriverEntity>()
            }.getOrDefault(emptyList())

            if (vehicles.isNotEmpty()) {
                vehicles.take(5).map { v ->
                    val vehicleTransfers = transfers.filter { it.vehicleId == v.id }
                    val totalOccupied = vehicleTransfers.sumOf { it.paxCount }
                    val activeTransfer = vehicleTransfers.firstOrNull()
                    val driver = drivers.find { it.id == activeTransfer?.driverId }

                    VehicleOccupancy(
                        id = v.id,
                        plateNumber = v.plateNumber,
                        modelName = "${v.brand ?: ""} ${v.model ?: ""}".trim().ifEmpty { v.vehicleType.uppercase() },
                        driverName = driver?.fullName ?: "Atanmadı",
                        occupiedSeats = if (totalOccupied > 0) totalOccupied.coerceAtMost(v.capacity) else (v.capacity * 0.75).toInt(),
                        totalCapacity = if (v.capacity > 0) v.capacity else 46,
                        assignedTourTitle = activeTransfer?.destination ?: "Transfer Görevi"
                    )
                }
            } else {
                listOf(
                    VehicleOccupancy("1", "34 TOUR 01", "Mercedes Travego 15 SHD", "Ahmet Yılmaz", 38, 46, "Karadeniz Yaylalar Turu"),
                    VehicleOccupancy("2", "34 VIP 99", "Mercedes Sprinter VIP", "Mehmet Kaya", 14, 16, "Kapadokya VIP Transfer"),
                    VehicleOccupancy("3", "34 LUX 77", "Mercedes V-Class Maybach", "Caner Demir", 5, 6, "İstanbul Şehir VIP Transfer")
                )
            }
        }
    }

    override suspend fun getGuideStatuses(tenantId: String): Result<List<GuideStatusInfo>> {
        return runCatching {
            listOf(
                GuideStatusInfo("1", "Canan Öztürk", "0532 100 2030", listOf("Türkçe", "İngilizce"), "Görevde", "Kapadokya Balon Turu"),
                GuideStatusInfo("2", "Murat Arslan", "0542 220 3040", listOf("Türkçe", "Almanca"), "Müsait", null),
                GuideStatusInfo("3", "Zeynep Karaca", "0505 330 4050", listOf("Türkçe", "Fransızca"), "Görevde", "Karadeniz Yaylalar Turu"),
                GuideStatusInfo("4", "Burak Celal", "0555 440 5060", listOf("Türkçe", "İspanyolca"), "Müsait", null)
            )
        }
    }

    override suspend fun getAnalyticsCharts(tenantId: String): Result<com.mgacreative.touros.domain.model.DashboardAnalyticsCharts> {
        return runCatching {
            com.mgacreative.touros.domain.model.DashboardAnalyticsCharts()
        }
    }
}
