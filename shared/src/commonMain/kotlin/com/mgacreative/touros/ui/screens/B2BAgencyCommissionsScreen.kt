package com.mgacreative.touros.ui.screens

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
import com.mgacreative.touros.domain.model.B2BAgencyCommissionItem
import com.mgacreative.touros.ui.viewmodel.B2BAgencyCommissionsViewModel

/**
 * 4.1.3 B2B Tur Bazlı ve Dönemsel Komisyon Döküm Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2BAgencyCommissionsScreen(
    viewModel: B2BAgencyCommissionsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val filteredCommissions = remember(state.commissions, state.selectedPeriod) {
        if (state.selectedPeriod == "Tüm Dönemler") state.commissions
        else state.commissions.filter { it.periodName == state.selectedPeriod }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💰 B2B Komisyon Dökümü", fontWeight = FontWeight.Bold) },
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
            // 1. Dönem Filtre Çipleri
            Text("📅 Hakediş Dönemi Seçimi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.availablePeriods.forEach { period ->
                    FilterChip(
                        selected = state.selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(period, fontSize = 10.sp) }
                    )
                }
            }

            // 2. Özet Komisyon Metrik Kartları
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💵 Toplam Komisyon", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.totalEarnedCommission} TRY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⏳ Bekleyen Hak Ediş", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.pendingCommission} TRY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                    }
                }
            }

            // 3. Tur Bazlı Komisyon Listesi
            Text("📋 Tur Bazlı Komisyon Dökümü (${filteredCommissions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredCommissions) { item ->
                        CommissionItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun CommissionItemCard(item: B2BAgencyCommissionItem) {
    val (statusColor, statusText) = when (item.status) {
        "ODENDI" -> Color(0xFF15803D) to "✅ ÖDENDİ"
        "BEKLIYOR" -> Color(0xFFEA580C) to "⏳ BEKLİYOR"
        else -> MaterialTheme.colorScheme.primary to "👍 HAK EDİLDİ"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.tourTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Satış Adedi: ${item.bookingCount} Rezervasyon", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Komisyon Oranı: %${item.commissionRate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Top. Brüt Ciro", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${item.grossSalesAmount} TRY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Kazanılan Net Komisyon", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${item.commissionAmount} TRY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
        }
    }
}
