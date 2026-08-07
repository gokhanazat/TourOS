package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@Composable
fun TourOSBottomBar(
    items: List<TourOSNavItem>,
    onItemSelect: (TourOSNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(TourOSColors.Background),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val color = if (item.isSelected) TourOSColors.Primary else TourOSColors.TextSecondary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelect(item) }
                        .padding(vertical = TourOSSpacing.xSmall),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item.icon()
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        style = TourOSTypography.Caption.copy(color = color),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
