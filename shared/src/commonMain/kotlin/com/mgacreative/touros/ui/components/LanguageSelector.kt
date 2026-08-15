package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography


enum class AppLanguage(val code: String, val flag: String, val label: String) {
    TR("tr", "🇹🇷", "Türkçe"),
    EN("en", "🇬🇧", "English"),
    RU("ru", "🇷🇺", "Русский")
}

/**
 * Web, Desktop, Android ve iOS platformlarında birebir aynı tasarım diline sahip
 * 3 Dilli (TR, EN, RU) Bayraklı Dil Seçim Bileşeni.
 */
@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage = AppLanguage.TR,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AppLanguage.values().forEachIndexed { index, language ->
            val isSelected = language == selectedLanguage
            Text(
                text = language.code.uppercase(),
                style = TourOSTypography.Caption.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) textColor else textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            if (index < AppLanguage.values().size - 1) {
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
