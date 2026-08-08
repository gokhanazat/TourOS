package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.B2CPushNotificationItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CNotificationsReviewViewModel

/**
 * B2C Müşteri Bildirimleri & Tur Değerlendirme Ekranı — TourOS 0.3
 *
 * Basit kronolojik bildirim listesi.
 * Okunmamış bildirimler hafif Primary Container arka planla vurgulanır.
 */
@Composable
fun B2CNotificationsReviewScreen(
    viewModel: B2CNotificationsReviewViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var commentInput by remember { mutableStateOf("Rehberimiz Mehmet Bey harikaydı, balon turu organizasyonu kusursuzdu! Teşekkürler.") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Bildirimler & Duyurular",
                subtitle = "Seyahat hatırlatmaları, duyurular ve tur puanlama",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── 1. İKİ SEKMELİ DÜZEN (BİLDİRİMLER & DEĞERLENDİRME) ───────────────
            SecondaryTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = TourOSColors.Surface,
                contentColor = TourOSColors.Primary
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) }
                ) {
                    Text(
                        "🔔 Bildirimlerim (${state.unreadCount} Okunmamış)",
                        modifier = Modifier.padding(TourOSSpacing.medium),
                        style = TourOSTypography.Label.copy(
                            color = if (state.selectedTab == 0) TourOSColors.Primary else TourOSColors.TextSecondary
                        )
                    )
                }

                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) }
                ) {
                    Text(
                        "⭐ Tur Değerlendir",
                        modifier = Modifier.padding(TourOSSpacing.medium),
                        style = TourOSTypography.Label.copy(
                            color = if (state.selectedTab == 1) TourOSColors.Primary else TourOSColors.TextSecondary
                        )
                    )
                }
            }

            // Bildirim Mesajı
            if (state.notificationMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SuccessContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        state.notificationMessage!!,
                        style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                    )
                }
            }

            // ── 2. SEKMELERE GÖRE İÇERİK ───────────────────────────────────────
            if (state.selectedTab == 0) {
                // TAB 0: BASİT KRONOLOJİK BİLDİRİM LİSTESİ
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else if (state.notifications.isEmpty()) {
                    TourOSEmptyState(
                        title = "Henüz Bildiriminiz Bulunmuyor",
                        description = "Tur hatırlatmaları ve duyurular zamanı geldiğinde burada listelenecektir.",
                        icon = { Text("🔔", style = TourOSTypography.DisplaySmall) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.notifications) { notif ->
                            B2CPushNotificationCard(notif = notif)
                        }
                    }
                }
            } else {
                // TAB 1: TUR DEĞERLENDİRME FORMU
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.Surface,
                        contentPadding = TourOSSpacing.large
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            Text(
                                "🎉 Tamamlanan Turunuzu Değerlendirin",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.PrimaryContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    "Tur: ${state.selectedTourTitle}",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                                )
                            }

                            // Yıldız Seçim Barı
                            Text(
                                "Puanınız (${state.rating.toInt()} / 5 Yıldız):",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                (1..5).forEach { star ->
                                    val isSelected = star <= state.rating.toInt()
                                    OutlinedButton(
                                        onClick = { viewModel.updateRating(star.toDouble()) },
                                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.SecondaryContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text(
                                            if (isSelected) "★ $star" else "☆ $star",
                                            style = TourOSTypography.Label.copy(
                                                color = if (isSelected) TourOSColors.Secondary else TourOSColors.TextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            TourOSTextField(
                                value = commentInput,
                                onValueChange = { commentInput = it },
                                label = "Tur Deneyiminiz ve Yorumunuz",
                                placeholder = "Rehberlik, araç konforu ve otel hakkında düşünceleriniz...",
                                modifier = Modifier.fillMaxWidth()
                            )

                            TourOSButton(
                                text = "🌟 Değerlendirmeyi Gönder",
                                onClick = { viewModel.submitReview(commentInput) },
                                enabled = !state.isLoading && commentInput.isNotBlank(),
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── KRONOLOJİK BİLDİRİM KARTI (OKUNMAMIŞLAR PRIMARY CONTAINER İLE VURGULANIR) ──

@Composable
private fun B2CPushNotificationCard(notif: B2CPushNotificationItem) {
    // OKUNMAMIŞLAR PRIMARY CONTAINER ARKA PLANLA VURGULANIR (Strict Rule)
    val cardBg = if (!notif.isRead) {
        TourOSColors.PrimaryContainer.copy(alpha = 0.45f)
    } else {
        TourOSColors.Surface
    }

    val icon = if (notif.category == "REMINDER") "🎈" else "🔥"

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBg,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalAlignment = Alignment.Top
        ) {
            Text(icon, style = TourOSTypography.TitleLarge)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notif.title,
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )

                    if (!notif.isRead) {
                        TourOSStatusBadge(
                            text = "🔵 YENİ",
                            backgroundColor = TourOSColors.Primary,
                            textColor = TourOSColors.OnPrimary
                        )
                    }
                }

                Text(
                    notif.body,
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                )

                Text(
                    "🕒 ${notif.createdAt}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
        }
    }
}
