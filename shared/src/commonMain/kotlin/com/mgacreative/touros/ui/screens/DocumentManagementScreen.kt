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
import com.mgacreative.touros.domain.model.DocumentItem
import com.mgacreative.touros.ui.viewmodel.DocumentManagementViewModel

/**
 * 3.4.1 Belge Yükleme ve Saklama Ekranı (Pasaport, Vize, Sözleşme, Voucher, PDF, Fotoğraf).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManagementScreen(
    viewModel: DocumentManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var docTitle by remember { mutableStateOf("") }
    var selectedUploadType by remember { mutableStateOf("passport") }

    val categories = listOf(
        "all" to "Tümü",
        "passport" to "📕 Pasaport",
        "visa" to "🛂 Vize",
        "contract" to "📝 Sözleşme",
        "voucher" to "🎟️ Voucher",
        "pdf" to "📄 PDF",
        "photo" to "🖼️ Fotoğraf"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📁 Belge Saklama & Supabase RLS", fontWeight = FontWeight.Bold) },
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
            // 1. Kategorik Filtre Çipleri
            ScrollableTabRow(selectedTabIndex = categories.indexOfFirst { it.first == state.selectedCategory }.coerceAtLeast(0), edgePadding = 0.dp) {
                categories.forEach { (catKey, catLabel) ->
                    Tab(
                        selected = state.selectedCategory == catKey,
                        onClick = { viewModel.loadDocuments(catKey) },
                        text = { Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Yeni Belge Yükleme Formu Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📤 Yeni Belge Yükle (Supabase Storage RLS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Belge Adı / Başlığı (Örn: Hans Pasaport)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Belge Türü:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedUploadType == "passport",
                            onClick = { selectedUploadType = "passport" },
                            label = { Text("📕 Pasaport", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedUploadType == "visa",
                            onClick = { selectedUploadType = "visa" },
                            label = { Text("🛂 Vize", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedUploadType == "voucher",
                            onClick = { selectedUploadType = "voucher" },
                            label = { Text("🎟️ Voucher", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedUploadType == "contract",
                            onClick = { selectedUploadType = "contract" },
                            label = { Text("📝 Sözleşme", fontSize = 10.sp) }
                        )
                    }

                    Button(
                        onClick = {
                            if (docTitle.isNotBlank()) {
                                viewModel.uploadSampleDocument(docTitle, selectedUploadType)
                                docTitle = ""
                            }
                        },
                        enabled = docTitle.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📤 Belgeyi Supabase Storage'a Yükle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Yüklü Belgeler Listesi
            Text("📋 Yüklü Belgeler (${state.documents.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.documents) { doc ->
                        DocumentCard(document = doc)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentCard(document: DocumentItem) {
    val (typeIcon, typeLabel) = when (document.documentType) {
        "passport" -> "📕" to "PASAPORT"
        "visa" -> "🛂" to "VİZE"
        "contract" -> "📝" to "SÖZLEŞME"
        "voucher" -> "🎟️" to "VOUCHER"
        "photo" -> "🖼️" to "FOTOĞRAF"
        else -> "📄" to "PDF/BELGE"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(typeIcon, fontSize = 18.sp)
                    Text(document.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(typeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Text("📁 Storage Yolu: ${document.filePath}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val sizeKb = document.fileSize / 1024
                Text("Boyut: ${sizeKb} KB | Tarih: ${document.createdAt}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("👁️ Görüntüle", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
