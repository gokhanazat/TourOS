package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.GuideStatusInfo
import com.mgacreative.touros.domain.model.UpcomingTour
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.model.VehicleOccupancy
import com.mgacreative.touros.ui.components.DashboardChartsSection
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.navigation.getNavigationItemsForRole
import com.mgacreative.touros.ui.theme.NavigationType
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.theme.calculateNavigationType
import com.mgacreative.touros.ui.viewmodel.DashboardUiState
import com.mgacreative.touros.ui.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Operasyon Dashboard Ekranı.
 * - Üstte KPI kartları grid'i (Expanded: 4-6 sütun, Compact: 2 sütun).
 * - Her kart tek büyük sayı + küçük trend ikonu.
 * - Altta iki sütunlu bölüm: Solda liste widget'ları, sağda grafik paneli (Compact'ta alt alta).
 * - Grafiklerde sadece Primary ve Accent/Secondary tonlarının opaklıkları kullanılır.
 */
@Composable
fun DashboardScreen(
    currentRole: UserRole = UserRole.TOUR_OPERATOR,
    onNavigateToLogin: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel()
) {
    val navigationType = calculateNavigationType()
    val visibleItems = getNavigationItemsForRole(currentRole)
    val uiState by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        if (navigationType == NavigationType.NAVIGATION_RAIL ||
            navigationType == NavigationType.PERMANENT_NAVIGATION_DRAWER
        ) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = TourOSColors.Background
            ) {
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                visibleItems.forEach { item ->
                    NavigationRailItem(
                        selected = item.title == "Ana Sayfa",
                        onClick = { },
                        icon = { },
                        label = { Text(item.title, style = TourOSTypography.Caption) }
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                TourOSTopBar(
                    title = "Operasyon Dashboard",
                    subtitle = "Rol: ${currentRole.displayName} • Canlı İşletme & Operasyon Özeti",
                    actions = {
                        TourOSButton(
                            text = "Çıkış Yap",
                            onClick = onNavigateToLogin,
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }
                )
            },
            containerColor = TourOSColors.Surface,
            modifier = Modifier.weight(1f)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(TourOSSpacing.large)
            ) {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> {
                        TourOSLoadingIndicator(message = "Dashboard metrikleri ve veriler yükleniyor...")
                    }
                    is DashboardUiState.Error -> {
                        TourOSEmptyState(
                            title = "Dashboard Yüklenemedi",
                            description = state.message
                        )
                    }
                    is DashboardUiState.Success -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val isExpanded = maxWidth >= 960.dp
                            val kpiColumns = if (isExpanded) 5 else 2

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // 1. Üst KPI Kartları Grid'i
                                item {
                                    KpiGridSection(summary = state.summary, columnsCount = kpiColumns)
                                }

                                // 2. İki Sütunlu Bölüm (Expanded: Yan Yana, Compact: Alt Alta)
                                item {
                                    if (isExpanded) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                                        ) {
                                            // Solda Liste Widget'ları
                                            Column(
                                                modifier = Modifier.weight(1.2f),
                                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                                            ) {
                                                UpcomingToursWidget(tours = state.upcomingTours)
                                                VehicleOccupancyWidget(vehicles = state.vehicleOccupancies)
                                                GuideStatusWidget(guides = state.guideStatuses)
                                            }

                                            // Sağda Grafik Paneli
                                            Column(modifier = Modifier.weight(1f)) {
                                                DashboardChartsSection(charts = state.analyticsCharts)
                                            }
                                        }
                                    } else {
                                        // Compact Alt Alta
                                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)) {
                                            UpcomingToursWidget(tours = state.upcomingTours)
                                            VehicleOccupancyWidget(vehicles = state.vehicleOccupancies)
                                            GuideStatusWidget(guides = state.guideStatuses)
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
}

@Composable
private fun KpiGridSection(summary: DashboardSummary, columnsCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Text(
            text = "📊 Key Performance Indicators (KPI)",
            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            item {
                KpiStatCard(
                    title = "Günlük Ciro",
                    value = "${summary.dailySales} ₺",
                    trendText = "↗ +14.2%"
                )
            }
            item {
                KpiStatCard(
                    title = "Bu Ay Toplam",
                    value = "${summary.monthlySales} ₺",
                    trendText = "↗ +8.5%"
                )
            }
            item {
                KpiStatCard(
                    title = "Doluluk Oranı",
                    value = "%${summary.occupancyRate}",
                    trendText = "↗ +5.1%"
                )
            }
            item {
                KpiStatCard(
                    title = "İptal Sayısı",
                    value = "${summary.cancellationCount}",
                    trendText = "↘ -2.4%"
                )
            }
            item {
                KpiStatCard(
                    title = "Bekleyen Alacak",
                    value = "${summary.pendingPaymentsAmount} ₺",
                    trendText = "↗ +1.1%"
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    trendText: String
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                TourOSStatusBadge(
                    text = trendText,
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

            Text(
                text = value,
                style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
            )
        }
    }
}

@Composable
private fun UpcomingToursWidget(tours: List<UpcomingTour>) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.large
    ) {
        Text(text = "🗓️ Yaklaşan Turlar", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        tours.forEach { tour ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tour.tourTitle, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    Text(text = "📅 Kalkış: ${tour.departureDate}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
                TourOSStatusBadge(
                    text = "${tour.bookedCount}/${tour.capacity} Pax",
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }
            HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))
        }
    }
}

@Composable
private fun VehicleOccupancyWidget(vehicles: List<VehicleOccupancy>) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.large
    ) {
        Text(text = "🚌 Araç Dolulukları Özeti", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        vehicles.forEach { vehicle ->
            val safeCapacity = if (vehicle.totalCapacity > 0) vehicle.totalCapacity else 46
            val occupancyPercentage = (vehicle.occupiedSeats * 100 / safeCapacity).coerceIn(0, 100)
            val progressFraction = (vehicle.occupiedSeats.toFloat() / safeCapacity.toFloat()).coerceIn(0f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${vehicle.plateNumber} (${vehicle.modelName})", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    TourOSStatusBadge(
                        text = "%$occupancyPercentage Dolu",
                        backgroundColor = TourOSColors.PrimaryContainer,
                        textColor = TourOSColors.Primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                    color = TourOSColors.Primary,
                    trackColor = TourOSColors.PrimaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "👨‍✈️ Kaptan: ${vehicle.driverName}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(text = "💺 ${vehicle.occupiedSeats}/$safeCapacity Koltuk", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                }
            }
            HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))
        }
    }
}

@Composable
private fun GuideStatusWidget(guides: List<GuideStatusInfo>) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.large
    ) {
        Text(text = "🚩 Rehber Durumu & Performans", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        guides.forEach { guide ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = guide.fullName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    Text(text = "Diller: ${guide.languages.joinToString(", ")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    TourOSStatusBadge(
                        text = "⭐ 4.9",
                        backgroundColor = TourOSColors.Secondary.copy(alpha = 0.2f),
                        textColor = TourOSColors.Secondary
                    )
                    TourOSStatusBadge(
                        text = guide.status,
                        backgroundColor = TourOSColors.PrimaryContainer,
                        textColor = TourOSColors.Primary
                    )
                }
            }
            HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))
        }
    }
}
