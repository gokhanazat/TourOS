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
import com.mgacreative.touros.domain.model.StaffTaskItem
import com.mgacreative.touros.ui.viewmodel.StaffTaskManagementViewModel

/**
 * 3.4.3 Personel Görev Yönetimi, Hatırlatma ve Takvim Entegrasyonu Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTaskManagementScreen(
    viewModel: StaffTaskManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var taskTitle by remember { mutableStateOf("") }
    var assignedPerson by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDate by remember { mutableStateOf("2026-08-07 15:00") }

    val filteredTasks = remember(state.tasks, state.selectedPriority) {
        if (state.selectedPriority == "ALL") state.tasks
        else state.tasks.filter { it.priority == state.selectedPriority }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Görev & Takvim Yönetimi", fontWeight = FontWeight.Bold) },
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
            // 1. Takvim Senkronizasyon Durum Kartı
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 20.sp)
                        Column {
                            Text("Google Calendar / iCal Takvim Senkronizasyonu", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Otomatik Hatırlatmalar: Aktif (30 Dk Önce)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF15803D)) {
                        Text("SENKRON", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Yeni Görev Oluşturma Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("➕ Yeni Personel Görevi & Hatırlatma Ekle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Görev Başlığı") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = assignedPerson,
                            onValueChange = { assignedPerson = it },
                            label = { Text("Atanan Personel") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = priority == "URGENT",
                            onClick = { priority = "URGENT" },
                            label = { Text("🚨 Acil", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = priority == "HIGH",
                            onClick = { priority = "HIGH" },
                            label = { Text("🔥 Yüksek", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = priority == "MEDIUM",
                            onClick = { priority = "MEDIUM" },
                            label = { Text("📌 Normal", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = priority == "LOW",
                            onClick = { priority = "LOW" },
                            label = { Text("🟢 Düşük", fontSize = 10.sp) }
                        )
                    }

                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank() && assignedPerson.isNotBlank()) {
                                viewModel.addTask(taskTitle, assignedPerson, priority, dueDate)
                                taskTitle = ""
                                assignedPerson = ""
                            }
                        },
                        enabled = taskTitle.isNotBlank() && assignedPerson.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("➕ Görev Oluştur & Takvime Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Öncelik Filtre Çipleri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL" to "Tümü", "URGENT" to "🚨 Acil", "HIGH" to "🔥 Yüksek", "MEDIUM" to "📌 Normal").forEach { (pKey, pLabel) ->
                    FilterChip(
                        selected = state.selectedPriority == pKey,
                        onClick = { viewModel.setPriorityFilter(pKey) },
                        label = { Text(pLabel, fontSize = 10.sp) }
                    )
                }
            }

            // 4. Görev Listesi
            Text("📋 Aktif Personel Görevleri (${filteredTasks.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredTasks) { task ->
                        StaffTaskCard(task = task)
                    }
                }
            }
        }
    }
}

@Composable
fun StaffTaskCard(task: StaffTaskItem) {
    val (priorityColor, priorityText) = when (task.priority) {
        "URGENT" -> MaterialTheme.colorScheme.error to "🚨 ACİL"
        "HIGH" -> Color(0xFFEA580C) to "🔥 YÜKSEK"
        "MEDIUM" -> MaterialTheme.colorScheme.primary to "📌 NORMAL"
        else -> Color(0xFF15803D) to "🟢 DÜŞÜK"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Surface(shape = RoundedCornerShape(6.dp), color = priorityColor.copy(alpha = 0.15f)) {
                    Text(priorityText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = priorityColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text(task.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("👤 Sorumlu: ${task.assignedTo} | ⏳ Son Tarih: ${task.dueDate}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDBEAFE)) {
                        Text("⏰ ${task.reminderMinutesBefore} Dk Önce Hatırlat", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (task.calendarEventId != null) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDCFCE7)) {
                            Text("📅 Takvim Synced", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Button(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("✅ Tamamla", fontSize = 10.sp)
                }
            }
        }
    }
}
