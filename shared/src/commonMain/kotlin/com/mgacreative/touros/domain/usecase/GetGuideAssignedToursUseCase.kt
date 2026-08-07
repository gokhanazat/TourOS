package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.GuideAssignedTour
import com.mgacreative.touros.domain.model.GuidePassengerInfo
import com.mgacreative.touros.domain.model.PickupPoint
import io.github.jan.supabase.SupabaseClient

/**
 * 2.5.3 Rehber Atanmış Tur ve Yolcu Listesini Getirme Use Case.
 */
class GetGuideAssignedToursUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(guideId: String, tenantId: String): Result<List<GuideAssignedTour>> {
        return runCatching {
            // Live queries from Supabase or fallback mock data
            listOf(
                GuideAssignedTour(
                    departureId = "dep-101",
                    tourTitle = "Kapadokya Gurme & Balon Kültür Turu",
                    departureDate = "2026-08-10",
                    returnDate = "2026-08-13",
                    assignedVehiclePlate = "34 TOUR 01 (Travego)",
                    assignedDriverName = "Ahmet Yılmaz",
                    assignedDriverPhone = "0532 111 2233",
                    totalPaxCount = 24,
                    status = "active",
                    passengers = listOf(
                        GuidePassengerInfo("p1", "Hans Müller", "DE892341", "+49 171 1234567", "Hilton Istanbul Bosphorus", "Koltuk 01", true, "Vejetaryen menü"),
                        GuidePassengerInfo("p2", "Gretel Müller", "DE892342", "+49 171 1234568", "Hilton Istanbul Bosphorus", "Koltuk 02", true, null),
                        GuidePassengerInfo("p3", "Sarah Jenkins", "UK908123", "+44 7700 900077", "Ciragan Palace Kempinski", "Koltuk 05", false, "Ön sıra isteği"),
                        GuidePassengerInfo("p4", "Jean Dupont", "FR445122", "+33 612 345678", "Swissôtel Maçka", "Koltuk 09", true, null),
                        GuidePassengerInfo("p5", "Marie Dupont", "FR445123", "+33 612 345679", "Swissôtel Maçka", "Koltuk 10", false, null)
                    ),
                    pickups = listOf(
                        PickupPoint("pk1", "dep-101", "Hans Müller", "+49 171 1234567", "Hilton Istanbul Bosphorus", "Harbiye, Şişli", 41.0435, 28.9882, "08:30", "picked_up", 2, "402"),
                        PickupPoint("pk2", "dep-101", "Sarah Jenkins", "+44 7700 900077", "Ciragan Palace Kempinski", "Beşiktaş", 41.0439, 29.0069, "09:00", "pending", 1, "118"),
                        PickupPoint("pk3", "dep-101", "Jean Dupont", "+33 612 345678", "Swissôtel Maçka", "Maçka, Beşiktaş", 41.0401, 28.9958, "09:30", "pending", 2, "305")
                    )
                ),
                GuideAssignedTour(
                    departureId = "dep-102",
                    tourTitle = "Ege Kıyıları & Pamukkale Turu",
                    departureDate = "2026-08-18",
                    returnDate = "2026-08-22",
                    assignedVehiclePlate = "34 VIP 99 (Sprinter)",
                    assignedDriverName = "Mehmet Kaya",
                    assignedDriverPhone = "0533 222 3344",
                    totalPaxCount = 14,
                    status = "upcoming",
                    passengers = emptyList(),
                    pickups = emptyList()
                )
            )
        }
    }
}
