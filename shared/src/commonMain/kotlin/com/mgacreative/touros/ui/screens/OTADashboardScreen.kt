package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

private data class OTAProviderItemData(
    val id: String,
    val name: String,
    val logoIcon: String,
    val isConnected: Boolean,
    val lastSyncedAt: String,
    val productCount: Int,
    val bookingCount: Int,
    val hasError: Boolean = false
)

private val sampleOTAProviders = listOf(
    OTAProviderItemData("viator", "Viator / TripAdvisor", "🌐", true, "2 Dk Önce (18:20)", 14, 185, false),
    OTAProviderItemData("getyourguide", "GetYourGuide", "🎯", true, "5 Dk Önce (18:17)", 10, 142, false),
    OTAProviderItemData("booking", "Booking.com Experiences", "🏨", true, "12 Dk Önce (18:10)", 22, 210, false),
    OTAProviderItemData("expedia", "Expedia Local Expert", "✈️", false, "Bağlantı Kesildi (Hata)", 8, 45, true),
    OTAProviderItemData("airbnb", "Airbnb Experiences", "🏡", true, "1Sa Önce (17:22)", 6, 68, false),
    OTAProviderItemData("tiqets", "Tiqets Partner API", "🎟️", true, "30 Dk Önce (17:52)", 12, 94, false)
)

/**
 * OTA Dashboard Ekranı — TourOS 0.3
 *
 * Bağlı sağlayıcı kartları grid'i.
 * Her kartta sağlayıcı logosu/adı, bağlantı durumu rozeti (Success/Error), son sync zamanı ve 'Yönet' butonu.
 */
@Composable
fun OTADashboardScreen(
    viewModel: OTAHubViewModel,
    tenantId: String = "tenant-001",
    onNavigateToLogs: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var providersList by remember { mutableStateOf(sampleOTAProviders) }
    var selectedProviderForManage by remember { mutableStateOf<OTAProviderItemData?>(null) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "OTA Entegrasyon Dashboard",
                subtitle = "Canlı online seyahat kanalları ve API senkronizasyon kontrolü",
                actions = {
                    TourOSButton(
                        text = "🔄 Tümünü Senkronize Et",
                        onClick = { viewModel.loadBookings(tenantId = tenantId) },
                        variant = TourOSButtonVariant.SECONDARY,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val gridColumns = if (maxWidth >= 1024.dp) 3 else if (maxWidth >= 640.dp) 2 else 1

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ÖZET METRİK KARTLARI (Kanal Bağlantı İstatistikleri)
                OTAMetricsOverviewBar(
                    totalBookings = providersList.sumOf { it.bookingCount },
                    activeChannels = providersList.count { it.isConnected },
                    totalChannels = providersList.size,
                    errorChannels = providersList.count { it.hasError }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔗 Entegre OTA Sağlayıcı Kanalları (${providersList.size})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    TourOSStatusBadge(
                        text = "${providersList.count { it.isConnected }} / ${providersList.size} CANLI",
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success
                    )
                }

                // ── BAĞLI SAĞLAYICI KARTLARI GRID'İ ──────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(providersList) { provider ->
                        OTAProviderGridCardItem(
                            provider = provider,
                            onManage = { selectedProviderForManage = provider }
                        )
                    }
                }
            }

            // KANAL YÖNETİM MODAL DIALOGU
            selectedProviderForManage?.let { provider ->
                OTAManageModalDialog(
                    provider = provider,
                    onDismiss = { selectedProviderForManage = null },
                    onSyncNow = {
                        viewModel.syncNow(provider.id, true, tenantId)
                        selectedProviderForManage = null
                    }
                )
            }
        }
    }
}

// ─── ÖZET METRİK BARI BİLEŞENİ ────────────────────────────────────────────────

@Composable
private fun OTAMetricsOverviewBar(
    totalBookings: Int,
    activeChannels: Int,
    totalChannels: Int,
    errorChannels: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.PrimaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Toplam OTA Rezervasyonu", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("$totalBookings REZ", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        }

        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.SuccessContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Aktif Kanal Bağlantısı", style = TourOSTypography.Caption.copy(color = TourOSColors.Success))
                Text("$activeChannels / $totalChannels AKTİF", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Success))
            }
        }

        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = if (errorChannels > 0) TourOSColors.SecondaryContainer else TourOSColors.Surface,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Hatalı Kanal Durumu", style = TourOSTypography.Caption.copy(color = if (errorChannels > 0) TourOSColors.Secondary else TourOSColors.TextSecondary))
                Text("$errorChannels HATA", style = TourOSTypography.TitleLarge.copy(color = if (errorChannels > 0) TourOSColors.Secondary else TourOSColors.TextPrimary))
            }
        }
    }
}

// ─── BAĞLI SAĞLAYICI KARTI BİLEŞENİ (Strict Rule: Logo, Rozet, Sync, Yönet) ─────

@Composable
private fun OTAProviderGridCardItem(
    provider: OTAProviderItemData,
    onManage: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // 1. SAĞLAYICI LOGOSU & BAĞLANTI DURUMU ROZETİ (Success/Error) (Strict Rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(provider.logoIcon, style = TourOSTypography.TitleLarge)
                    Text(
                        provider.name,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }

                // BAĞLANTI DURUMU ROZETİ (Success/Error Strict Rule)
                TourOSStatusBadge(
                    text = if (provider.hasError) "❌ KESİNTİ HATA" else if (provider.isConnected) "✅ BAĞLANTI AKTİF" else "○ PASİF",
                    backgroundColor = if (provider.hasError) TourOSColors.SecondaryContainer else if (provider.isConnected) TourOSColors.SuccessContainer else TourOSColors.Surface,
                    textColor = if (provider.hasError) TourOSColors.Secondary else if (provider.isConnected) TourOSColors.Success else TourOSColors.TextSecondary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // 2. SON SYNC ZAMANI (Strict Rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Son Sync Zamanı:",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    provider.lastSyncedAt,
                    style = TourOSTypography.Label.copy(
                        color = if (provider.hasError) TourOSColors.Secondary else TourOSColors.Primary
                    )
                )
            }

            // 3. EŞLEŞEN ÜRÜN VE REZERVASYON DETAYLARI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Eşleşen Ürün: ${provider.productCount} Tur",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    "Rezervasyon: ${provider.bookingCount} Rez",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                )
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

            // 4. 'YÖNET' BUTONU (Strict Rule)
            TourOSButton(
                text = "⚙️ Yönet & Ayarlar",
                onClick = onManage,
                variant = TourOSButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── KANAL YÖNETİM MODAL DIALOGU ──────────────────────────────────────────────

@Composable
private fun OTAManageModalDialog(
    provider: OTAProviderItemData,
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(provider.logoIcon, style = TourOSTypography.TitleLarge)
                Text(
                    "${provider.name} Yönetimi",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Text(
                    "Kanal Durumu: ${if (provider.isConnected) "Bağlı ve Aktif" else "Bağlantı Kesildi"}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                )
                Text(
                    "Son Senkronizasyon: ${provider.lastSyncedAt}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                HorizontalDivider(color = TourOSColors.Divider)

                TourOSButton(
                    text = "⚡ Şimdi Manuel Senkronize Et",
                    onClick = onSyncNow,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TourOSButton(
                text = "Kapat",
                onClick = onDismiss,
                variant = TourOSButtonVariant.TERTIARY
            )
        },
        containerColor = TourOSColors.Surface,
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )
}
