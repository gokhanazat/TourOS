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
import com.mgacreative.touros.domain.model.B2BAgencyVoucherItem
import com.mgacreative.touros.ui.viewmodel.B2BAgencyVouchersViewModel

/**
 * 4.1.4 B2B Acente Voucher İndirme ve Yazdırma Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2BAgencyVouchersScreen(
    viewModel: B2BAgencyVouchersViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val filteredVouchers = remember(state.vouchers, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.vouchers
        else state.vouchers.filter {
            it.bookingCode.contains(state.searchQuery, ignoreCase = true) ||
            it.guestName.contains(state.searchQuery, ignoreCase = true) ||
            it.tourTitle.contains(state.searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🖨️ Voucher Yazdırma & İndirme", fontWeight = FontWeight.Bold) },
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
            // 1. Arama Çubuğu
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Rezervasyon Kodu veya Misafir Adı Ara...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Voucher Belgeleri Listesi
            Text("🎟️ İndirilebilir Voucher Belgeleri (${filteredVouchers.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredVouchers) { item ->
                        AgencyVoucherCard(item = item, onPrint = { viewModel.printVoucher(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun AgencyVoucherCard(item: B2BAgencyVoucherItem, onPrint: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🎟️", fontSize = 20.sp)
                    Column {
                        Text(item.bookingCode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(item.guestName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("${item.paxCount} Kişi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Tur: ${item.tourTitle}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Otel: ${item.hotelName} | Kalkış: ${item.departureDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val mbInt = item.fileSizeBytes / 1048576
                val mbDec = (item.fileSizeBytes % 1048576) / 104857
                Text("Boyut: ${mbInt}.${mbDec} MB | Yazdırıldı: ${item.printedCount} Kez", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("📥 PDF İndir", fontSize = 10.sp)
                    }
                    Button(
                        onClick = onPrint,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🖨️ Yazdır", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
