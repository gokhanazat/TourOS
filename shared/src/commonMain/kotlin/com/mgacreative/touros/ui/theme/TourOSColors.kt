package com.mgacreative.touros.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * TourOS Kurumsal Renk Paleti (Sabit Hex Değerleri)
 * Sadece Light Mode desteklenir.
 */
@Immutable
object TourOSColors {
    // Brand / Primary
    val Primary = Color(0xFF1F4E5F)            // Koyu petrol lacivert
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE7EEF0)   // Seçili/aktif satır arka planı
    val OnPrimaryContainer = Color(0xFF1F4E5F)

    // Secondary / Accent
    val Secondary = Color(0xFFC97A2B)          // Sıcak amber (Önemli CTA)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFDF6EE)
    val OnSecondaryContainer = Color(0xFFC97A2B)

    // Surface & Background
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF7F8F9)
    val Border = Color(0xFFE4E7EB)
    val Divider = Color(0xFFE4E7EB)

    // Typography / Neutral
    val TextPrimary = Color(0xFF1F2328)
    val TextSecondary = Color(0xFF6B7280)
    val TextDisabled = Color(0xFFADB5BD)

    // Status Colors
    val Success = Color(0xFF2E7D5B)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFE8F5E9)

    val Warning = Color(0xFFB98900)
    val OnWarning = Color(0xFFFFFFFF)
    val WarningContainer = Color(0xFFFFF8E1)

    val Error = Color(0xFFC0392B)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFEBEE)

    val Info = Color(0xFF2E6FA3)
    val OnInfo = Color(0xFFFFFFFF)
    val InfoContainer = Color(0xFFE3F2FD)

    // Rezervasyon Durum Renkleri
    val StatusPending = Warning         // Bekliyor
    val StatusOption = Info            // Opsiyon
    val StatusConfirmed = Success      // Onaylandı
    val StatusCancelled = Error        // İptal
    val StatusCompleted = TextSecondary// Tamamlandı (nötr gri)
}
