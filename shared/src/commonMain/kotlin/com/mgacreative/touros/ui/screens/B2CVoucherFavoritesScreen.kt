package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mgacreative.touros.domain.model.B2CCustomerVoucherItem
import com.mgacreative.touros.domain.model.B2CFavoriteTourItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CVoucherFavoritesViewModel

/**
 * B2C Voucher & Favorilerim Ekranı — TourOS 0.3
 *
 * İki sekme: '1. Voucher'larım' ve '2. Favorilerim'.
 * Her iki sekmede kart listesi; veri olmadığında TourOSEmptyState bileşeni gösterilir.
 */
@Composable
fun B2CVoucherFavoritesScreen(
    viewModel: B2CVoucherFavoritesViewModel,
    onNavigateToTourDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Voucher & Favorilerim",
                subtitle = "Seyahat belgeleriniz ve takip ettiğiniz rotalar",
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
            // ── 1. İKİ SEKMELİ TAB BAR ─────────────────────────────────────────
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
                        "🎟️ Voucher'larım (${state.vouchers.size})",
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
                        "❤️ Favorilerim (${state.favoriteTours.size})",
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

            // ── 2. SEKMELERE GÖRE KART LİSTESİ VEYA TOUROSEMPTYSTATE ────────────
            if (state.selectedTab == 0) {
                // TAB 0: VOUCHER'LARIM
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else if (state.vouchers.isEmpty()) {
                    // BOŞ DURUMDA TOUROSEMPTYSTATE BİLEŞENİ
                    TourOSEmptyState(
                        title = "Henüz Voucher Kaydınız Bulunmuyor",
                        description = "Satın aldığınız seyahat belgeleri ve onay voucher'ları bu sekmede listelenir.",
                        icon = { Text("🎟️", style = TourOSTypography.DisplaySmall) },
                        actionButtonText = "Turları Keşfet",
                        onActionClick = { onNavigateToTourDetail("t101") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.vouchers) { voucher ->
                            B2CVoucherCardItem(voucher = voucher)
                        }
                    }
                }
            } else {
                // TAB 1: FAVORİLERİM
                if (state.favoriteTours.isEmpty()) {
                    // BOŞ DURUMDA TOUROSEMPTYSTATE BİLEŞENİ
                    TourOSEmptyState(
                        title = "Henüz Favori Tur Eklemediniz",
                        description = "Beğendiğiniz seyahat rotalarını favorilerinize ekleyerek daha sonra kolayca ulaşabilirsiniz.",
                        icon = { Text("❤️", style = TourOSTypography.DisplaySmall) },
                        actionButtonText = "Turlara Göz At",
                        onActionClick = { onNavigateToTourDetail("t101") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.favoriteTours) { tour ->
                            B2CFavoriteTourCardItem(
                                tour = tour,
                                onRemove = { viewModel.toggleFavorite(tour.tourId) },
                                onDetail = { onNavigateToTourDetail(tour.tourId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── B2C VOUCHER KART BİLEŞENİ ────────────────────────────────────────────────

@Composable
private fun B2CVoucherCardItem(voucher: B2CCustomerVoucherItem) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    voucher.bookingCode,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )

                TourOSStatusBadge(
                    text = "ONAYLI VOUCHER",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }

            Text(
                voucher.tourTitle,
                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
            )

            Text(
                "Otel: ${voucher.hotelName}  ·  Tarih: ${voucher.departureDate} (${voucher.paxCount} Pax)",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    TourOSButton(
                        text = "👁️ Görüntüle",
                        onClick = { },
                        variant = TourOSButtonVariant.TERTIARY
                    )

                    TourOSButton(
                        text = "📥 PDF İndir",
                        onClick = { },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

// ─── B2C FAVORİ TUR KART BİLEŞENİ ─────────────────────────────────────────────

@Composable
private fun B2CFavoriteTourCardItem(
    tour: B2CFavoriteTourItem,
    onRemove: () -> Unit,
    onDetail: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSStatusBadge(
                    text = tour.category,
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )

                TourOSStatusBadge(
                    text = "⭐ ${tour.rating}",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary
                )
            }

            Text(
                tour.tourTitle,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            Text(
                "Başlangıç Fiyatı: ₺ ${formatFavMoney(tour.price)}",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRemove) {
                    Text("💔", style = TourOSTypography.TitleLarge)
                }

                TourOSButton(
                    text = "✈️ Tura Git →",
                    onClick = onDetail,
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        }
    }
}

private fun formatFavMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}
