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
import com.mgacreative.touros.domain.model.GuidePerformanceItem
import com.mgacreative.touros.domain.model.GuidePerformanceSummary
import com.mgacreative.touros.ui.viewmodel.GuidePerformanceReportViewModel
import com.mgacreative.touros.ui.viewmodel.GuidePerformanceUiState

/**
 * 2.5.5 Rehber Performans Raporu Yönetim Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidePerformanceReportScreen(
    viewModel: GuidePerformanceReportViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Rehber Performans Raporu", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is GuidePerformanceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GuidePerformanceUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is GuidePerformanceUiState.Success -> {
                    // 1. KPI Performans Özet Kartları
                    GuidePerformanceKpiSection(summary = state.summary)

                    // 2. Performans Tablosu ve Detay Rehber Kartları
                    Text("🏆 Rehber Kadrosu Derecelendirme ve Tur İstatistikleri:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.summary.guides) { item ->
                            GuidePerformanceCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuidePerformanceKpiSection(summary: GuidePerformanceSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👨‍💼 Rehber Kadrosu", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${summary.totalActiveGuides} Aktif Rehber", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF08A))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⭐ Ortalama Kadro Puanı", fontSize = 10.sp, color = Color(0xFF854D0E))
                    Text("${summary.avgFleetRating} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🚩 Toplam Tamamlanan Tur", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text("${summary.totalToursExecuted} Tur", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🏆 En Yüksek Puanlı Rehber", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summary.topRatedGuideName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GuidePerformanceCard(item: GuidePerformanceItem) {
    val isStarGuide = item.performanceLevel == "Yıldız Rehber"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStarGuide) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isStarGuide) Color(0xFFFEF08A) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isStarGuide) "🏆 ${item.performanceLevel}" else "🌟 ${item.performanceLevel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStarGuide) Color(0xFF854D0E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "⭐ ${item.rating} / 5.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text(item.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Kokart No: ${item.licenseNumber ?: "Lisanslı"} | Uzmanlık: ${item.specialization ?: "Kültür Turları"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Puan İlerleme Çubuğu
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Puan Performansı", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%${(item.rating * 20).toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { (item.rating / 5.0).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (isStarGuide) Color(0xFFEAB308) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🚩 Tamamlanan Tur: ${item.totalToursCompleted}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("💬 Müşteri Değerlendirmesi: ${item.totalReviews} (${item.fiveStarReviews} Tanesi ⭐ 5 Yıldız)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
