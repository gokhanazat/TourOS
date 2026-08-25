package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

data class TourOSColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val cellContent: @Composable (T) -> Unit
)

/**
 * Responsive Adaptif Tablo Bileşeni.
 * - Expanded / Medium genişlikte: DataTable (sütunlu tablo)
 * - Compact genişlikte: Kart listesi (aynı veri, mobil odaklı sunum)
 */
@Composable
fun <T> TourOSDataTable(
    items: List<T>,
    columns: List<TourOSColumn<T>>,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    dense: Boolean = false,
    selectedItem: T? = null,
    onItemClick: ((T) -> Unit)? = null,
    compactCardContent: @Composable (T) -> Unit
) {
    if (isCompact) {
        // Compact: Kart Listesi
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(if (dense) TourOSSpacing.small else TourOSSpacing.medium),
            contentPadding = PaddingValues(vertical = TourOSSpacing.small)
        ) {
            items(items) { item ->
                TourOSCard(
                    onClick = if (onItemClick != null) { { onItemClick(item) } } else null,
                    backgroundColor = if (selectedItem == item) TourOSColors.PrimaryContainer else TourOSColors.Background
                ) {
                    compactCardContent(item)
                }
            }
        }
    } else {
        val hPadding = if (dense) 12.dp else TourOSSpacing.large
        val vPadding = if (dense) 6.dp else TourOSSpacing.medium
        val headerVPadding = if (dense) 8.dp else TourOSSpacing.medium
        val radius = if (dense) 8.dp else TourOSSpacing.cornerRadius

        // Expanded / Medium: Sütunlu Tablo
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = TourOSSpacing.borderWidth,
                    color = TourOSColors.Border,
                    shape = RoundedCornerShape(radius)
                )
                .background(TourOSColors.Background, RoundedCornerShape(radius))
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Surface)
                    .padding(horizontal = hPadding, vertical = headerVPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                columns.forEach { column ->
                    Box(modifier = Modifier.weight(column.weight)) {
                        Text(
                            text = column.title,
                            style = if (dense) {
                                TourOSTypography.Caption.copy(
                                    color = TourOSColors.TextSecondary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            } else {
                                TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)

            // Data Rows
            LazyColumn {
                items(items) { item ->
                    val isSelected = selectedItem == item
                    val rowBg = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Background

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .then(
                                if (onItemClick != null) Modifier.clickable { onItemClick(item) } else Modifier
                            )
                            .padding(horizontal = hPadding, vertical = vPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        columns.forEach { column ->
                            Box(modifier = Modifier.weight(column.weight)) {
                                column.cellContent(item)
                            }
                        }
                    }
                    HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                }
            }
        }
    }
}
