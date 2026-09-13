package com.mgacreative.touros.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mgacreative.touros.ai.model.AIGroupedHotelOffer
import com.mgacreative.touros.ai.model.AIAssistantMessage
import com.mgacreative.touros.ai.viewmodel.AIAssistantViewModel
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import kotlin.math.roundToInt

@Composable
fun AIAssistantFloatingWidget(
    viewModel: AIAssistantViewModel,
    modifier: Modifier = Modifier,
    onNavigateToBooking: (productId: String, agencyId: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    // Boyutlandırma
    var windowWidth by remember { mutableStateOf(820.dp) }
    var windowHeight by remember { mutableStateOf(680.dp) }
    var isMinimized by remember { mutableStateOf(false) }

    // Sürüklenebilir Pozisyon
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // 2. ve 3. Adımlarda Otomatik %50 Küçülme
    LaunchedEffect(uiState.isCompactMode) {
        if (uiState.isCompactMode) {
            windowWidth = 550.dp
            windowHeight = 600.dp
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // 1. Sürüklenebilir Chat Penceresi
        AnimatedVisibility(
            visible = uiState.isOpen,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Surface(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(TourOSSpacing.large)
                    .width(if (isMinimized) 360.dp else windowWidth)
                    .height(if (isMinimized) 56.dp else windowHeight)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, TourOSColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                color = TourOSColors.Surface,
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sürüklenebilir Üst Başlık Barı (Drag Handle)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(TourOSColors.Primary, TourOSColors.Primary.copy(alpha = 0.85f))
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                            .padding(TourOSSpacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "Taşı",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Онлайн-турагент TourOS (RU)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (!isMinimized) {
                                    Text(
                                        "Персональный подбор туров & Онлайн-помощь (Перетащите за шапку)",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (!isMinimized) {
                                // Boyut Butonları
                                TextButton(
                                    onClick = { windowWidth = 550.dp; windowHeight = 600.dp },
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("50%", color = if (windowWidth == 550.dp) Color.Yellow else Color.White, fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = { windowWidth = 820.dp; windowHeight = 680.dp },
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("75%", color = if (windowWidth == 820.dp) Color.Yellow else Color.White, fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = { windowWidth = 1080.dp; windowHeight = 750.dp },
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("100%", color = if (windowWidth == 1080.dp) Color.Yellow else Color.White, fontSize = 11.sp)
                                }

                                // Debug Toggle Butonu
                                IconButton(
                                    onClick = { viewModel.toggleDebugPanel() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Translate,
                                        contentDescription = "Debug TR",
                                        tint = if (uiState.isDebugPanelVisible) Color.Yellow else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // 🧹 Sohbeti Temizle / Yeni Arama Butonu
                                IconButton(
                                    onClick = { viewModel.clearChat() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = "Sohbeti Temizle",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Simge Durumuna Küçült / Aç
                            IconButton(
                                onClick = { isMinimized = !isMinimized },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (isMinimized) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Küçült/Büyüt",
                                    tint = Color.White
                                )
                            }

                            // Kapat Butonu
                            IconButton(
                                onClick = { viewModel.toggleChat() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                            }
                        }
                    }

                    // İçerik (Sadece pencere açıkken)
                    if (!isMinimized) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(TourOSSpacing.small),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.messages) { msg ->
                                ChatMessageItem(msg, isDebugVisible = uiState.isDebugPanelVisible)
                            }

                            // 🏨 GRUPLANMIŞ OTELLER (Aynı otel tek satır, altında operatör teklifleri)
                            if (uiState.currentGroupedHotels.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "✨ Найдено ${uiState.currentGroupedHotels.size} отелей (нажмите на отель для выбора туроператора):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TourOSColors.Primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                items(uiState.currentGroupedHotels) { hotelGroup ->
                                    AIGroupedHotelCard(
                                        hotelGroup = hotelGroup,
                                        onSelectOffer = { selectedProduct ->
                                            viewModel.onProductSelectedForBooking(selectedProduct)
                                            onNavigateToBooking(selectedProduct.id, "acn-101")
                                        }
                                    )
                                }
                            }

                            if (uiState.isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = TourOSColors.Primary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "Ищу предложения туроператоров...",
                                            fontSize = 12.sp,
                                            color = TourOSColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Alt Mesaj Yazma Barı
                        HorizontalDivider(color = TourOSColors.Border)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Напишите запрос на русском...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onSend = {
                                        if (inputText.isNotBlank() && !uiState.isLoading) {
                                            viewModel.sendMessage(inputText)
                                            inputText = ""
                                        }
                                    }
                                ),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Send
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TourOSColors.Primary,
                                    unfocusedBorderColor = TourOSColors.Border
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() && !uiState.isLoading) {
                                        viewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .background(TourOSColors.Primary, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // 2. Yüzen Açma Butonu (Kişisel Danışman Kimliği & Şık Kapsül Buton)
        if (!uiState.isOpen) {
            Surface(
                onClick = { viewModel.toggleChat() },
                shape = RoundedCornerShape(28.dp),
                color = TourOSColors.Primary,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Тур-консультант AI",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Онлайн-турагент AI",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(msg: AIAssistantMessage, isDebugVisible: Boolean) {
    val isUser = msg.sender == "USER"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) TourOSColors.Primary else TourOSColors.SurfaceVariant,
            modifier = Modifier.widthIn(max = 580.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.textRu,
                    color = if (isUser) Color.White else TourOSColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                if (isDebugVisible && msg.debugTranslationTr.isNotBlank() && !isUser) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TR: ${msg.debugTranslationTr}",
                            fontSize = 10.sp,
                            color = TourOSColors.Secondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🏨 TEK SATIR OTEL KARTI & TIKLANDIĞINDA AÇILAN OPERATÖR TEKLİFLERİ (AKORDİYON)
 */
@Composable
private fun AIGroupedHotelCard(
    hotelGroup: AIGroupedHotelOffer,
    onSelectOffer: (UnifiedProductEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, TourOSColors.Border, RoundedCornerShape(14.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ─── 1. Ana Otel Satırı ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol Görsel
                AsyncImage(
                    model = hotelGroup.pictureUrl?.ifBlank { "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=300" } ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=300",
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 120.dp, height = 80.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(Modifier.width(14.dp))

                // Orta Bilgiler
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            hotelGroup.hotelName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TourOSColors.TextPrimary
                        )
                        Spacer(Modifier.width(6.dp))
                        repeat(hotelGroup.stars.coerceIn(1, 5)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "📍 ${hotelGroup.region}${if (hotelGroup.subRegion.isNotBlank()) " (${hotelGroup.subRegion})" else ""}, ${hotelGroup.country}",
                        fontSize = 11.sp,
                        color = TourOSColors.TextSecondary
                    )

                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "✈️ ${hotelGroup.departureCity} • ${hotelGroup.nights} Ночей",
                            fontSize = 11.sp,
                            color = Color(0xFF0F5A56),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "🍽️ ${hotelGroup.mealType}",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Sağ: En Düşük Fiyat ve Operatörleri Göster Butonu
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "от ${hotelGroup.minPrice.toInt()} ${hotelGroup.currency}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = TourOSColors.Primary
                    )
                    Text(
                        "${hotelGroup.operatorOffers.size} предложений операторов",
                        fontSize = 10.sp,
                        color = Color(0xFF0F5A56),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpanded) Color(0xFF1E293B) else Color(0xFF0F5A56),
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isExpanded) "Скрыть цены ▲" else "Цены операторов ▼",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ─── 2. Açılır Operatör Teklifleri Akordiyonu ───
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hotelGroup.operatorOffers.forEach { offer ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // 1. Satır: Operatör Rozeti + Gece Rozeti + Tarih Aralığı Rozeti
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // 💼 Operatör Rozeti (Gri Çerçeve)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("💼", fontSize = 10.sp)
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    offer.safeOperatorName.ifBlank { "Biblioglobus" },
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF334155)
                                                )
                                            }
                                        }

                                        // 🌙 Gece Rozeti (Açık Yeşil)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF0FDF4),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🌙", fontSize = 10.sp)
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "${offer.nights} Ночей",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }

                                        // 📅 Tarih Rozeti (Açık Mavi)
                                        val depDate = offer.departureDate?.ifBlank { "2026-10-01" } ?: "2026-10-01"
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFEFF6FF),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("📅", fontSize = 10.sp)
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "$depDate (вылет)",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E40AF)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // 2. Satır: Konum + Oda Tipi + Yemek Konsepti
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "📍 ${offer.region}, ${offer.country}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text("•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                        Text(
                                            "🛏️ ${offer.safeRoomType}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Text("•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                        Text(
                                            "🍽️ ${offer.safeMealType}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                // 3. Sağ: Kişi Sayısı + Fiyat + Yeşil "Забронировать ➔" Butonu
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${offer.adults} чел. • ${offer.nights}н. Всего",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            "${offer.price.toInt()} ${offer.currency}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF0F5A56)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Button(
                                        onClick = { onSelectOffer(offer) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF0F5A56),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = "Забронировать ➔",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
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
