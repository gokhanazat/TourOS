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
import com.mgacreative.touros.domain.model.B2CCustomerVoucherItem
import com.mgacreative.touros.domain.model.B2CFavoriteTourItem
import com.mgacreative.touros.ui.viewmodel.B2CVoucherFavoritesViewModel

/**
 * 4.2.4 B2C Müşteri Voucher Görüntüleme ve Favori Turlar Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2CVoucherFavoritesScreen(
    viewModel: B2CVoucherFavoritesViewModel,
    onNavigateToTourDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📄 Voucher & ❤️ Favorilerim", fontWeight = FontWeight.Bold) },
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
            // Tab Selector
            SecondaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(selected = state.selectedTab == 0, onClick = { viewModel.selectTab(0) }) {
                    Text("1. 📄 Voucher Belgelerim", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. ❤️ Favori Turlarım (${state.favoriteTours.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Voucher Belgelerim
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.vouchers) { voucher ->
                            B2CVoucherCardItem(voucher = voucher)
                        }
                    }
                }
            } else {
                // TAB 1: Favori Turlarım
                if (state.favoriteTours.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("💔 Henüz favori tur eklemediniz.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.favoriteTours) { tour ->
                            B2CFavoriteTourCardItem(tour = tour, onRemove = { viewModel.toggleFavorite(tour.tourId) }, onDetail = { onNavigateToTourDetail(tour.tourId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun B2CVoucherCardItem(voucher: B2CCustomerVoucherItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(voucher.bookingCode, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                    Text("ONAYLI VOUCHER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Text(voucher.tourTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Otel: ${voucher.hotelName} | Tarih: ${voucher.departureDate} (${voucher.paxCount} Kişi)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("👁️ Görüntüle", fontSize = 10.sp)
                    }
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("📥 PDF İndir", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun B2CFavoriteTourCardItem(tour: B2CFavoriteTourItem, onRemove: () -> Unit, onDetail: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tour.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("⭐ ${tour.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
            }

            Text(tour.tourTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Başlangıç Fiyatı: ${tour.price} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Text("💔", fontSize = 18.sp)
                }

                Button(onClick = onDetail, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("✈️ Tura Git >", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
