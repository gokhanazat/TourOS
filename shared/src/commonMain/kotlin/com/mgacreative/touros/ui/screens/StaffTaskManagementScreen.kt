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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.StaffTaskItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.StaffTaskManagementViewModel

/**
 * Görev Yönetimi (Kanban / Takvim) Ekranı — TourOS 0.3
 *
 * Kanban benzeri 3 durumlu liste (Yapılacak / Devam Ediyor / Tamamlandı).
 * Görev kartları küçük ve kompakt.
 * Öncelik rengiyle değil METİNLE etiketli ("Öncelik: Yüksek", "Öncelik: Orta", "Öncelik: Düşük", "Öncelik: Acil").
 */
@Composable
fun StaffTaskManagementScreen(
    viewModel: StaffTaskManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedKanbanTab by remember { mutableStateOf(0) } // 0: Yapılacak, 1: Devam Ediyor, 2: Tamamlandı

    val todoTasks = remember(state.tasks) { state.tasks.filter { it.status == "TODO" || it.status == "OPEN" } }
    val inProgressTasks = remember(state.tasks) { state.tasks.filter { it.status == "IN_PROGRESS" } }
    val doneTasks = remember(state.tasks) { state.tasks.filter { it.status == "DONE" || it.status == "COMPLETED" } }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Görev Yönetimi (Kanban)",
                subtitle = "Personel görev takibi ve takvim entegrasyonu",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    TourOSButton(
                        text = "+ Yeni Görev",
                        onClick = { showAddTaskDialog = true },
                        variant = TourOSButtonVariant.PRIMARY,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large)
        ) {
            val isExpanded = maxWidth >= 768.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── Takvim Senkronizasyon Durum Kartı ─────────────────────────
                CalendarSyncStatusBanner()

                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }

                if (isExpanded) {
                    // ── MASAÜSTÜ / TABLET: 3 KOLONLU KANBAN PANOSU (YAN YANA) ──────
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // Kolon 1: Yapılacak (To-Do)
                        KanbanColumnPanel(
                            title = "📌 Yapılacak (${todoTasks.size})",
                            tasks = todoTasks,
                            columnBg = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        )

                        // Kolon 2: Devam Ediyor (In-Progress)
                        KanbanColumnPanel(
                            title = "🔄 Devam Ediyor (${inProgressTasks.size})",
                            tasks = inProgressTasks,
                            columnBg = TourOSColors.SecondaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        )

                        // Kolon 3: Tamamlandı (Done)
                        KanbanColumnPanel(
                            title = "✅ Tamamlandı (${doneTasks.size})",
                            tasks = doneTasks,
                            columnBg = TourOSColors.SuccessContainer.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // ── MOBİL: SEKMELİ KANBAN DÜZENİ ───────────────────────────
                    PrimaryTabRow(
                        selectedTabIndex = selectedKanbanTab,
                        containerColor = TourOSColors.Background,
                        contentColor = TourOSColors.Primary
                    ) {
                        Tab(
                            selected = selectedKanbanTab == 0,
                            onClick = { selectedKanbanTab = 0 },
                            text = { Text("📌 Yapılacak (${todoTasks.size})", style = TourOSTypography.Label) }
                        )
                        Tab(
                            selected = selectedKanbanTab == 1,
                            onClick = { selectedKanbanTab = 1 },
                            text = { Text("🔄 Devam Ediyor (${inProgressTasks.size})", style = TourOSTypography.Label) }
                        )
                        Tab(
                            selected = selectedKanbanTab == 2,
                            onClick = { selectedKanbanTab = 2 },
                            text = { Text("✅ Tamamlandı (${doneTasks.size})", style = TourOSTypography.Label) }
                        )
                    }

                    val activeTabTasks = when (selectedKanbanTab) {
                        0 -> todoTasks
                        1 -> inProgressTasks
                        else -> doneTasks
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        items(activeTabTasks) { task ->
                            SmallTaskCard(task = task)
                        }
                    }
                }
            }

            // Yeni Görev Ekleme Modal Formu
            if (showAddTaskDialog) {
                CreateTaskDialog(
                    onDismiss = { showAddTaskDialog = false },
                    onSave = { titleVal: String, assignedVal: String, priorityVal: String, dueDateVal: String ->
                        viewModel.addTask(titleVal, assignedVal, priorityVal, dueDateVal)
                        showAddTaskDialog = false
                    }
                )
            }
        }
    }
}

// ─── Takvim Senkronizasyonu Üst Banner ───────────────────────────────────────

@Composable
private fun CalendarSyncStatusBanner() {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅", style = TourOSTypography.TitleLarge)
                Column {
                    Text(
                        "Google Calendar & iCal Otomatik Senkronizasyon",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                    )
                    Text(
                        "Personel görev zamanlamaları canlı takvimde güncellenir.",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            TourOSStatusBadge(
                text = "🟢 SENKRON",
                backgroundColor = TourOSColors.SuccessContainer,
                textColor = TourOSColors.Success
            )
        }
    }
}

// ─── Kanban Kolon Paneli (Masaüstü/Tablet) ───────────────────────────────────

@Composable
private fun KanbanColumnPanel(
    title: String,
    tasks: List<StaffTaskItem>,
    columnBg: Color,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier.fillMaxHeight(),
        backgroundColor = columnBg,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Text(
                title,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Görev yok",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(tasks) { task ->
                        SmallTaskCard(task = task)
                    }
                }
            }
        }
    }
}

// ─── KÜÇÜK VE METİNLE ETİKETLİ ÖNCELİKLİ GÖREV KARTI ─────────────────────────

@Composable
private fun SmallTaskCard(task: StaffTaskItem) {
    // METİN İLE ETİKETLİ ÖNCELİK (Öncelik rengiyle değil METİNLE etiketlenir)
    val priorityTextLabel = when (task.priority) {
        "URGENT" -> "Öncelik: Acil"
        "HIGH" -> "Öncelik: Yüksek"
        "MEDIUM" -> "Öncelik: Orta"
        else -> "Öncelik: Düşük"
    }

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Küçük ve Kompakt Görev Başlığı
                Text(
                    task.title,
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // ÖNCELİK RENGİYLE DEĞİL METİNLE ETİKETLİ (Strict Rule)
                TourOSStatusBadge(
                    text = priorityTextLabel,
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                    maxLines = 2
                )
            }

            HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👤 ${task.assignedTo}  ·  ⏳ ${task.dueDate}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                TourOSButton(
                    text = "✅ Tamamla",
                    onClick = {},
                    variant = TourOSButtonVariant.TERTIARY
                )
            }
        }
    }
}

// ─── Modal Form: Yeni Görev Oluşturma ─────────────────────────────────────────

@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, assignedTo: String, priority: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDate by remember { mutableStateOf("2026-08-08 17:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "➕ Yeni Personel Görevi Ekle",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Görev Başlığı",
                    placeholder = "Örn: Otel Konfirmasyonlarını Al",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = "Sorumlu Personel",
                    placeholder = "Örn: Mehmet Can",
                    modifier = Modifier.fillMaxWidth()
                )

                // Öncelik Metin Seçimi
                Text("Öncelik Metin Derecesi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = priority == "URGENT", onClick = { priority = "URGENT" }, label = { Text("Öncelik: Acil", style = TourOSTypography.Caption) })
                    FilterChip(selected = priority == "HIGH", onClick = { priority = "HIGH" }, label = { Text("Öncelik: Yüksek", style = TourOSTypography.Caption) })
                    FilterChip(selected = priority == "MEDIUM", onClick = { priority = "MEDIUM" }, label = { Text("Öncelik: Orta", style = TourOSTypography.Caption) })
                }

                TourOSTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "Son Tamamlama Tarihi & Saat",
                    placeholder = "2026-08-08 17:00",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TourOSButton(
                text = "💾 Görevi Kaydet",
                onClick = { onSave(title, assignedTo, priority, dueDate) },
                enabled = title.isNotBlank() && assignedTo.isNotBlank(),
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TourOSButton(
                text = "İptal",
                onClick = onDismiss,
                variant = TourOSButtonVariant.SECONDARY
            )
        }
    )
}
