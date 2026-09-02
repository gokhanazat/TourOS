package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Badge
import androidx.compose.ui.graphics.vector.ImageVector
import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.GuideStatusInfo
import com.mgacreative.touros.domain.model.UpcomingTour
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.model.VehicleOccupancy
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DashboardUiState
import com.mgacreative.touros.ui.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS Ultra-Kompakt Kurumsal ERP Dashboard Ekranı.
 *
 * - Üstte 5'li Kompakt KPI Şeridi (Tek satır, düşük yükseklik).
 * - Sol Bölüm (%60): Yaklaşan Tur & Otel Operasyonları, Araç Dolulukları ve Rehber Durumları.
 * - Sağ Bölüm (%40): Hızlı Aksiyon Kısayolları, Operasyonel Canlı Uyarılar ve Mini Finansal Trend Grafiği.
 */
@Composable
fun DashboardScreen(
    currentRole: UserRole = UserRole.TOUR_OPERATOR,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onNavigateToTours: () -> Unit = {},
    onNavigateToHotels: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel()
) {
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TourOSSpacing.large, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyon Dashboard & Yönetim"),
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yetki")}: ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(currentRole.displayName)} • ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Canlı İşletme & Operasyon Özeti")}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TourOSStatusBadge(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Canlı Sistem Aktif"),
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success
                    )
                    TourOSButton(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Çıkış"),
                        onClick = onNavigateToLogin,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            }
        },
        containerColor = TourOSColors.Surface,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.xSmall)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    TourOSLoadingIndicator(message = "Dashboard metrikleri yükleniyor...")
                }
                is DashboardUiState.Error -> {
                    TourOSEmptyState(
                        title = "Dashboard Yüklenemedi",
                        description = state.message
                    )
                }
                is DashboardUiState.Success -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isExpanded = maxWidth >= 860.dp

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // ── 1. ÜST KOMPAKT 5'Lİ KPI ŞERİDİ ─────────────────────
                            item {
                                CompactKpiRow(summary = state.summary, isExpanded = isExpanded)
                            }

                            // ── 2. ANA ÇİFT SÜTUNLU DÜZEN (Sol: %58 Operasyon, Sağ: %42 Finans & Aksiyon)
                            item {
                                if (isExpanded) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                    ) {
                                        // SOL KOLON (%58): OPERASYON VE DOLULUK
                                        Column(
                                            modifier = Modifier.weight(1.35f),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            CompactUpcomingOperationsWidget(
                                                tours = state.upcomingTours,
                                                onViewAllClick = onNavigateToBookings
                                            )
                                            CompactVehicleOccupancyWidget(vehicles = state.vehicleOccupancies)
                                            CompactGuideStatusWidget(guides = state.guideStatuses)
                                        }

                                        // SAĞ KOLON (%42): HIZLI AKSİYONLAR, UYARILAR VE GRAFİKLER
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            QuickActionsWidget(
                                                onNavigateToBookings = onNavigateToBookings,
                                                onNavigateToTours = onNavigateToTours,
                                                onNavigateToHotels = onNavigateToHotels,
                                                onNavigateToReports = onNavigateToReports
                                            )
                                            OperationalAlertsWidget()
                                            DashboardChartsSection(charts = state.analyticsCharts)
                                        }
                                    }
                                } else {
                                    // MOBİL / DAR EKRAN
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                        QuickActionsWidget(
                                            onNavigateToBookings = onNavigateToBookings,
                                            onNavigateToTours = onNavigateToTours,
                                            onNavigateToHotels = onNavigateToHotels,
                                            onNavigateToReports = onNavigateToReports
                                        )
                                        OperationalAlertsWidget()
                                        CompactUpcomingOperationsWidget(
                                            tours = state.upcomingTours,
                                            onViewAllClick = onNavigateToBookings
                                        )
                                        CompactVehicleOccupancyWidget(vehicles = state.vehicleOccupancies)
                                        CompactGuideStatusWidget(guides = state.guideStatuses)
                                        DashboardChartsSection(charts = state.analyticsCharts)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── 1. KOMPAKT 5'Lİ KPI ŞERİDİ ───────────────────────────────────────────────

@Composable
private fun CompactKpiRow(summary: DashboardSummary, isExpanded: Boolean) {
    if (isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            KpiStatItem(
                title = "Günlük Ciro",
                value = "₺ ${formatCurrency(summary.dailySales)}",
                trend = "+14%",
                trendIcon = Icons.Default.TrendingUp,
                trendColor = TourOSColors.Success,
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f)
            )
            KpiStatItem(
                title = "Aylık Ciro",
                value = "₺ ${formatCurrency(summary.monthlySales)}",
                trend = "+8%",
                trendIcon = Icons.Default.TrendingUp,
                trendColor = TourOSColors.Success,
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f)
            )
            KpiStatItem(
                title = "Doluluk Oranı",
                value = "%${summary.occupancyRate.toInt()}",
                trend = "Normal",
                trendIcon = Icons.Default.CheckCircle,
                trendColor = TourOSColors.Success,
                icon = Icons.Default.Analytics,
                modifier = Modifier.weight(1f)
            )
            KpiStatItem(
                title = "İptal / İade",
                value = "${summary.cancellationCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Adet")}",
                trend = "Düşük",
                trendIcon = Icons.Default.TrendingDown,
                trendColor = TourOSColors.Success,
                icon = Icons.Default.Cancel,
                modifier = Modifier.weight(1f)
            )
            KpiStatItem(
                title = "Bekleyen Alacak",
                value = "₺ ${formatCurrency(summary.pendingPaymentsAmount)}",
                trend = "Vade",
                trendIcon = Icons.Default.Schedule,
                trendColor = TourOSColors.Warning,
                icon = Icons.Default.CreditCard,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            modifier = Modifier.fillMaxWidth().height(170.dp)
        ) {
            item {
                KpiStatItem(
                    title = "Günlük Ciro",
                    value = "₺ ${formatCurrency(summary.dailySales)}",
                    trend = "+14%",
                    trendIcon = Icons.Default.TrendingUp,
                    trendColor = TourOSColors.Success,
                    icon = Icons.Default.AccountBalanceWallet
                )
            }
            item {
                KpiStatItem(
                    title = "Aylık Ciro",
                    value = "₺ ${formatCurrency(summary.monthlySales)}",
                    trend = "+8%",
                    trendIcon = Icons.Default.TrendingUp,
                    trendColor = TourOSColors.Success,
                    icon = Icons.Default.CalendarMonth
                )
            }
            item {
                KpiStatItem(
                    title = "Doluluk",
                    value = "%${summary.occupancyRate.toInt()}",
                    trend = "Normal",
                    trendIcon = Icons.Default.CheckCircle,
                    trendColor = TourOSColors.Success,
                    icon = Icons.Default.Analytics
                )
            }
            item {
                KpiStatItem(
                    title = "Bekleyen Alacak",
                    value = "₺ ${formatCurrency(summary.pendingPaymentsAmount)}",
                    trend = "Vade",
                    trendIcon = Icons.Default.Schedule,
                    trendColor = TourOSColors.Warning,
                    icon = Icons.Default.CreditCard
                )
            }
        }
    }
}

@Composable
private fun KpiStatItem(
    title: String,
    value: String,
    trend: String,
    trendIcon: ImageVector,
    trendColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TourOSSpacing.medium, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(title),
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
                Text(
                    text = value,
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TourOSColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(trend),
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }
        }
    }
}

// ─── 2. HIZLI AKSİYON KISAYOLLARI WIDGET'I ────────────────────────────────────

@Composable
private fun QuickActionsWidget(
    onNavigateToBookings: () -> Unit,
    onNavigateToTours: () -> Unit,
    onNavigateToHotels: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = TourOSColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hızlı Operasyon Kısayolları"),
                    style = TourOSTypography.TitleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                QuickActionChip(label = "+ Rezervasyon", icon = Icons.Default.ConfirmationNumber, onClick = onNavigateToBookings, modifier = Modifier.weight(1f))
                QuickActionChip(label = "+ Tur Ekle", icon = Icons.Default.DirectionsBus, onClick = onNavigateToTours, modifier = Modifier.weight(1f))
                QuickActionChip(label = "+ Otel Tanımla", icon = Icons.Default.Hotel, onClick = onNavigateToHotels, modifier = Modifier.weight(1f))
                QuickActionChip(label = "Rapor Al", icon = Icons.Default.Assessment, onClick = onNavigateToReports, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, TourOSColors.Border, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        color = TourOSColors.Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TourOSColors.Primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(label),
                style = TourOSTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = TourOSColors.TextPrimary,
                maxLines = 1
            )
        }
    }
}

// ─── 3. OPERASYONEL CANLI UYARILAR WIDGET'I ────────────────────────────────────

@Composable
private fun OperationalAlertsWidget() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel Canlı Uyarılar"),
                        style = TourOSTypography.TitleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                }
                TourOSStatusBadge(
                    text = "3 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bildirim")}",
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            AlertItemRow(icon = Icons.Default.Schedule, iconColor = Color(0xFFF59E0B), text = "2 Rezervasyonun opsiyon süresi bugün 18:00'de doluyor.", tag = "Opsiyon")
            AlertItemRow(icon = Icons.Default.Warning, iconColor = Color(0xFFE11D48), text = "Kapadokya Turu %85 doluluğa ulaştı. Kontenjan kontrolü önerilir.", tag = "Kontenjan")
            AlertItemRow(icon = Icons.Default.CreditCard, iconColor = Color(0xFF0284C7), text = "Vadesi gelen 3 acente cari ödemesi bekliyor.", tag = "Tahsilat")
        }
    }
}

@Composable
private fun AlertItemRow(icon: ImageVector, iconColor: Color, text: String, tag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(TourOSColors.Surface)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(text),
                style = TourOSTypography.Caption,
                color = TourOSColors.TextPrimary,
                maxLines = 1
            )
        }
        Text(
            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(tag),
            style = TourOSTypography.Caption,
            fontWeight = FontWeight.Bold,
            color = TourOSColors.Primary
        )
    }
}

// ─── 4. YAKLAŞAN TUR & OTEL OPERASYONLARI ─────────────────────────────────────

@Composable
private fun CompactUpcomingOperationsWidget(
    tours: List<UpcomingTour>,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yaklaşan Tur & Otel Operasyonları"),
                        style = TourOSTypography.TitleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAllClick() }
                ) {
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümünü Gör"),
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (tours.isEmpty()) {
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz planlanmış aktif operasyon kaydı bulunmuyor."),
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    tours.take(4).forEach { tour ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.Surface)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = TourOSColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = tour.tourTitle,
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = TourOSColors.TextSecondary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = tour.departureDate,
                                            style = TourOSTypography.Caption,
                                            color = TourOSColors.TextSecondary
                                        )
                                    }
                                }
                            }

                            TourOSStatusBadge(
                                text = "${tour.bookedCount}/${tour.capacity} Pax",
                                backgroundColor = TourOSColors.PrimaryContainer,
                                textColor = TourOSColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 5. ARAÇ & KONTENJAN DOLULUKLARI ─────────────────────────────────────────

@Composable
private fun CompactVehicleOccupancyWidget(vehicles: List<VehicleOccupancy>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = TourOSColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Araç & Transfer Doluluk Takibi"),
                    style = TourOSTypography.TitleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
            }

            if (vehicles.isEmpty()) {
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif araç operasyonu bulunmuyor."),
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
            } else {
                vehicles.take(3).forEach { vehicle ->
                    val safeCapacity = if (vehicle.totalCapacity > 0) vehicle.totalCapacity else 46
                    val occupancyPercentage = (vehicle.occupiedSeats * 100 / safeCapacity).coerceIn(0, 100)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${vehicle.plateNumber} (${vehicle.modelName})",
                                style = TourOSTypography.Caption,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                            Text(
                                text = "%$occupancyPercentage (${vehicle.occupiedSeats}/$safeCapacity)",
                                style = TourOSTypography.Caption,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.Primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (vehicle.occupiedSeats.toFloat() / safeCapacity.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.5.dp)),
                            color = TourOSColors.Primary,
                            trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

// ─── 6. REHBER & SAHA GÖREV DURUMLARI ────────────────────────────────────────

@Composable
private fun CompactGuideStatusWidget(guides: List<GuideStatusInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
        border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = TourOSColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel Rehber & Saha Durumu"),
                    style = TourOSTypography.TitleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
            }

            if (guides.isEmpty()) {
                Text(
                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kayıtlı rehber görev durumu bulunmuyor."),
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    guides.take(3).forEach { guide ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TourOSColors.Surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = guide.fullName,
                                        style = TourOSTypography.Caption,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (guide.status == "ON_DUTY" || guide.status == "GÖREVDE") TourOSColors.Success else Color(0xFFE5A93C))
                                    )
                                }
                                Text(
                                    text = guide.assignedTourTitle ?: com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müsait"),
                                    style = TourOSTypography.Caption,
                                    color = TourOSColors.TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    return com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(value, decimals = false)
}
