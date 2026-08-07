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
import com.mgacreative.touros.domain.model.NotificationChannel
import com.mgacreative.touros.domain.model.NotificationResult
import com.mgacreative.touros.ui.viewmodel.NotificationHubViewModel

/**
 * 3.4.4 NotificationService Çoklu Kanal Bildirim Ekranı (Push, SMS, WhatsApp, E-Posta).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHubScreen(
    viewModel: NotificationHubViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var recipient by remember { mutableStateOf("+905329998877") }
    var title by remember { mutableStateOf("Kapadokya Turu Kalkış Bilgilendirmesi") }
    var content by remember { mutableStateOf("Sayın Misafirimiz, turunuz yarın saat 08:30'da kalkacaktır.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔔 Çoklu Kanal Bildirim Merkezi", fontWeight = FontWeight.Bold) },
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
            // 1. Kanal Seçim Çipleri
            Text("📡 İletişim Kanalı Seçimi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = state.selectedChannel == NotificationChannel.PUSH,
                    onClick = { viewModel.setSelectedChannel(NotificationChannel.PUSH) },
                    label = { Text("📲 Push (FCM)", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = state.selectedChannel == NotificationChannel.WHATSAPP,
                    onClick = { viewModel.setSelectedChannel(NotificationChannel.WHATSAPP) },
                    label = { Text("💬 WhatsApp", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = state.selectedChannel == NotificationChannel.SMS,
                    onClick = { viewModel.setSelectedChannel(NotificationChannel.SMS) },
                    label = { Text("📱 SMS", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = state.selectedChannel == NotificationChannel.EMAIL,
                    onClick = { viewModel.setSelectedChannel(NotificationChannel.EMAIL) },
                    label = { Text("📧 E-Posta", fontSize = 10.sp) }
                )
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Bildirim Gönderim Formu
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🚀 Anlık Bildirim Gönder (${state.selectedChannel.name})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text(if (state.selectedChannel == NotificationChannel.EMAIL) "Alıcı E-Posta Adresi" else "Alıcı Telefon No / User ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bildirim Başlığı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Mesaj İçeriği") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )

                    Button(
                        onClick = {
                            if (recipient.isNotBlank() && content.isNotBlank()) {
                                viewModel.sendNotification(recipient, title, content)
                            }
                        },
                        enabled = recipient.isNotBlank() && content.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🚀 Bildirimi ${state.selectedChannel.name} Üzerinden Gönder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Gönderilen Bildirim Geçmiş Günlüğü
            Text("📜 Bildirim Günlüğü (${state.dispatchHistory.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.dispatchHistory) { log ->
                        NotificationLogCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationLogCard(log: NotificationResult) {
    val (icon, color) = when (log.channel) {
        NotificationChannel.PUSH -> "📲" to MaterialTheme.colorScheme.primary
        NotificationChannel.WHATSAPP -> "💬" to Color(0xFF16A34A)
        NotificationChannel.SMS -> "📱" to Color(0xFFEA580C)
        NotificationChannel.EMAIL -> "📧" to Color(0xFF2563EB)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(icon, fontSize = 18.sp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(log.title ?: "Bildirim", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Alıcı: ${log.recipient} | ${log.content}", fontSize = 10.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${log.createdAt} | Provider: ${log.provider}", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                Text(log.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
