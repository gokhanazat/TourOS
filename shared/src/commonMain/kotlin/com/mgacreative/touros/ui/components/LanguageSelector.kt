package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.theme.TourOSTypography

enum class AppLanguage(val code: String, val flag: String, val label: String, val displayCode: String) {
    RU("ru", "🇷🇺", "Русский", "RU"),
    EN("en", "🇬🇧", "English", "ENG"),
    TR("tr", "🇹🇷", "Türkçe", "TR")
}

/**
 * Web, Desktop, Android ve iOS platformlarında birebir aynı tasarım diline sahip
 * 3 Dilli (RU - ENG - TR) Dil Seçim Bileşeni.
 */
@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage = AppLanguage.RU,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF0F172A),
    activeBgColor: Color = Color(0xFF0284C7),
    isPillStyle: Boolean = true
) {
    if (isPillStyle) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(2.dp)
        ) {
            AppLanguage.entries.forEach { language ->
                val isSelected = language == selectedLanguage
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) activeBgColor else Color.Transparent)
                        .clickable { onLanguageSelected(language) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = language.displayCode,
                        style = TourOSTypography.Caption.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else textColor.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            AppLanguage.entries.forEachIndexed { index, language ->
                val isSelected = language == selectedLanguage
                Text(
                    text = language.displayCode,
                    style = TourOSTypography.Caption.copy(
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) activeBgColor else textColor.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .clickable { onLanguageSelected(language) }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
                if (index < AppLanguage.entries.size - 1) {
                    Text(
                        text = "|",
                        style = TourOSTypography.Caption.copy(
                            color = textColor.copy(alpha = 0.3f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
