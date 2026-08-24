package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mgacreative.touros.ui.theme.TourOSTypography

enum class DestinationLevel {
    ALL,
    COUNTRY,
    CITY,
    RESORT
}

data class DestinationItem(
    val id: String,
    val name: String,
    val parentName: String? = null,
    val countryName: String,
    val flag: String,
    val level: DestinationLevel,
    val airportCode: String? = null,
    val description: String? = null
)

/**
 * TourOS Tasarım Sistemine uygun Hiyerarşik Destinasyon Seçici (Ülke -> Şehir/Hub -> Belde/Resort).
 */
@Composable
fun HierarchicalDestinationPickerDialog(
    currentSelection: String = "",
    onDestinationSelected: (DestinationItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allDestinations = remember {
        listOf(
            // TÜRKİYE
            DestinationItem("tr_all", "Türkiye (Tüm Bölgeler)", null, "Türkiye", "🇹🇷", DestinationLevel.COUNTRY, null, "Tüm Türkiye turları ve otelleri"),
            
            // ANTALYA
            DestinationItem("tr_ayt", "Antalya (Tüm Bölge)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "AYT", "Lara, Belek, Side, Kemer, Alanya"),
            DestinationItem("tr_ayt_belek", "Belek / Boğazkent", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Lüks Resort & Golf Otelleri"),
            DestinationItem("tr_ayt_lara", "Lara / Kundu", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Havalimanına En Yakın Sahil Bandı"),
            DestinationItem("tr_ayt_side", "Side / Manavgat", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Tarihi Yarımada & Kum Plajlar"),
            DestinationItem("tr_ayt_kemer", "Kemer / Beldibi / Tekirova", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Dağ & Deniz Manzaralı Tesisler"),
            DestinationItem("tr_ayt_alanya", "Alanya / Okurcalar / Mahmutlar", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "GZP", "Kleopatra Plajı & Kalabalık Merkez"),
            DestinationItem("tr_ayt_cirali", "Çıralı / Olimpos / Kaş", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Butik & Doğa Otelleri"),

            // MUĞLA / EGE
            DestinationItem("tr_mugla", "Muğla (Tüm Ege)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "BJV", "Bodrum, Marmaris, Fethiye, Datça"),
            DestinationItem("tr_bjv_bodrum", "Bodrum (Yalıkavak, Torba, Gümbet)", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "BJV", "Gece Hayatı & Marinalar"),
            DestinationItem("tr_dlm_marmaris", "Marmaris / İçmeler / Turunç", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Koylar & Çam Ormanları"),
            DestinationItem("tr_dlm_fethiye", "Fethiye / Ölüdeniz / Göcek", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Mavi Yolculuk & Yamaç Paraşütü"),
            DestinationItem("tr_dlm_datca", "Datça", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Sakin Koylar & Taş Evler"),

            // İZMİR
            DestinationItem("tr_izmir", "İzmir (Tüm Bölge)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "ADB", "Çeşme, Alaçatı, Kuşadası"),
            DestinationItem("tr_adb_cesme", "Çeşme / Alaçatı", "İzmir", "Türkiye", "🏖️", DestinationLevel.RESORT, "ADB", "Rüzgar Sörfü & Butik Taş Oteller"),
            DestinationItem("tr_adb_kusadasi", "Kuşadası / Selçuk", "İzmir", "Türkiye", "🏖️", DestinationLevel.RESORT, "ADB", "Efes Antik Kenti & Plajlar"),

            // İSTANBUL & DİĞER
            DestinationItem("tr_ist", "İstanbul (Tüm Şehir)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "IST", "Sultanahmet, Taksim, Boğaz"),
            DestinationItem("tr_nav", "Kapadokya (Göreme / Ürgüp)", "Nevşehir", "Türkiye", "🎈", DestinationLevel.RESORT, "NAV", "Balon Turları & Mağara Oteller"),

            // RUSYA
            DestinationItem("ru_all", "Rusya (Tüm Şehirler)", null, "Rusya", "🇷🇺", DestinationLevel.COUNTRY, null, "Moskova, St. Petersburg, Sochi, Kazan"),
            DestinationItem("ru_svo", "Moskova (Tüm Havalimanları)", "Rusya", "Rusya", "🇷🇺", DestinationLevel.CITY, "SVO", "Şeremetyevo, Domodedovo, Vnukovo"),
            DestinationItem("ru_led", "St. Petersburg", "Rusya", "Rusya", "🇷🇺", DestinationLevel.CITY, "LED", "Pulkovo & Kültür Turları"),
            DestinationItem("ru_aer", "Sochi / Adler / Krasnaya Polyana", "Rusya", "Rusya", "🇷🇺", DestinationLevel.CITY, "AER", "Karadeniz Sahili & Kayak Merkezleri"),
            DestinationItem("ru_kzn", "Kazan", "Rusya", "Rusya", "🇷🇺", DestinationLevel.CITY, "KZN", "Volga & Tataristan"),

            // MISIR
            DestinationItem("eg_all", "Mısır (Tüm Bölgeler)", null, "Mısır", "🇪🇬", DestinationLevel.COUNTRY, null, "Kızıldeniz & Nil Nehri Turları"),
            DestinationItem("eg_ssh", "Şarm El-Şeyh (Naama / Nabq)", "Mısır", "Mısır", "🏖️", DestinationLevel.RESORT, "SSH", "Dalış Merkezleri & Mercan Resifleri"),
            DestinationItem("eg_hrg", "Hurgada (El Gouna / Makadi)", "Mısır", "Mısır", "🏖️", DestinationLevel.RESORT, "HRG", "Kum Plajlar & Su Sporları"),

            // TAYLAND
            DestinationItem("th_all", "Tayland (Tüm Bölgeler)", null, "Tayland", "🇹🇭", DestinationLevel.COUNTRY, null, "Phuket, Pattaya, Bangkok, Koh Samui"),
            DestinationItem("th_hkt", "Phuket (Patong, Karon, Kata)", "Tayland", "Tayland", "🏖️", DestinationLevel.RESORT, "HKT", "Tropikal Ada & Plaj Kulüpleri"),
            DestinationItem("th_utp", "Pattaya (Jomtien, Naklua)", "Tayland", "Tayland", "🏖️", DestinationLevel.RESORT, "UTP", "Eğlence, Gece Hayatı & Su Sporları"),
            DestinationItem("th_bkk", "Bangkok (Sukhumvit, Silom)", "Tayland", "Tayland", "🏙️", DestinationLevel.CITY, "BKK", "Tapınaklar & Alışveriş"),
            DestinationItem("th_usm", "Koh Samui (Chaweng)", "Tayland", "Tayland", "🏝️", DestinationLevel.RESORT, "USM", "Palmiye Plajları & Lüks Villalar"),

            // VİETNAM
            DestinationItem("vn_all", "Vietnam (Tüm Bölgeler)", null, "Vietnam", "🇻🇳", DestinationLevel.COUNTRY, null, "Da Nang, Phu Quoc, Nha Trang"),
            DestinationItem("vn_dad", "Da Nang / Hoi An", "Vietnam", "Vietnam", "🏖️", DestinationLevel.RESORT, "DAD", "Mermer Dağları & Altın Köprü"),
            DestinationItem("vn_pqc", "Phu Quoc (Long Beach)", "Vietnam", "Vietnam", "🏝️", DestinationLevel.RESORT, "PQC", "Tropikal Ada & Gün Batımı Kasabası"),
            DestinationItem("vn_cxr", "Nha Trang (Tran Phu)", "Vietnam", "Vietnam", "🏖️", DestinationLevel.RESORT, "CXR", "Akdeniz Havasında Asya Sahili"),

            // BAE
            DestinationItem("ae_all", "Birleşik Arap Emirlikleri", null, "BAE", "🇦🇪", DestinationLevel.COUNTRY, null, "Dubai, Abu Dhabi, Sharjah"),
            DestinationItem("ae_dxb", "Dubai (Marina, Palm, Downtown)", "BAE", "BAE", "🏙️", DestinationLevel.CITY, "DXB", "Lüks Oteller & Çöl Safarisi"),
            DestinationItem("ae_auh", "Abu Dhabi (Saadiyat, Yas)", "BAE", "BAE", "🏖️", DestinationLevel.CITY, "AUH", "Louvre & Tema Parklar")
        )
    }

    val filteredDestinations = remember(searchQuery) {
        if (searchQuery.isBlank()) allDestinations
        else {
            allDestinations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.countryName.contains(searchQuery, ignoreCase = true) ||
                (it.parentName?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.airportCode?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(620.dp)
                .height(640.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = AppLanguageManager.translate("DESTİNASYON SEÇİMİ"),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate("Nereye Gitmek İstiyorsunuz?"),
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

                // Canlı Arama Input'u
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Ülke, şehir, belde (ör. Antalya, Belek, Bodrum)...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Hiyerarşik Liste
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDestinations, key = { it.id }) { item ->
                        val isSelected = currentSelection.contains(item.name, ignoreCase = true)

                        val indentPadding = when (item.level) {
                            DestinationLevel.COUNTRY -> 0.dp
                            DestinationLevel.CITY -> 16.dp
                            DestinationLevel.RESORT -> 32.dp
                            else -> 0.dp
                        }

                        val badgeBg = when (item.level) {
                            DestinationLevel.COUNTRY -> Color(0xFF0F5A56)
                            DestinationLevel.CITY -> Color(0xFF14B8A6)
                            DestinationLevel.RESORT -> Color(0xFFE2E8F0)
                            else -> Color(0xFF94A3B8)
                        }

                        val badgeText = when (item.level) {
                            DestinationLevel.COUNTRY -> "ÜLKE"
                            DestinationLevel.CITY -> "ŞEHİR / BÖLGE"
                            DestinationLevel.RESORT -> "BELDE / RESORT"
                            else -> ""
                        }

                        val badgeTextColor = if (item.level == DestinationLevel.RESORT) Color(0xFF334155) else Color.White

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFE6F4F1) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = indentPadding)
                                .clickable {
                                    onDestinationSelected(item)
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.flag, fontSize = 20.sp)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = TourOSTypography.BodyMedium.copy(
                                                    fontWeight = if (item.level == DestinationLevel.COUNTRY || item.level == DestinationLevel.CITY) FontWeight.Bold else FontWeight.Medium,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 14.sp
                                                )
                                            )
                                            if (item.airportCode != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFEFF6FF)
                                                ) {
                                                    Text(
                                                        text = "✈ ${item.airportCode}",
                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (item.description != null) {
                                            Text(
                                                text = item.description,
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = TourOSTypography.Caption.copy(color = badgeTextColor, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
