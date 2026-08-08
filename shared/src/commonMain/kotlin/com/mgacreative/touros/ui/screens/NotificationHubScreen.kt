package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.NotificationChannel
import com.mgacreative.touros.domain.model.NotificationResult
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.NotificationHubViewModel

private data class ChannelFilterOption(val key: String, val label: String, val icon: String)

private val channelFilters = listOf(
    ChannelFilterOption("ALL", "Tüm Bildirimler", "🔔"),
    ChannelFilterOption("PUSH", "Push (Sistem)", "📲"),
    ChannelFilterOption("WHATSAPP", "WhatsApp", "💬"),
    ChannelFilterOption("SMS", "SMS", "📱"),
    ChannelFilterOption("EMAIL", "E-Posta", "📧")
)

private data class NotificationItemUi(
    val id: String,
    val channel: NotificationChannel,
    val title: String,
    val content: String,
    val recipient: String,
    val timestamp: String,
    val isRead: Boolean
)

/**
 * Bildirim Merkezi — TourOS 0.3
 *
 * Kronolojik bildirim listesi.
 * Okunmamışlar hafif Primary Container arka planla vurgulanır.
 * Üstte kanal filtre chip'leri (Push, WhatsApp, SMS, E-Posta).
 */
@Composable
fun NotificationHubScreen(
    viewModel: NotificationHubViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedChannelFilter by remember { mutableStateOf("ALL") }

    // Kronolojik Bildirim Veri Mock / Listesi
    var notificationList by remember {
        mutableStateOf(
            listOf(
                NotificationItemUi("n1", NotificationChannel.PUSH, "Yeni Rezervasyon Alındı (#BK-9812)", "Hans Müller Kapadokya VIP Turu için 12,000 TRY ödeme yaptı.", "Operasyon Ekibi", "5 Dk Önce", isRead = false),
                NotificationItemUi("n2", NotificationChannel.WHATSAPP, "Voucher Bağlantısı Gönderildi", "Sayın Sarah Jenkins, seyahat voucher belgeniz WhatsApp üzerinden iletildi.", "+44 7700 900077", "18 Dk Önce", isRead = false),
                NotificationItemUi("n3", NotificationChannel.SMS, "Transfer Aracı Yola Çıktı", "34 TUR 06 plakalı Mercedes Sprinter havalimanına hareket etti.", "+90 532 111 2233", "45 Dk Önce", isRead = false),
                NotificationItemUi("n4", NotificationChannel.EMAIL, "Otomatik Satış Faturası Kesildi", "INV-202608-002 numaralı e-fatura müşteriye e-posta ile iletildi.", "sarah@jenkins.com", "2 Saat Önce", isRead = true),
                NotificationItemUi("n5", NotificationChannel.PUSH, "Rehber Görev Onayı", "Rehber Mehmet Can 'Ege Turu' görevini onayladı ve takvime ekledi.", "Rehber Ekibi", "4 Saat Önce", isRead = true)
            )
        )
    }

    val filteredNotifications = remember(notificationList, selectedChannelFilter) {
        if (selectedChannelFilter == "ALL") notificationList
        else notificationList.filter { it.channel.name == selectedChannelFilter }
    }

    val unreadCount = remember(notificationList) { notificationList.count { !it.isRead } }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Bildirim Merkezi",
                subtitle = "Kronolojik bildirim günlüğü ve kanal takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TourOSButton(
                            text = "Tümünü Okundu İşaretle",
                            onClick = {
                                notificationList = notificationList.map { it.copy(isRead = true) }
                            },
                            variant = TourOSButtonVariant.TERTIARY,
                            modifier = Modifier.padding(end = TourOSSpacing.small)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── 1. ÜSTTE KANAL FİLTRE CHIP'LERİ ──────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📡 İletişim Kanalları",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    if (unreadCount > 0) {
                        TourOSStatusBadge(
                            text = "$unreadCount Okunmamış",
                            backgroundColor = TourOSColors.PrimaryContainer,
                            textColor = TourOSColors.Primary
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(channelFilters) { opt ->
                        FilterChip(
                            selected = selectedChannelFilter == opt.key,
                            onClick = { selectedChannelFilter = opt.key },
                            label = { Text("${opt.icon} ${opt.label}", style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.Primary,
                                selectedLabelColor = TourOSColors.OnPrimary
                            )
                        )
                    }
                }
            }

            // ── 2. KRONOLOJİK BİLDİRİM LİSTESİ ───────────────────────────────
            item {
                Text(
                    "📜 Kronolojik Bildirim Akışı (${filteredNotifications.size})",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                )
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Bu kanala ait bildirim bulunamadı.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            } else {
                items(filteredNotifications) { item ->
                    ChronologicalNotificationCard(
                        item = item,
                        onMarkAsRead = {
                            notificationList = notificationList.map {
                                if (it.id == item.id) it.copy(isRead = true) else it
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─── KRONOLOJİK BİLDİRİM KARTI (OKUNMAMIŞLAR PRIMARY CONTAINER İLE VURGULANIR) ──

@Composable
private fun ChronologicalNotificationCard(
    item: NotificationItemUi,
    onMarkAsRead: () -> Unit
) {
    // OKUNMAMIŞLAR HAFİF PRIMARY CONTAINER ARKA PLANLA VURGULANIR (Strict Rule)
    val cardBg = if (!item.isRead) {
        TourOSColors.PrimaryContainer.copy(alpha = 0.45f)
    } else {
        TourOSColors.Surface
    }

    val (channelIcon, channelBadgeText) = when (item.channel) {
        NotificationChannel.PUSH -> "📲" to "PUSH"
        NotificationChannel.WHATSAPP -> "💬" to "WHATSAPP"
        NotificationChannel.SMS -> "📱" to "SMS"
        NotificationChannel.EMAIL -> "📧" to "E-POSTA"
    }

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.isRead) onMarkAsRead() },
        backgroundColor = cardBg,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(channelIcon, style = TourOSTypography.TitleLarge)
                    Text(
                        item.title,
                        style = TourOSTypography.Label.copy(
                            color = TourOSColors.TextPrimary
                        )
                    )
                }

                // Okunmadı rozeti veya Kanal badgesi
                if (!item.isRead) {
                    TourOSStatusBadge(
                        text = "🔵 YENİ",
                        backgroundColor = TourOSColors.Primary,
                        textColor = TourOSColors.OnPrimary
                    )
                } else {
                    TourOSStatusBadge(
                        text = channelBadgeText,
                        backgroundColor = TourOSColors.SecondaryContainer,
                        textColor = TourOSColors.Secondary
                    )
                }
            }

            Text(
                item.content,
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
            )

            HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👤 ${item.recipient}  ·  🕒 ${item.timestamp}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                if (!item.isRead) {
                    TourOSButton(
                        text = "✓ Okundu",
                        onClick = onMarkAsRead,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            }
        }
    }
}
