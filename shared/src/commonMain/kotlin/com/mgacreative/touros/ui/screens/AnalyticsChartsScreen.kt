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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.CountrySalesData
import com.mgacreative.touros.domain.model.DailySalesData
import com.mgacreative.touros.ui.viewmodel.AnalyticsChartsViewModel

/**
 * 3.3.2 Analitik Grafikleri Ekranı (Günlük Satışlar & Ülke Dağılımı).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsChartsScreen(
    viewModel: AnalyticsChartsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📈 Analitik Grafikleri & Raporlar", fontWeight = FontWeight.Bold) },
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
            // 1. Zaman Aralığı Filtresi
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedDays == 7,
                    onClick = { viewModel.loadData(7) },
                    label = { Text("📅 Son 7 Gün", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.selectedDays == 14,
                    onClick = { viewModel.loadData(14) },
                    label = { Text("🗓️ Son 14 Gün", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.selectedDays == 30,
                    onClick = { viewModel.loadData(30) },
                    label = { Text("📊 Son 30 Gün", fontSize = 11.sp) }
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                    // 2. Günlük Satış Trendi Grafiği (Bar Chart)
                    item {
                        DailySalesBarChartCard(dailySales = state.dailySales)
                    }

                    // 3. Ülke Bazlı Satış Dağılımı Grafiği (Country Breakdown)
                    item {
                        Text("🌐 Ülke Bazlı Satış Dağılımı:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    items(state.countrySales) { country ->
                        CountrySalesProgressCard(country = country)
                    }
                }
            }
        }
    }
}

@Composable
fun DailySalesBarChartCard(dailySales: List<DailySalesData>) {
    val maxAmount = (dailySales.maxOfOrNull { it.totalAmount } ?: 1.0).coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📊 Günlük Satış Trendi (TRY)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val totalPeriodSales = dailySales.sumOf { it.totalAmount }
                Text("${totalPeriodSales.toInt()} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Visual Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailySales.forEach { dayData ->
                    val ratio = (dayData.totalAmount / maxAmount).toFloat().coerceIn(0.08f, 1.0f)
                    val barHeight = (120 * ratio).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${(dayData.totalAmount / 1000).toInt()}k",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(barHeight)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val shortDate = if (dayData.saleDate.length >= 5) dayData.saleDate.takeLast(5) else dayData.saleDate
                        Text(
                            text = shortDate,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountrySalesProgressCard(country: CountrySalesData) {
    val flag = when (country.countryCode.uppercase()) {
        "DE" -> "🇩🇪"
        "GB" -> "🇬🇧"
        "RU" -> "🇷🇺"
        "US" -> "🇺🇸"
        "AE" -> "🇦🇪"
        else -> "🇹🇷"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$flag ${country.countryName} (${country.bookingCount} Rezervasyon)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("%${country.percentage}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }

            LinearProgressIndicator(
                progress = { (country.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Ciro: ${country.totalAmount} TRY", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
