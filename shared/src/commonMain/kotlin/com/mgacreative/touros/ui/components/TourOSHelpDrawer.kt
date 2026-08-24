package com.mgacreative.touros.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.HelpGuide
import com.mgacreative.touros.domain.repository.HelpGuideRepository
import com.mgacreative.touros.ui.localization.AppLanguageManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Sayfa İçi Kompakt Pop-up Asistan Kartı (Floating Pop-up Assistant).
 * Ekranı kaplamadan sağ alt köşede zarif bir chat penceresi şeklinde açılır.
 */
@Composable
fun TourOSHelpDrawer(
    currentRoute: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean = true
) {
    val helpGuideRepository: HelpGuideRepository = koinInject()
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Sayfa kimliğini kesin olarak normalize etme
    val detectedRouteKey = remember(currentRoute) {
        resolveScreenKey(currentRoute)
    }

    var selectedRouteKey by remember(detectedRouteKey) {
        mutableStateOf(detectedRouteKey)
    }

    var guides by remember { mutableStateOf<List<HelpGuide>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedQuestionId by remember { mutableStateOf<String?>(null) }
    var isScreenSelectorOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRouteKey, currentLanguage) {
        isLoading = true
        coroutineScope.launch {
            val result = helpGuideRepository.getHelpGuidesForScreen(selectedRouteKey, currentLanguage.code)
            guides = result.getOrDefault(emptyList())
            isLoading = false
            expandedQuestionId = guides.firstOrNull()?.id
        }
    }

    val filteredGuides = remember(guides, searchQuery) {
        guides.filter { guide ->
            searchQuery.isBlank() ||
                    guide.question.contains(searchQuery, ignoreCase = true) ||
                    guide.answer.contains(searchQuery, ignoreCase = true) ||
                    guide.title.contains(searchQuery, ignoreCase = true)
        }
    }

    val availableScreens = remember(currentLanguage) {
        listOf(
            "TourFormRoute" to getScreenFriendlyTitle("TourFormRoute", currentLanguage.code),
            "HotelFormRoute" to getScreenFriendlyTitle("HotelFormRoute", currentLanguage.code),
            "CompanySettingsRoute" to getScreenFriendlyTitle("CompanySettingsRoute", currentLanguage.code),
            "B2BTourSearchDashboardRoute" to getScreenFriendlyTitle("B2BTourSearchDashboardRoute", currentLanguage.code),
            "BookingsRoute" to getScreenFriendlyTitle("BookingsRoute", currentLanguage.code),
            "FinancialReportsRoute" to getScreenFriendlyTitle("FinancialReportsRoute", currentLanguage.code),
            "DashboardRoute" to getScreenFriendlyTitle("DashboardRoute", currentLanguage.code)
        )
    }

    val screenTitleDisplay = remember(selectedRouteKey, currentLanguage) {
        getScreenFriendlyTitle(selectedRouteKey, currentLanguage.code)
    }

    // Karartma Arka Planı (Hafif Şeffaf)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable(onClick = onDismiss),
        contentAlignment = if (isExpandedScreen) Alignment.BottomEnd else Alignment.BottomCenter
    ) {
        // Kompakt Pop-up Kartı
        Surface(
            modifier = modifier
                .clickable(enabled = false) {}
                .padding(
                    end = if (isExpandedScreen) 24.dp else 12.dp,
                    bottom = if (isExpandedScreen) 88.dp else 76.dp,
                    start = if (isExpandedScreen) 0.dp else 12.dp
                )
                .then(
                    if (isExpandedScreen) {
                        Modifier
                            .width(375.dp)
                            .height(510.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(490.dp)
                    }
                )
                .shadow(20.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0xFF006B5E).copy(alpha = 0.35f)),
            color = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFBFDF9))
            ) {
                // ── 1. POP-UP HEADER ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF006B5E), Color(0xFF1F4E5F))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💬", fontSize = 15.sp)
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isScreenSelectorOpen = !isScreenSelectorOpen }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (currentLanguage.code) {
                                        "tr" -> "Sayfa Rehberi"
                                        "ru" -> "Справка по странице"
                                        else -> "Page Guide"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = screenTitleDisplay,
                                        color = Color(0xFF7CF8E1),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = " ▾",
                                        color = Color(0xFF7CF8E1),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Dil Toggle Butonu (TR -> EN -> RU -> TR)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .clickable {
                                        val nextLang = when (currentLanguage.code) {
                                            "tr" -> "en"
                                            "en" -> "ru"
                                            else -> "tr"
                                        }
                                        AppLanguageManager.setLanguage(nextLang)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = currentLanguage.code.uppercase(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Kapat Butonu
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── 1.1 SAYFA SEÇİM AÇILIR MENÜSÜ ──
                if (isScreenSelectorOpen) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE6F4F1),
                        tonalElevation = 2.dp
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableScreens) { (routeKey, title) ->
                                val isSelected = selectedRouteKey == routeKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF006B5E) else Color.White)
                                        .clickable {
                                            selectedRouteKey = routeKey
                                            isScreenSelectorOpen = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 2. ARAMA KUTUSU ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = when (currentLanguage.code) {
                                    "tr" -> "Bu sayfada nasıl yapılır? Ara..."
                                    "ru" -> "Поиск по этой странице..."
                                    else -> "Search on this page..."
                                },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF006B5E),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFFECEFF1), thickness = 1.dp)

                // ── 3. SORU & CEVAP AKIŞI ──
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage.code) {
                                "tr" -> "Yükleniyor..."
                                "ru" -> "Загрузка..."
                                else -> "Loading..."
                            },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else if (filteredGuides.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage.code) {
                                "tr" -> "Rehber bulunamadı."
                                "ru" -> "Ничего не найдено."
                                else -> "No guide found."
                            },
                            color = Color.Gray,
                            fontSize = 12.5.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGuides, key = { it.id.ifBlank { it.question } }) { guide ->
                            val isExpanded = expandedQuestionId == guide.id || (expandedQuestionId == null && guide == filteredGuides.first())

                            CompactGuideCard(
                                guide = guide,
                                isExpanded = isExpanded,
                                onClick = {
                                    expandedQuestionId = if (isExpanded) null else guide.id
                                }
                            )
                        }
                    }
                }

                // ── 4. FOOTER ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (currentLanguage.code) {
                                "tr" -> "💡 Sayfa alanlarına göre hazır rehber"
                                "ru" -> "💡 Подсказки по заполнению полей"
                                else -> "💡 In-app guided field tips"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGuideCard(
    guide: HelpGuide,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isExpanded) 1.2.dp else 1.dp,
                color = if (isExpanded) Color(0xFF006B5E) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        color = if (isExpanded) Color(0xFFF4FBF9) else Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (isExpanded) Color(0xFF006B5E) else Color(0xFFE2E8F0),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${guide.stepOrder}",
                            color = if (isExpanded) Color.White else Color(0xFF475569),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = guide.question,
                        color = if (isExpanded) Color(0xFF006B5E) else Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 28.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFCCEDE5), thickness = 1.dp, modifier = Modifier.padding(bottom = 6.dp))

                    Text(
                        text = guide.answer,
                        color = Color(0xFF334155),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Sayfa rotasını güvenilir şekilde normalize eder.
 */
fun resolveScreenKey(rawRoute: String?): String {
    if (rawRoute.isNullOrBlank()) return "DashboardRoute"
    val clean = rawRoute.substringBefore("?").substringBefore("/").substringAfterLast(".")
    return when {
        clean.contains("TourForm", ignoreCase = true) -> "TourFormRoute"
        clean.contains("HotelForm", ignoreCase = true) -> "HotelFormRoute"
        clean.contains("CompanySettings", ignoreCase = true) || clean.contains("Settings", ignoreCase = true) -> "CompanySettingsRoute"
        clean.contains("B2BTourSearch", ignoreCase = true) -> "B2BTourSearchDashboardRoute"
        clean.contains("Booking", ignoreCase = true) -> "BookingsRoute"
        clean.contains("Finance", ignoreCase = true) || clean.contains("Invoice", ignoreCase = true) || clean.contains("CurrentAccount", ignoreCase = true) -> "FinancialReportsRoute"
        clean.contains("Dashboard", ignoreCase = true) -> "DashboardRoute"
        clean.contains("Hotel", ignoreCase = true) -> "HotelFormRoute"
        clean.contains("Tour", ignoreCase = true) -> "TourFormRoute"
        else -> "DashboardRoute"
    }
}

private fun getScreenFriendlyTitle(route: String, lang: String): String {
    val lower = lang.lowercase()
    val isTr = lower.startsWith("tr")
    val isRu = lower.startsWith("ru")
    return when {
        route.contains("TourForm") -> when {
            isTr -> "Yeni Tur Oluştur / Düzenle"
            isRu -> "Создание / Редактирование тура"
            else -> "Create / Edit Tour"
        }
        route.contains("HotelForm") -> when {
            isTr -> "Yeni Otel Kaydı"
            isRu -> "Новый отель"
            else -> "Create New Hotel"
        }
        route.contains("CompanySettings") || route.contains("Settings") -> when {
            isTr -> "Şirket & Marka Ayarları"
            isRu -> "Настройки компании и бренда"
            else -> "Company & Branding Settings"
        }
        route.contains("B2BTourSearch") -> when {
            isTr -> "B2B Hizmet Arama & Rezervasyon"
            isRu -> "Поиск B2B и бронирование"
            else -> "B2B Search & Booking"
        }
        route.contains("Booking") -> when {
            isTr -> "Rezervasyon Yönetimi"
            isRu -> "Управление бронированиями"
            else -> "Booking Management"
        }
        route.contains("Finance") || route.contains("Invoice") || route.contains("CurrentAccount") -> when {
            isTr -> "Finans & Muhasebe"
            isRu -> "Финансы и бухгалтерия"
            else -> "Finance & Accounting"
        }
        route.contains("Dashboard") -> when {
            isTr -> "Genel Bakış & Dashboard"
            isRu -> "Обзор и Дашборд"
            else -> "Overview & Dashboard"
        }
        else -> when {
            isTr -> "Sayfa Rehberi"
            isRu -> "Справка по странице"
            else -> "Page Guide"
        }
    }
}
