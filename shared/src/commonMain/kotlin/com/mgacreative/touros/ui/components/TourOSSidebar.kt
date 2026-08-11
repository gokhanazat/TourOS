package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

data class TourOSNavGroup(
    val categoryTitle: String,
    val items: List<TourOSNavItem>
)

data class TourOSNavItem(
    val title: String,
    val route: Any,
    val icon: (@Composable () -> Unit)? = null,
    val isSelected: Boolean = false,
    val badgeCount: Int? = null
)

/**
 * Expanded (Masaüstü/Tablet Landscape) Genişliği için Sabit Sol Sidebar / NavigationRail.
 * Logo + Kategorize Gruplandırılmış Menü Listesi + Alt Kullanıcı Bilgi Kartı içerir.
 */
@Composable
fun TourOSSidebar(
    items: List<TourOSNavItem>,
    onItemSelect: (TourOSNavItem) -> Unit,
    modifier: Modifier = Modifier,
    groups: List<TourOSNavGroup>? = null,
    userName: String = "",
    userRole: String = "",
    onLogoutClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(TourOSColors.Surface)
            .border(
                width = TourOSSpacing.borderWidth,
                color = TourOSColors.Border
            )
    ) {
        // Logo Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(TourOSColors.Primary)
                .padding(horizontal = TourOSSpacing.large),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.Secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnSecondary)
                    )
                }
                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                Text(
                    text = "TourOS",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary)
                )
            }
        }

        HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)

        // Menu Items (Gruplandırılmış veya Tekil Liste)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = TourOSSpacing.small, horizontal = TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
        ) {
            if (groups != null && groups.isNotEmpty()) {
                groups.forEach { group ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = group.categoryTitle,
                        style = TourOSTypography.Caption.copy(
                            color = TourOSColors.TextSecondary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                    )

                    group.items.forEach { item ->
                        RenderSidebarItem(item = item, onItemSelect = onItemSelect)
                    }
                }
            } else {
                items.forEach { item ->
                    RenderSidebarItem(item = item, onItemSelect = onItemSelect)
                }
            }
        }

        HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)

        // Bottom User Card
        if (userName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TourOSSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TourOSColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        maxLines = 1
                    )
                    if (userRole.isNotBlank()) {
                        Text(
                            text = userRole,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                            maxLines = 1
                        )
                    }
                }

                if (onLogoutClick != null) {
                    IconButton(onClick = onLogoutClick) {
                        Text("🚪", style = TourOSTypography.TitleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderSidebarItem(
    item: TourOSNavItem,
    onItemSelect: (TourOSNavItem) -> Unit
) {
    val bg = if (item.isSelected) TourOSColors.PrimaryContainer else Color.Transparent
    val contentColor = if (item.isSelected) TourOSColors.Primary else TourOSColors.TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(bg)
            .clickable { onItemSelect(item) }
            .padding(horizontal = TourOSSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.icon != null) {
            item.icon.invoke()
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
        }
        Text(
            text = item.title,
            style = TourOSTypography.TitleMedium.copy(
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (item.isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            ),
            modifier = Modifier.weight(1f)
        )

        if (item.badgeCount != null && item.badgeCount > 0) {
            TourOSStatusBadge(
                text = item.badgeCount.toString(),
                backgroundColor = TourOSColors.SecondaryContainer,
                textColor = TourOSColors.Secondary
            )
        }
    }
}
