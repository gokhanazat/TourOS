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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.viewmodel.HotelFormViewModel

/**
 * 2.3.1 Otel Kayıt / Düzenleme Formu Screen (Ad, Konum, Yıldız, Açıklama, Fotoğraf).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelFormScreen(
    viewModel: HotelFormViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.hotelId.isNullOrBlank()) "🏨 Yeni Otel Kaydı" else "🏨 Otel Bilgilerini Düzenle", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Temel Otel Bilgileri
            Text("🏨 Temel Bilgiler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Otel Adı *") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = { viewModel.updateCity(it) },
                    label = { Text("Şehir *") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = uiState.country,
                    onValueChange = { viewModel.updateCountry(it) },
                    label = { Text("Ülke") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Yıldız Seçimi (Star Rating 1-5)
            Column {
                Text("Otel Yıldız Derecesi: ${uiState.starRating} Yıldız ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { rating ->
                        FilterChip(
                            selected = uiState.starRating == rating,
                            onClick = { viewModel.updateStarRating(rating) },
                            label = { Text("$rating ⭐") }
                        )
                    }
                }
            }

            // 2. Açıklama & Fotoğraf URL
            HorizontalDivider()
            Text("🖼️ Görsel ve Açıklama", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = uiState.coverImageUrl,
                onValueChange = { viewModel.updateCoverImageUrl(it) },
                label = { Text("Kapak Fotoğrafı Görsel URL (Unsplash/CDN)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Otel Tanıtım Açıklaması") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            // 3. İletişim Bilgileri
            HorizontalDivider()
            Text("📞 İletişim & Adres Bilgileri", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("Açık Adres") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.updatePhone(it) },
                    label = { Text("Telefon") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("E-posta") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isSavedSuccess) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "✅ Otel kaydı başarıyla oluşturuldu / güncellendi!",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = "Hata: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { viewModel.saveHotel() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading && uiState.name.isNotBlank() && uiState.city.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("💾 Otel Kaydını Tamamla")
                }
            }
        }
    }
}
