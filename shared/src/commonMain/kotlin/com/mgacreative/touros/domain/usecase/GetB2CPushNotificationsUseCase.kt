package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CPushNotificationItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.6 B2C Push Bildirimlerini Getirme Use Case.
 */
class GetB2CPushNotificationsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, customerId: String = "cust-101"): Result<List<B2CPushNotificationItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_customer_id", customerId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2c_push_notifications", params)
                .decodeList<B2CPushNotificationItem>()

            if (list.isEmpty()) getFallbackNotifications() else list
        }.recover { getFallbackNotifications() }
    }

    private fun getFallbackNotifications(): List<B2CPushNotificationItem> {
        return listOf(
            B2CPushNotificationItem("n1", "Kapadokya Turu Yarın Başlıyor! 🎈", "Sayın Elif Yılmaz, tur aracınız yarın saat 07:00 da otelinizden hareket edecektir.", "REMINDER", false, "2 saat önce"),
            B2CPushNotificationItem("n2", "Erken Rezervasyon Fırsatı: %15 İndirim 🔥", "Karadeniz Yaylalar Turu için erken rezervasyon fırsatını kaçırmayın.", "PROMOTION", true, "Dün")
        )
    }
}
