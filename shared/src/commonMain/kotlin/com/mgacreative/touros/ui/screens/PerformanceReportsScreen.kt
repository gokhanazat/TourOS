package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.CancellationMetrics
import com.mgacreative.touros.domain.model.PerformerRanking
import com.mgacreative.touros.domain.model.TopTourPerformance
import com.mgacreative.touros.ui.viewmodel.PerformanceReportsViewModel

/**
 * 3.3.3 Performans Raporları Ekranı (En Çok Satan Turlar, İptal Oranı, Skorkart).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceReportsScreen(
    viewModel: PerformanceReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val tabs = listOf("🏆 Top Turlar", "⚠️ İptal Oranı", "👥 Skorkartı")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Performans Raporları", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Sekmeler
            PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (state.selectedTab) {
                    0 -> TopToursView(tours = state.topTours)
                    1 -> CancellationAnalysisView(metrics = state.cancellationMetrics)
                    2 -> PerformersLeaderboardView(performers = state.performers)
                }
            }
        }
    }
}

@Composable
fun TopToursView(tours: List<TopTourPerformance>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("🏆 En Çok Satan & En Kârlı Turlar Sıralaması:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        itemsIndexed(tours) { index, tour ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (index) {
                                0 -> Color(0xFFFEF08A)
                                1 -> Color(0xFFE2E8F0)
                                2 -> Color(0xFFFFEDD5)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                "#${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tour.tourTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${tour.totalSalesCount} Toplam Satış | Kâr Marjı: %${tour.profitMargin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("${tour.totalRevenue} TRY", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Net Kâr: ${tour.netProfit} TRY", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                    }
                }
            }
        }
    }
}

@Composable
fun CancellationAnalysisView(metrics: CancellationMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("⚠️ İptal Oranı ve Risk Analiz Raporu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("📊 Genel İptal Oranı:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val rateColor = if (metrics.cancellationRate <= 8.0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                    Text("%${metrics.cancellationRate}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = rateColor)
                }

                LinearProgressIndicator(
                    progress = { (metrics.cancellationRate / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (metrics.cancellationRate <= 8.0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Toplam Rezervasyon", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${metrics.totalBookings} Adet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("İptal Edilen Rezervasyon", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${metrics.cancelledBookings} Adet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun PerformersLeaderboardView(performers: List<PerformerRanking>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("👥 Personel / Acente / Rehber Performans Skorkartı:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(performers) { performer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(performer.performerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(performer.performerType, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("${performer.completedJobs} Tamamlanan Operasyon | ⭐ ${performer.avgRating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text("${performer.totalRevenue} TRY", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
