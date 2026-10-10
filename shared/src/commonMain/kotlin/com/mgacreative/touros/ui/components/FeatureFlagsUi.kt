package com.mgacreative.touros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.data.featureflags.FeatureFlags
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Arama ekranlarındaki "Uçak Bileti / Uçuşlar" sekmesinin görünür olup olmadığı.
 * Ayar okunamazsa sekme GİZLİ kalır (güvenli varsayılan).
 */
@Composable
fun rememberFlightSearchTabEnabled(): Boolean {
    val client: SupabaseClient = koinInject()
    LaunchedEffect(Unit) { FeatureFlags.ensureLoaded(client) }
    val flags by FeatureFlags.flags.collectAsState()
    return FeatureFlags.isEnabled(FeatureFlags.FLIGHT_SEARCH_TAB, flags)
}

/**
 * CMS ekranı için aç/kapa kartı. Değişikliği sadece süper admin kaydedebilir (RPC kontrol eder).
 */
@Composable
fun FeatureFlagsAdminCard(modifier: Modifier = Modifier) {
    val client: SupabaseClient = koinInject()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { FeatureFlags.ensureLoaded(client, force = true) }
    val all by FeatureFlags.all.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    var busyKey by remember { mutableStateOf<String?>(null) }

    TourOSCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                text = AppLanguageManager.translate("Özellik Aç / Kapa"),
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AppLanguageManager.translate("Değişiklik anında kaydedilir; kullanıcılar sayfayı yenilediğinde görür."),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            if (all.isEmpty()) {
                Text(
                    text = AppLanguageManager.translate("Ayarlar yüklenemedi veya veritabanında henüz tanımlı değil."),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Warning)
                )
            }

            all.forEach { flag ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = TourOSSpacing.medium)) {
                        Text(
                            text = flagTitle(flag.flagKey),
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                        flag.description?.takeIf { it.isNotBlank() }?.let {
                            Text(text = it, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                    }
                    Switch(
                        checked = flag.isEnabled,
                        enabled = busyKey == null,
                        onCheckedChange = { checked ->
                            busyKey = flag.flagKey
                            scope.launch {
                                FeatureFlags.setEnabled(client, flag.flagKey, checked)
                                    .onSuccess { message = AppLanguageManager.translate("Kaydedildi.") }
                                    .onFailure { message = "${AppLanguageManager.translate("Kaydedilemedi")}: ${it.message.orEmpty()}" }
                                busyKey = null
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = TourOSColors.Success)
                    )
                }
            }

            message?.let {
                Text(text = it, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            }
        }
    }
}

private fun flagTitle(key: String): String = when (key) {
    FeatureFlags.FLIGHT_SEARCH_TAB -> AppLanguageManager.translate("Uçuş arama sekmesi (Web + Acente)")
    FeatureFlags.TOUR_ACTUALIZATION -> AppLanguageManager.translate("Rezervasyonda canlı tur detayı (Tourvisor)")
    else -> key
}
