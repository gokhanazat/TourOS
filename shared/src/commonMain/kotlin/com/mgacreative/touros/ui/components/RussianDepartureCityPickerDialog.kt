package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography

data class RussianDepartureCity(
    val id: String,
    val nameRu: String,
    val nameTr: String,
    val nameEn: String,
    val airportCode: String,
    val airportNameRu: String,
    val isPopular: Boolean = false
)

private val defaultRussianDepartureCities = listOf(
    RussianDepartureCity("ru_mow", "Москва (Все аэропорты)", "Moskova (Tüm Havalimanları)", "Moscow (All Airports)", "MOW", "Шереметьево, Домодедово, Внуково", true),
    RussianDepartureCity("ru_svo", "Москва (Шереметьево)", "Moskova (Şeremetyevo)", "Moscow (Sheremetyevo)", "SVO", "Международный аэропорт Шереметьево", true),
    RussianDepartureCity("ru_vko", "Москва (Внуково)", "Moskova (Vnukovo)", "Moscow (Vnukovo)", "VKO", "Международный аэропорт Внуково", true),
    RussianDepartureCity("ru_dme", "Москва (Домодедово)", "Moskova (Domodedovo)", "Moscow (Domodedovo)", "DME", "Московский аэропорт Домодедово", true),
    RussianDepartureCity("ru_led", "Санкт-Петербург", "St. Petersburg", "St. Petersburg", "LED", "Аэропорт Пулково", true),
    RussianDepartureCity("ru_kzn", "Казань", "Kazan", "Kazan", "KZN", "Международный аэропорт Казань", true),
    RussianDepartureCity("ru_svx", "Екатеринбург", "Yekaterinburg", "Yekaterinburg", "SVX", "Международный аэропорт Кольцово", true),
    RussianDepartureCity("ru_ovb", "Новосибирск", "Novosibirsk", "Novosibirsk", "OVB", "Международный аэропорт Толмачёво", true),
    RussianDepartureCity("ru_kuf", "Самара", "Samara", "Samara", "KUF", "Международный аэропорт Курумоч", true),
    RussianDepartureCity("ru_ufa", "Уфа", "Ufa", "Ufa", "UFA", "Международный аэропорт Уфа", true),
    RussianDepartureCity("ru_aer", "Сочи (Адлер)", "Sochi (Adler)", "Sochi (Adler)", "AER", "Международный аэропорт Сочи", true),
    RussianDepartureCity("ru_mrv", "Минеральные Воды", "Mineralnye Vody", "Mineralnye Vody", "MRV", "Международный аэропорт Минеральные Воды", true),
    RussianDepartureCity("ru_goj", "Нижний Новгород", "Nizhny Novgorod", "Nizhny Novgorod", "GOJ", "Международный аэропорт Чкалов", false),
    RussianDepartureCity("ru_cek", "Челябинск", "Chelyabinsk", "Chelyabinsk", "CEK", "Международный аэропорт Баландино", false),
    RussianDepartureCity("ru_kja", "Красноярск", "Krasnoyarsk", "Krasnoyarsk", "KJA", "Международный аэропорт Емельяново", false),
    RussianDepartureCity("ru_pee", "Пермь", "Perm", "Perm", "PEE", "Международный аэропорт Большое Савино", false),
    RussianDepartureCity("ru_vog", "Волгоград", "Volgograd", "Volgograd", "VOG", "Международный аэропорт Гумрак", false),
    RussianDepartureCity("ru_oms", "Омск", "Omsk", "Omsk", "OMS", "Омск-Центральный", false),
    RussianDepartureCity("ru_tjm", "Тюмень", "Tyumen", "Tyumen", "TJM", "Международный аэропорт Рощино", false),
    RussianDepartureCity("ru_ikt", "Иркутск", "Irkutsk", "Irkutsk", "IKT", "Международный аэропорт Иркутск", false),
    RussianDepartureCity("ru_kgd", "Калининград", "Kaliningrad", "Kaliningrad", "KGD", "Международный аэропорт Храброво", false)
)

/**
 * Rusya Kalkış Şehirleri Seçim Modalı (Kiril ve Latin Harf Duyarlı Canlı Arama).
 */
@Composable
fun RussianDepartureCityPickerDialog(
    currentSelection: String = "",
    onCitySelected: (RussianDepartureCity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            defaultRussianDepartureCities
        } else {
            val q = searchQuery.trim().lowercase()
            defaultRussianDepartureCities.filter {
                it.nameRu.lowercase().contains(q) ||
                it.nameTr.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.airportCode.lowercase().contains(q) ||
                it.airportNameRu.lowercase().contains(q)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 360.dp, max = 820.dp)
                .fillMaxWidth(0.92f)
                .heightIn(min = 480.dp, max = 650.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "🇷🇺 ГОРОД ВЫЛЕТА / KALKIŞ ŞEHRİ",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Text(
                            text = "Откуда вы летите? (Rusya Kalkış Noktası)",
                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", fontSize = 14.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }
                }

                // Canlı Arama Input'u (Kiril & Latin)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Поиск: Москва, Казань, SVO, LED, СПБ...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F5A56),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                // Hızlı Popüler Şehir Çipleri
                if (searchQuery.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "⭐ Популярные города (Popüler Kalkışlar):",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(defaultRussianDepartureCities.filter { it.isPopular }.take(6)) { popCity ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .clickable {
                                            onCitySelected(popCity)
                                            onDismiss()
                                        },
                                    color = Color(0xFFF8FAFC)
                                ) {
                                    Text(
                                        text = popCity.nameRu.substringBefore(" ("),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F5A56))
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Şehir Listesi
                if (filteredCities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Город не найден / Eşleşen Rusya kalkış şehri bulunamadı.", style = TourOSTypography.BodyMedium, color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCities, key = { it.id }) { city ->
                            val isSelected = currentSelection.contains(city.nameRu, ignoreCase = true) || currentSelection.contains(city.nameTr, ignoreCase = true)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFE6F4F1) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCitySelected(city)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛫", fontSize = 18.sp)
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "${city.nameRu} (${city.nameTr})",
                                                style = TourOSTypography.BodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 14.sp
                                                )
                                            )
                                            Text(
                                                text = city.airportNameRu,
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEFF6FF)
                                    ) {
                                        Text(
                                            text = city.airportCode,
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
}
