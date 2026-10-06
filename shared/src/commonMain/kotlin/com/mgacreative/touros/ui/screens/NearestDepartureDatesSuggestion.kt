package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel

/**
 * "Seçilen tarihlerde tur yok → en yakın kalkış tarihleri" öneri kutusu.
 * Acente (B2B) ve Web ekranlarının "sonuç bulunamadı" bölümünde ortak kullanılır.
 *
 * Kurallar:
 * - Sonuç listesine KARIŞTIRILMAZ; ayrı, açıkça etiketlenmiş bir öneri kutusudur.
 * - Bir tarihe tıklanınca arama çubuğundaki tarih de o güne güncellenir ve arama yeniden yapılır,
 *   böylece ekrandaki sonuçlar ile arama çubuğu her zaman birebir uyumlu kalır.
 *
 * @param isoDates  ViewModel'in önerdiği tarihler (yyyy-MM-dd, kronolojik)
 * @param onDateSelected seçilen tarih "dd.MM.yyyy" biçiminde döner
 */
@Composable
fun NearestDepartureDatesSuggestion(
    isoDates: List<String>,
    startDateText: String,
    endDateText: String,
    onDateSelected: (dotDate: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isoDates.isEmpty()) return

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val range = if (endDateText.isNotBlank() && endDateText != startDateText) "$startDateText – $endDateText" else startDateText

    val title = when (currentLanguage.code) {
        "ru" -> "На выбранные даты ($range) туров не найдено."
        "en" -> "No tours found for the selected dates ($range)."
        "de" -> "Für die gewählten Daten ($range) wurden keine Reisen gefunden."
        else -> "Seçtiğiniz tarihlerde ($range) tur bulunamadı."
    }
    val subtitle = when (currentLanguage.code) {
        "ru" -> "Ближайшие даты вылета по вашему запросу:"
        "en" -> "Nearest departure dates for your search:"
        "de" -> "Nächste Abflugtermine für Ihre Suche:"
        else -> "Aramanıza uyan en yakın kalkış tarihleri:"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0FDFA),
        border = BorderStroke(1.dp, Color(0xFF99F6E4))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color(0xFF475569),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                isoDates.forEach { iso ->
                    val dot = B2BTourSearchViewModel.isoToDot(iso)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F5A56),
                        modifier = Modifier.clickable { onDateSelected(dot) }
                    ) {
                        Text(
                            text = "📅 $dot",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
