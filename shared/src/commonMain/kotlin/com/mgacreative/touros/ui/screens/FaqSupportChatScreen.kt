package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.faq.ChatMessage
import com.mgacreative.touros.domain.model.faq.ChatSender
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.FaqSupportChatViewModel

/**
 * SSS Destek Asistanı Ekranı — TourOS 0.3
 *
 * Klasik sohbet balonu düzeni (Kullanıcı sağda Primary Container, Asistan solda Surface).
 * Alt kısımda sabit mesaj giriş çubuğu.
 */
@Composable
fun FaqSupportChatScreen(
    viewModel: FaqSupportChatViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val quickQuestions = remember {
        listOf(
            "👤 Canlı Temsilciye Bağlan",
            "🎫 Bilet / Voucher Nereden Alınır?",
            "📅 Tur İptal Koşulları Nedir?",
            "📍 Buluşma Noktası Neresi?",
            "💳 Ödeme Seçenekleri?"
        )
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "SSS Destek Asistanı",
                subtitle = "7/24 Yapay zeka destek ve canlı acente asistanı",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        },
        bottomBar = {
            // ── ALT KISIMDA SABİT MESAJ GİRİŞ ÇUBUĞU (Strict Rule) ─────────────
            StickyBottomInputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendUserQuery(inputText)
                        inputText = ""
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 1. KLASİK SOHBET BALONU AKIŞ LİSTESİ (Strict Rule) ─────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                contentPadding = PaddingValues(vertical = TourOSSpacing.medium)
            ) {
                items(uiState.messages) { msg ->
                    ClassicChatBubbleItem(message = msg)
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = TourOSColors.Primary
                            )
                            Text(
                                "🤖 Asistan yanıt hazırlıyor...",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                }
            }

            // ── 2. HIZLI SORU ÇİPLERİ ŞERİDİ ─────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                items(quickQuestions) { q ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            if (q.contains("Temsilciye Bağlan")) {
                                viewModel.requestHumanOperator()
                            } else {
                                viewModel.sendUserQuery(q)
                            }
                        },
                        label = {
                            Text(q, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

// ─── KLASİK SOHBET BALONU İTEMİ (Kullanıcı Sağda PrimaryContainer, Asistan Solda Surface) ─

@Composable
private fun ClassicChatBubbleItem(message: ChatMessage) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    // KULLANICI SAĞDA PRIMARY CONTAINER, ASİSTAN SOLDA SURFACE (Strict Rule)
    val bubbleBg = if (isUser) TourOSColors.PrimaryContainer else TourOSColors.Surface
    val textColor = if (isUser) TourOSColors.Primary else TourOSColors.TextPrimary
    val senderLabel = if (isUser) "Siz (Kullanıcı)" else "🤖 TourOS Yapay Zeka Asistanı"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = senderLabel,
            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        TourOSCard(
            modifier = Modifier.widthIn(max = 310.dp),
            backgroundColor = bubbleBg,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = message.content,
                    style = TourOSTypography.BodyMedium.copy(color = textColor)
                )

                Text(
                    text = "18:24",
                    style = TourOSTypography.Caption.copy(
                        color = if (isUser) TourOSColors.Primary.copy(alpha = 0.7f) else TourOSColors.TextSecondary
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ─── ALT KISIMDA SABİT MESAJ GİRİŞ ÇUBUĞU BİLEŞENİ ────────────────────────────

@Composable
private fun StickyBottomInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = TourOSColors.Surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = "Mesajınızı yazın veya soru sorun...",
                modifier = Modifier.weight(1f)
            )

            TourOSButton(
                text = "Gönder ➔",
                onClick = onSend,
                enabled = inputText.isNotBlank(),
                variant = TourOSButtonVariant.PRIMARY
            )
        }
    }
}
