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
    val items: List<TourOSNavItem>,
    val isCollapsible: Boolean = true,
    val isInitiallyExpanded: Boolean = true
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
    onLogoutClick: (() -> Unit)? = null,
    onNavigateToWeb: (() -> Unit)? = null
) {
    val expandedGroups = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateMapOf<String, Boolean>() }

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
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "a",
                    style = TourOSTypography.DisplaySmall.copy(
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        fontSize = 34.sp
                    )
                )
                Text(
                    text = "xileto",
                    style = TourOSTypography.TitleLarge.copy(
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)

        // 🌐 Web'e Dön Butonu (Menü Üstü Kolay Geçiş)
        if (onNavigateToWeb != null) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToWeb() },
                color = TourOSColors.PrimaryContainer.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🌐", fontSize = 14.sp)
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Web Sayfasına Dön"),
                        style = TourOSTypography.BodyMedium.copy(
                            color = TourOSColors.Primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("➔", fontSize = 12.sp, color = TourOSColors.Primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)
        }

        // Menu Items (Gruplandırılmış veya Tekil Liste - Akıcı ve Katlanabilir Başlıklar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 6.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (groups != null && groups.isNotEmpty()) {
                groups.forEach { group ->
                    val hasActiveChild = group.items.any { it.isSelected }
                    val isExpanded = expandedGroups[group.categoryTitle] ?: (group.isInitiallyExpanded || hasActiveChild)

                    Spacer(modifier = Modifier.height(4.dp))
                    if (group.isCollapsible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (hasActiveChild) TourOSColors.PrimaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable {
                                    expandedGroups[group.categoryTitle] = !isExpanded
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = group.categoryTitle.uppercase(),
                                style = TourOSTypography.TitleMedium.copy(
                                    color = if (hasActiveChild) TourOSColors.Primary else TourOSColors.TextPrimary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.6.sp
                                )
                            )
                            Text(
                                text = if (isExpanded) "▾" else "▸",
                                style = TourOSTypography.TitleMedium.copy(
                                    color = if (hasActiveChild) TourOSColors.Primary else TourOSColors.TextSecondary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    } else {
                        Text(
                            text = group.categoryTitle.uppercase(),
                            style = TourOSTypography.TitleMedium.copy(
                                color = TourOSColors.Primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 0.6.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = !group.isCollapsible || isExpanded,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        ) {
                            group.items.forEach { item ->
                                RenderSidebarItem(item = item, onItemSelect = onItemSelect)
                            }
                        }
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
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary, fontSize = 12.sp),
                        maxLines = 1
                    )
                    if (userRole.isNotBlank()) {
                        Text(
                            text = userRole,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp),
                            maxLines = 1
                        )
                    }
                }

                if (onLogoutClick != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onLogoutClick,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Çıkış",
                            style = TourOSTypography.Caption.copy(
                                color = TourOSColors.Error,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
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
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onItemSelect(item) }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.icon != null) {
            item.icon.invoke()
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = item.title,
            style = TourOSTypography.BodyMedium.copy(
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (item.isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
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
