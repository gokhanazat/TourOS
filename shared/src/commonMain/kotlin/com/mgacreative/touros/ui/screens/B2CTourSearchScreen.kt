package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.mgacreative.touros.domain.model.B2CTourItem
import com.mgacreative.touros.ui.viewmodel.B2CTourSearchViewModel

/**
 * 4.2.1 B2C Müşteri Mobil Uygulaması Tur Arama ve Filtreleme Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2CTourSearchScreen(
    viewModel: B2CTourSearchViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var minPriceInput by remember { mutableStateOf("") }
    var maxPriceInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Tur Bul & Keşfet", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Arama Çubuğu
            OutlinedTextField(
                value = state.filter.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Nereye gitmek istersiniz? (Örn: Kapadokya, Roma)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Kategori Filtre Çipleri
            Text("🏷️ Kategori Seçimi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                items(state.availableCategories) { cat ->
                    val isSelected = (cat == "Tümü" && state.filter.category == null) || state.filter.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            // 3. Ülke Filtre Çipleri
            Text("🌍 Ülke Filtresi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                items(state.availableCountries) { country ->
                    val isSelected = (country == "Tümü" && state.filter.country == null) || state.filter.country == country
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCountry(country) },
                        label = { Text(country, fontSize = 11.sp) }
                    )
                }
            }

            // 4. Fiyat Aralığı Filtresi
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minPriceInput,
                    onValueChange = {
                        minPriceInput = it
                        viewModel.updatePriceRange(it.toDoubleOrNull(), maxPriceInput.toDoubleOrNull())
                    },
                    label = { Text("Min TL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxPriceInput,
                    onValueChange = {
                        maxPriceInput = it
                        viewModel.updatePriceRange(minPriceInput.toDoubleOrNull(), it.toDoubleOrNull())
                    },
                    label = { Text("Max TL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. Tur Listesi
            Text("🎉 Bulunan Turlar (${state.tours.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.tours) { tour ->
                        B2CTourCard(tour = tour, onClick = { onNavigateToDetail(tour.tourId) })
                    }
                }
            }
        }
    }
}

@Composable
fun B2CTourCard(tour: B2CTourItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(tour.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }

                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF08A)) {
                    Text("⭐ ${tour.rating} (${tour.reviewCount})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Text(tour.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📍 ${tour.destinationCountry} | ${tour.durationDays} Gün", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🗓️ Kalkış: ${tour.nextDepartureDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kişi Başı Başlangıç", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${tour.price} ${tour.currency}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("İncele >", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
