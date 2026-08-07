package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.B2CPushNotificationItem
import com.mgacreative.touros.ui.viewmodel.B2CNotificationsReviewViewModel

/**
 * 4.2.6 B2C Müşteri Push Bildirimleri ve Tur Değerlendirme Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2CNotificationsReviewScreen(
    viewModel: B2CNotificationsReviewViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var commentInput by remember { mutableStateOf("Rehberimiz Mehmet Bey harikaydı, balon turu organizasyonu kusursuzdu! Teşekkürler.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔔 Bildirimler & ⭐ Değerlendirme", fontWeight = FontWeight.Bold) },
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
                    Text("1. 🔔 Bildirimlerim (${state.unreadCount})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. ⭐ Tur Değerlendir", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Push Bildirimleri Listesi
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.notifications) { notif ->
                            B2CPushNotificationCard(notif = notif)
                        }
                    }
                }
            } else {
                // TAB 1: Tur Değerlendirme Formu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🎉 Tamamlanan Turunuzu Değerlendirin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                Text(state.selectedTourTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(10.dp))
                            }

                            // Star Rating Selector Bar
                            Text("Puanınız (${state.rating.toInt()} / 5 Yıldız):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..5).forEach { star ->
                                    val isSelected = star <= state.rating.toInt()
                                    OutlinedButton(
                                        onClick = { viewModel.updateRating(star.toDouble()) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF08A)) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text(if (isSelected) "★ $star" else "☆ $star", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF854D0E) else Color.Gray)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = commentInput,
                                onValueChange = { commentInput = it },
                                label = { Text("Tur Deneyiminiz ve Yorumunuz") },
                                modifier = Modifier.fillMaxWidth().height(100.dp)
                            )

                            Button(
                                onClick = { viewModel.submitReview(commentInput) },
                                enabled = !state.isLoading && commentInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                else Text("🌟 Değerlendirmeyi Gönder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun B2CPushNotificationCard(notif: B2CPushNotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Text(if (notif.category == "REMINDER") "🎈" else "🔥", fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (!notif.isRead) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF2563EB)) {
                            Text("YENİ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Text(notif.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(notif.createdAt, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}
