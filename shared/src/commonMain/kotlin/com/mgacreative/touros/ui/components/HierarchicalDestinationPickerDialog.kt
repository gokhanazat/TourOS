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
    val nameRu: String = "",
    val parentName: String? = null,
    val countryName: String,
    val flag: String,
    val level: DestinationLevel,
    val airportCode: String? = null,
    val description: String? = null
)

/**
 * TourOS Hiyerarşik Destinasyon Seçici (Ülke -> Şehir/Hub -> Belde/Resort).
 * Çift Dilli (Türkçe & Kiril Rusça) ve Kademeli Ülke Filtreli.
 */
@Composable
fun HierarchicalDestinationPickerDialog(
    currentSelection: String = "",
    onDestinationSelected: (DestinationItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountryTab by remember { mutableStateOf("ALL") }

    val allDestinations = remember {
        listOf(
            // TÜRKİYE
            DestinationItem("tr_all", "Türkiye (Tüm Bölgeler)", "Турция (Все регионы)", null, "Türkiye", "🇹🇷", DestinationLevel.COUNTRY, null, "Tüm Türkiye turları ve otelleri"),
            
            // ANTALYA
            DestinationItem("tr_ayt", "Antalya (Tüm Bölge)", "Анталья (Все курорты)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "AYT", "Lara, Belek, Side, Kemer, Alanya"),
            DestinationItem("tr_ayt_belek", "Belek / Boğazkent", "Белек / Богазкент", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Lüks Resort & Golf Otelleri"),
            DestinationItem("tr_ayt_lara", "Lara / Kundu", "Лара / Кунду", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Havalimanına En Yakın Sahil Bandı"),
            DestinationItem("tr_ayt_side", "Side / Manavgat", "Сиде / Манавгат", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Tarihi Yarımada & Kum Plajlar"),
            DestinationItem("tr_ayt_kemer", "Kemer / Beldibi / Tekirova", "Кемер / Бельдиби / Текирова", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "AYT", "Dağ & Deniz Manzaralı Tesisler"),
            DestinationItem("tr_ayt_alanya", "Alanya / Okurcalar / Mahmutlar", "Аланья / Окурджалар / Махмутлар", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "GZP", "Kleopatra Plajı & Kalabalık Merkez"),
            DestinationItem("tr_ayt_cirali", "Çıralı / Olimpos / Kaş", "Чиралы / Олимпос / Каш", "Antalya", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Butik & Doğa Otelleri"),

            // MUĞLA / EGE
            DestinationItem("tr_mugla", "Muğla (Tüm Ege)", "Эгейское побережье (Мугла)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "BJV", "Bodrum, Marmaris, Fethiye, Datça"),
            DestinationItem("tr_bjv_bodrum", "Bodrum (Yalıkavak, Torba, Gümbet)", "Бодрум (Ялыкавак, Торба, Гюмбет)", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "BJV", "Gece Hayatı & Marinalar"),
            DestinationItem("tr_dlm_marmaris", "Marmaris / İçmeler / Turunç", "Мармарис / Ичмелер / Турунч", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Koylar & Çam Ormanları"),
            DestinationItem("tr_dlm_fethiye", "Fethiye / Ölüdeniz / Göcek", "Фетхие / Олюдениз / Гёчек", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Mavi Yolculuk & Yamaç Paraşütü"),
            DestinationItem("tr_dlm_datca", "Datça", "Датча", "Muğla", "Türkiye", "🏖️", DestinationLevel.RESORT, "DLM", "Sakin Koylar & Taş Evler"),

            // İZMİR
            DestinationItem("tr_izmir", "İzmir (Tüm Bölge)", "Измир (Чешме, Кушадасы)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "ADB", "Çeşme, Alaçatı, Kuşadası"),
            DestinationItem("tr_adb_cesme", "Çeşme / Alaçatı", "Чешме / Алачаты", "İzmir", "Türkiye", "🏖️", DestinationLevel.RESORT, "ADB", "Rüzgar Sörfü & Butik Taş Oteller"),
            DestinationItem("tr_adb_kusadasi", "Kuşadası / Selçuk", "Кушадасы / Сельчук", "İzmir", "Türkiye", "🏖️", DestinationLevel.RESORT, "ADB", "Efes Antik Kenti & Plajlar"),

            // İSTANBUL & KAPADOKYA
            DestinationItem("tr_ist", "İstanbul (Tüm Şehir)", "Стамбул (Все районы)", "Türkiye", "Türkiye", "🇹🇷", DestinationLevel.CITY, "IST", "Sultanahmet, Taksim, Boğaz"),
            DestinationItem("tr_nav", "Kapadokya (Göreme / Ürgüp)", "Каппадокия (Гёреме / Ургюп)", "Nevşehir", "Türkiye", "🎈", DestinationLevel.RESORT, "NAV", "Balon Turları & Mağara Oteller"),

            // MISIR
            DestinationItem("eg_all", "Mısır (Tüm Bölgeler)", "Египет (Все курорты)", null, "Mısır", "🇪🇬", DestinationLevel.COUNTRY, null, "Kızıldeniz & Nil Nehri Turları"),
            DestinationItem("eg_ssh", "Şarm El-Şeyh (Naama / Nabq)", "Шарм-эль-Шейх (Наама / Набк)", "Mısır", "Mısır", "🏖️", DestinationLevel.RESORT, "SSH", "Dalış Merkezleri & Mercan Resifleri"),
            DestinationItem("eg_hrg", "Hurgada (El Gouna / Makadi)", "Хургада (Эль-Гуна / Макади)", "Mısır", "Mısır", "🏖️", DestinationLevel.RESORT, "HRG", "Kum Plajlar & Su Sporları"),

            // BAE
            DestinationItem("ae_all", "Birleşik Arap Emirlikleri", "ОАЭ (Все эмираты)", null, "BAE", "🇦🇪", DestinationLevel.COUNTRY, null, "Dubai, Abu Dhabi, Sharjah"),
            DestinationItem("ae_dxb", "Dubai (Marina, Palm, Downtown)", "Дубай (Марина, Пальм, Даунтаун)", "BAE", "BAE", "🏙️", DestinationLevel.CITY, "DXB", "Lüks Oteller & Çöl Safarisi"),
            DestinationItem("ae_auh", "Abu Dhabi (Saadiyat, Yas)", "Абу-Даби (Саадият, Яс)", "BAE", "BAE", "🏖️", DestinationLevel.CITY, "AUH", "Louvre & Tema Parklar"),

            // TAYLAND
            DestinationItem("th_all", "Tayland (Tüm Bölgeler)", "Таиланд (Все курорты)", null, "Tayland", "🇹🇭", DestinationLevel.COUNTRY, null, "Phuket, Pattaya, Bangkok, Koh Samui"),
            DestinationItem("th_hkt", "Phuket (Patong, Karon, Kata)", "Пхукет (Патонг, Карон, Ката)", "Tayland", "Tayland", "🏖️", DestinationLevel.RESORT, "HKT", "Tropikal Ada & Plaj Kulüpleri"),
            DestinationItem("th_utp", "Pattaya (Jomtien, Naklua)", "Паттайя (Джомтьен, Наклуа)", "Tayland", "Tayland", "🏖️", DestinationLevel.RESORT, "UTP", "Eğlence, Gece Hayatı & Su Sporları"),
            DestinationItem("th_bkk", "Bangkok (Sukhumvit, Silom)", "Бангкок", "Tayland", "Tayland", "🏙️", DestinationLevel.CITY, "BKK", "Tapınaklar & Alışveriş"),
            DestinationItem("th_usm", "Koh Samui (Chaweng)", "Самуи (Чавенг)", "Tayland", "Tayland", "🏝️", DestinationLevel.RESORT, "USM", "Palmiye Plajları & Lüks Villalar"),

            // VİETNAM
            DestinationItem("vn_all", "Vietnam (Tüm Bölgeler)", "Вьетнам (Все курорты)", null, "Vietnam", "🇻🇳", DestinationLevel.COUNTRY, null, "Da Nang, Phu Quoc, Nha Trang"),
            DestinationItem("vn_dad", "Da Nang / Hoi An", "Дананг / Хойан", "Vietnam", "Vietnam", "🏖️", DestinationLevel.RESORT, "DAD", "Mermer Dağları & Altın Köprü"),
            DestinationItem("vn_pqc", "Phu Quoc (Long Beach)", "Фукуок (Лонг Бич)", "Vietnam", "Vietnam", "🏝️", DestinationLevel.RESORT, "PQC", "Tropikal Ada & Gün Batımı Kasabası"),
            DestinationItem("vn_cxr", "Nha Trang (Tran Phu)", "Нячанг (Чан Фу)", "Vietnam", "Vietnam", "🏖️", DestinationLevel.RESORT, "CXR", "Akdeniz Havasında Asya Sahili")
        )
    }

    val filteredDestinations = remember(searchQuery, selectedCountryTab) {
        val listByCountry = if (selectedCountryTab == "ALL") allDestinations else allDestinations.filter { it.countryName.equals(selectedCountryTab, ignoreCase = true) }
        if (searchQuery.isBlank()) {
            listByCountry
        } else {
            val q = searchQuery.trim().lowercase()
            allDestinations.filter {
                it.name.lowercase().contains(q) ||
                it.nameRu.lowercase().contains(q) ||
                it.countryName.lowercase().contains(q) ||
                (it.parentName?.lowercase()?.contains(q) == true) ||
                (it.airportCode?.lowercase()?.contains(q) == true)
            }
        }
    }

    val countryTabs = listOf(
        "ALL" to "🌍 Tüm Ülkeler / Все страны",
        "Türkiye" to "🇹🇷 Türkiye / Турция",
        "Mısır" to "🇪🇬 Mısır / Египет",
        "BAE" to "🇦🇪 BAE / ОАЭ",
        "Tayland" to "🇹🇭 Tayland / Таиланд",
        "Vietnam" to "🇻🇳 Vietnam / Вьетнам"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 360.dp, max = 940.dp)
                .fillMaxWidth(0.92f)
                .heightIn(min = 520.dp, max = 720.dp)
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
                            text = "🌍 КУДА ВЫ ХОТИТЕ ПОЕХАТЬ? / DESTİNASYON SEÇİMİ",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Text(
                            text = "1. Ülke → 2. Şehir → 3. Alt Belde / Resort Seçimi",
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
                    placeholder = { Text("🔍 Поиск: Турция, Анталья, Белек, AYT, Bodrum...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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

                // Ülke Sekmeleri (Aramaya Ülkeden Başlama)
                if (searchQuery.isBlank()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(countryTabs) { (code, label) ->
                            val isSelected = selectedCountryTab == code
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { selectedCountryTab = code },
                                color = if (isSelected) Color(0xFF0F5A56) else Color(0xFFF8FAFC)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = TourOSTypography.Caption.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF0F172A)
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Hiyerarşik Liste
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredDestinations, key = { it.id }) { item ->
                        val isSelected = currentSelection.contains(item.name, ignoreCase = true) || (item.nameRu.isNotBlank() && currentSelection.contains(item.nameRu, ignoreCase = true))

                        val indentPadding = when (item.level) {
                            DestinationLevel.COUNTRY -> 0.dp
                            DestinationLevel.CITY -> 12.dp
                            DestinationLevel.RESORT -> 24.dp
                            else -> 0.dp
                        }

                        val badgeBg = when (item.level) {
                            DestinationLevel.COUNTRY -> Color(0xFF0F5A56)
                            DestinationLevel.CITY -> Color(0xFF14B8A6)
                            DestinationLevel.RESORT -> Color(0xFFE2E8F0)
                            else -> Color(0xFF94A3B8)
                        }

                        val badgeText = when (item.level) {
                            DestinationLevel.COUNTRY -> "ÜLKE / СТРАНА"
                            DestinationLevel.CITY -> "ŞEHİR / ГОРОД"
                            DestinationLevel.RESORT -> "RESORT / КУРОРТ"
                            else -> ""
                        }

                        val badgeTextColor = if (item.level == DestinationLevel.RESORT) Color(0xFF334155) else Color.White

                        Surface(
                            shape = RoundedCornerShape(10.dp),
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
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.flag, fontSize = 20.sp)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (item.nameRu.isNotBlank()) "${item.name} · ${item.nameRu}" else item.name,
                                                style = TourOSTypography.BodyMedium.copy(
                                                    fontWeight = if (item.level == DestinationLevel.COUNTRY || item.level == DestinationLevel.CITY) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (item.airportCode != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFEFF6FF),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                                ) {
                                                    Text(
                                                        text = "✈ ${item.airportCode}",
                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (item.description != null) {
                                            Text(
                                                text = item.description,
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = TourOSTypography.Caption.copy(color = badgeTextColor, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        maxLines = 1
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
