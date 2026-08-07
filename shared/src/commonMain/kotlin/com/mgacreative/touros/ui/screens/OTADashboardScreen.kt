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
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

data class OTAChannelUiModel(
    val providerId: String,
    val providerName: String,
    val isConnected: Boolean,
    val lastSyncedAt: String,
    val failedJobCount: Int,
    val bookingCount: Int,
    val productCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTADashboardScreen(
    viewModel: OTAHubViewModel,
    tenantId: String = "tenant-001",
    onNavigateToLogs: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChannelForLogs by remember { mutableStateOf<String?>(null) }

    val mockChannels = remember {
        listOf(
            OTAChannelUiModel("viator", "Viator / TripAdvisor", true, "10 dk önce", 0, 142, 12),
            OTAChannelUiModel("getyourguide", "GetYourGuide", true, "15 dk önce", 1, 98, 8),
            OTAChannelUiModel("hotelbeds", "HotelBeds", false, "Henüz bağlanmadı", 0, 0, 0),
            OTAChannelUiModel("booking", "Booking.com", true, "5 dk önce", 0, 210, 24),
            OTAChannelUiModel("expedia", "Expedia Partner Central", false, "Bağlantı Kesildi", 2, 45, 6)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OTA Entegrasyon Yönetim Paneli", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { viewModel.loadBookings(tenantId = tenantId) }) {
                        Text("Yenile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Özet Metrik Kartları
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = "Toplam Rezervasyon",
                        value = "495",
                        badge = "REZ",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Aktif Kanallar",
                        value = "3 / 5",
                        badge = "AKTİF",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Başarısız Job",
                        value = "3",
                        badge = "HATA",
                        modifier = Modifier.weight(1f),
                        isError = true
                    )
                }
            }

            item {
                Text(
                    text = "OTA Kanal Bağlantıları & Durum",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Kanal Listesi
            items(mockChannels) { channel ->
                OTAChannelCard(
                    channel = channel,
                    onConnect = {
                        viewModel.connectChannel(
                            OTAAccount(accountId = channel.providerId, providerId = channel.providerId, accountName = channel.providerName, apiKey = "key-123"),
                            tenantId
                        )
                    },
                    onDisconnect = {
                        viewModel.syncNow(channel.providerId, false, tenantId)
                    },
                    onSync = {
                        viewModel.syncNow(channel.providerId, true, tenantId)
                    },
                    onViewLogs = {
                        selectedChannelForLogs = channel.providerId
                        onNavigateToLogs(channel.providerId)
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OTAChannelCard(
    channel: OTAChannelUiModel,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onViewLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (channel.isConnected) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = channel.providerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (channel.isConnected) "BAĞLI" else "DEVRE DIŞI",
                            color = if (channel.isConnected) Color(0xFF2E7D32) else Color.DarkGray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Son Sync: ${channel.lastSyncedAt}", fontSize = 13.sp, color = Color.Gray)
                Text(text = "Başarısız Job: ${channel.failedJobCount}", fontSize = 13.sp, color = if (channel.failedJobCount > 0) Color.Red else Color.Gray)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Rezervasyon: ${channel.bookingCount}", fontSize = 13.sp)
                Text(text = "Eşleşen Ürün: ${channel.productCount}", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buton Grubu: Connect, Disconnect, Manuel Sync, Logları Görüntüle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (channel.isConnected) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Disconnect", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onSync,
                    enabled = channel.isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Manuel Sync", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onViewLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Loglar", fontSize = 11.sp)
                }
            }
        }
    }
}
