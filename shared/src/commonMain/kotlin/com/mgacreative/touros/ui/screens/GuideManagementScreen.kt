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
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.ui.viewmodel.GuideFormState
import com.mgacreative.touros.ui.viewmodel.GuideManagementViewModel
import com.mgacreative.touros.ui.viewmodel.GuideUiState

/**
 * 2.5.1 Rehber Veri Modeli ve CRUD Yönetim Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideManagementScreen(
    viewModel: GuideManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚩 Rehber Yönetimi & Portföyü", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!formState.isFormOpen) {
                FloatingActionButton(
                    onClick = { viewModel.openNewForm() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("+ Rehber Ekle", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
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
                is GuideUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GuideUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is GuideUiState.Success -> {
                    if (formState.isFormOpen) {
                        // Rehber Ekleme / Düzenleme Formu
                        GuideFormCard(
                            formState = formState,
                            onFullNameChange = { viewModel.updateFullName(it) },
                            onPhoneChange = { viewModel.updatePhone(it) },
                            onEmailChange = { viewModel.updateEmail(it) },
                            onLicenseNumberChange = { viewModel.updateLicenseNumber(it) },
                            onLanguagesCsvChange = { viewModel.updateLanguagesCsv(it) },
                            onSpecializationChange = { viewModel.updateSpecialization(it) },
                            onRatingChange = { viewModel.updateRating(it) },
                            onTotalToursChange = { viewModel.updateTotalTours(it) },
                            onNotesChange = { viewModel.updateNotes(it) },
                            onIsActiveChange = { viewModel.updateIsActive(it) },
                            onSave = { viewModel.saveGuide() },
                            onCancel = { viewModel.closeForm() }
                        )
                    } else {
                        // Arama Çubuğu
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.loadGuides(it, state.selectedLanguageFilter) },
                            placeholder = { Text("Rehber Adı veya Kokart No Ara...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Dil Filtre Çipleri
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedLanguageFilter == null,
                                onClick = { viewModel.loadGuides(state.searchQuery, null) },
                                label = { Text("Tüm Diller", fontSize = 11.sp) }
                            )
                            listOf("İngilizce", "Almanca", "Fransızca", "İspanyolca").forEach { lang ->
                                FilterChip(
                                    selected = state.selectedLanguageFilter == lang,
                                    onClick = { viewModel.loadGuides(state.searchQuery, lang) },
                                    label = { Text(lang, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (state.guides.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Arama kriterlerine uygun rehber bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(state.guides) { guide ->
                                    GuideItemCard(
                                        guide = guide,
                                        onEditClick = { viewModel.openEditForm(guide) },
                                        onDeleteClick = { viewModel.deleteGuide(guide.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideItemCard(
    guide: Guide,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Kokart: ${guide.licenseNumber ?: "Lisanssız"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Puan Rozeti
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF08A)
                    ) {
                        Text(
                            text = "⭐ ${guide.rating}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF854D0E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Tur Geçmişi Rozeti
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "🚩 ${guide.totalToursCompleted} Tur",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column {
                Text(guide.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "🎯 Uzmanlık: ${guide.specialization ?: "Genel Kültür"} | 📞 ${guide.phone ?: "-"} | ✉️ ${guide.email ?: "-"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bildiği Diller Çipleri
            if (!guide.languages.isNullOrEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🗣️ Diller:", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    guide.languages.forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(lang, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            if (!guide.notes.isNullOrBlank()) {
                Text("📝 ${guide.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDeleteClick) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onEditClick, shape = RoundedCornerShape(8.dp)) {
                    Text("Düzenle", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GuideFormCard(
    formState: GuideFormState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onLicenseNumberChange: (String) -> Unit,
    onLanguagesCsvChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onTotalToursChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (formState.isEditing) "✏️ Rehber Bilgilerini Düzenle" else "➕ Yeni Rehber Kaydı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancel) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            OutlinedTextField(
                value = formState.fullName,
                onValueChange = onFullNameChange,
                label = { Text("Rehber Adı Soyadı") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Telefon") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.email,
                    onValueChange = onEmailChange,
                    label = { Text("E-Posta") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.licenseNumber,
                    onValueChange = onLicenseNumberChange,
                    label = { Text("Kokart / Lisans No (Örn: K-12345)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.specialization,
                    onValueChange = onSpecializationChange,
                    label = { Text("Uzmanlık Alanı (Örn: Kapadokya)") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = formState.languagesCsv,
                onValueChange = onLanguagesCsvChange,
                label = { Text("Bildiği Diller (Virgülle: Türkçe, İngilizce, Almanca)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.rating,
                    onValueChange = onRatingChange,
                    label = { Text("Puan (Örn: 4.9)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.totalToursCompleted,
                    onValueChange = onTotalToursChange,
                    label = { Text("Tamamlanan Tur Sayısı") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = formState.notes,
                onValueChange = onNotesChange,
                label = { Text("Rehber Notları & Lisans Detayları") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formState.isActive, onCheckedChange = onIsActiveChange)
                Text("Rehberi Aktif Kadroda Göster", fontSize = 12.sp)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.fullName.isNotBlank()
            ) {
                Text("💾 Rehber Kaydını Oluştur / Güncelle")
            }
        }
    }
}
