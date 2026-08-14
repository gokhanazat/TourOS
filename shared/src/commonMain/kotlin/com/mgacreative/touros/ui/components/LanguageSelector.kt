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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(TourOSColors.PrimaryContainer)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = selectedLanguage.flag,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedLanguage.code.uppercase(),
                style = TourOSTypography.Caption.copy(
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "▾",
                fontSize = 12.sp,
                color = TourOSColors.TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(TourOSColors.Surface)
        ) {
            AppLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = language.flag, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${language.label} (${language.code.uppercase()})",
                                style = TourOSTypography.BodyMedium.copy(
                                    fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (language == selectedLanguage) TourOSColors.Primary else TourOSColors.TextPrimary
                                )
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onLanguageSelected(language)
                    }
                )
            }
        }
    }
}
