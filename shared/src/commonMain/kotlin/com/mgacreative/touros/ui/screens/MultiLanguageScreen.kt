package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.viewmodel.MultiLanguageViewModel

/**
 * 4.4.1 Çoklu Dil (TR, EN, DE, RU, AR, FR) ve Arapça RTL Düzeni Test Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiLanguageScreen(
    viewModel: MultiLanguageViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val lang = state.selectedLanguage
    val isRtl = lang.isRtl

    // Arapça için RTL LayoutDirection
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌍 Çoklu Dil & RTL Desteği", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 1. Dil Seçim Paneli (6 Dil)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🌐 Desteklenen Diller (6 Dil)", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.supportedLanguages.chunked(2).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { item ->
                                    FilterChip(
                                        selected = lang.code == item.code,
                                        onClick = { viewModel.selectLanguage(item) },
                                        label = { Text("${item.flagEmoji} ${item.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Dynamic RTL / LTR Composition Wrapper Preview Box
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${lang.flagEmoji} ${lang.name} Önizleme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val rtlBadgeColor = if (isRtl) Color(0xFFD97706) else Color(0xFF2563EB)
                            val rtlBadgeText = if (isRtl) "🇸🇦 RTL SAĞDAN SOLA DÜZEN" else "LTR SOLDAN SAĞA DÜZEN"
                            Surface(shape = RoundedCornerShape(6.dp), color = rtlBadgeColor.copy(alpha = 0.15f)) {
                                Text(rtlBadgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = rtlBadgeColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                state.translations["welcome_title"] ?: "TourOS Seyahat Sistemine Hoş Geldiniz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(state.translations["search_tours"] ?: "Tur Ara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(state.translations["checkout"] ?: "Ödemeye Geç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
