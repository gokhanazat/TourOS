package com.mgacreative.touros.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import kotlinx.coroutines.launch

/**
 * 🌟 TourOS Evrensel Dikey Kaydırma Çubuğu (ScrollState için)
 * Web, Masaüstü, Android ve iOS platformlarında fare ile tutulup kaydırılabilir.
 */
@Composable
fun TourOSVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 6.dp,
    expandedThickness: Dp = 10.dp,
    trackColor: Color = TourOSColors.Border.copy(alpha = 0.35f),
    thumbColor: Color = TourOSColors.Primary.copy(alpha = 0.65f),
    thumbHoverColor: Color = TourOSColors.Primary
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var trackHeightPx by remember { mutableStateOf(0f) }

    val currentThickness by animateDpAsState(
        targetValue = if (isHovered) expandedThickness else thickness,
        animationSpec = tween(150)
    )
    val animatedThumbColor by animateColorAsState(
        targetValue = if (isHovered) thumbHoverColor else thumbColor,
        animationSpec = tween(150)
    )

    val maxScroll = scrollState.maxValue.toFloat()
    if (maxScroll <= 0f) return

    val visibleRatio = (trackHeightPx / (trackHeightPx + maxScroll)).coerceIn(0.1f, 1f)
    val thumbHeightPx = (trackHeightPx * visibleRatio).coerceAtLeast(36f)
    val maxThumbOffset = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
    val thumbOffsetPx = if (maxScroll > 0f) {
        (scrollState.value.toFloat() / maxScroll) * maxThumbOffset
    } else 0f

    Box(
        modifier = modifier
            .width(expandedThickness)
            .fillMaxHeight()
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .hoverable(interactionSource)
            .pointerInput(maxScroll, maxThumbOffset, trackHeightPx) {
                detectTapGestures { offset ->
                    if (trackHeightPx > 0 && maxThumbOffset > 0) {
                        val targetRatio = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                        coroutineScope.launch {
                            scrollState.scrollTo((targetRatio * maxScroll).toInt())
                        }
                    }
                }
            }
            .pointerInput(maxScroll, maxThumbOffset) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (maxThumbOffset > 0) {
                        val deltaRatio = dragAmount.y / maxThumbOffset
                        val newScroll = (scrollState.value + (deltaRatio * maxScroll).toInt()).coerceIn(0, scrollState.maxValue)
                        coroutineScope.launch {
                            scrollState.scrollTo(newScroll)
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopEnd
    ) {
        // Arka Plan İzi (Track)
        Box(
            modifier = Modifier
                .width(currentThickness)
                .fillMaxHeight()
                .clip(RoundedCornerShape(currentThickness / 2))
                .background(trackColor)
        )

        // Tutulup Kaydırılan Başlık (Thumb)
        Box(
            modifier = Modifier
                .offset(y = with(androidx.compose.ui.platform.LocalDensity.current) { thumbOffsetPx.toDp() })
                .width(currentThickness)
                .height(with(androidx.compose.ui.platform.LocalDensity.current) { thumbHeightPx.toDp() })
                .clip(RoundedCornerShape(currentThickness / 2))
                .background(animatedThumbColor)
        )
    }
}

/**
 * 🌟 TourOS Evrensel Dikey Kaydırma Çubuğu (LazyListState için)
 * Web, Masaüstü, Android ve iOS platformlarında LazyColumn ile senkronize çalışır.
 */
@Composable
fun TourOSLazyListVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 6.dp,
    expandedThickness: Dp = 10.dp,
    trackColor: Color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
    thumbColor: Color = Color(0xFF0F5A56).copy(alpha = 0.55f),
    thumbHoverColor: Color = Color(0xFF0F5A56)
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var trackHeightPx by remember { mutableStateOf(0f) }

    val currentThickness by animateDpAsState(
        targetValue = if (isHovered) expandedThickness else thickness,
        animationSpec = tween(150)
    )
    val animatedThumbColor by animateColorAsState(
        targetValue = if (isHovered) thumbHoverColor else thumbColor,
        animationSpec = tween(150)
    )

    val layoutInfo = listState.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val visibleItemsCount = layoutInfo.visibleItemsInfo.size

    if (totalItemsCount <= 0 || visibleItemsCount >= totalItemsCount) return

    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

    val estimatedTotalHeight = totalItemsCount.toFloat()
    val visibleRatio = (visibleItemsCount.toFloat() / totalItemsCount.toFloat()).coerceIn(0.08f, 1f)
    val thumbHeightPx = (trackHeightPx * visibleRatio).coerceAtLeast(36f)
    val maxThumbOffset = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)

    val currentProgress = ((firstVisibleItemIndex.toFloat() + (if (layoutInfo.visibleItemsInfo.isNotEmpty()) firstVisibleItemScrollOffset.toFloat() / layoutInfo.visibleItemsInfo.first().size.coerceAtLeast(1) else 0f)) / (totalItemsCount - visibleItemsCount).coerceAtLeast(1)).coerceIn(0f, 1f)
    val thumbOffsetPx = currentProgress * maxThumbOffset

    Box(
        modifier = modifier
            .width(expandedThickness)
            .fillMaxHeight()
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .hoverable(interactionSource)
            .pointerInput(totalItemsCount, trackHeightPx) {
                detectTapGestures { offset ->
                    if (trackHeightPx > 0) {
                        val targetRatio = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                        val targetIndex = (targetRatio * totalItemsCount).toInt().coerceIn(0, totalItemsCount - 1)
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
            }
            .pointerInput(totalItemsCount, maxThumbOffset) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (maxThumbOffset > 0) {
                        val deltaRatio = dragAmount.y / maxThumbOffset
                        val newProgress = (currentProgress + deltaRatio).coerceIn(0f, 1f)
                        val targetIndex = (newProgress * (totalItemsCount - visibleItemsCount)).toInt().coerceIn(0, totalItemsCount - 1)
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopEnd
    ) {
        // Arka Plan İzi (Track)
        Box(
            modifier = Modifier
                .width(currentThickness)
                .fillMaxHeight()
                .clip(RoundedCornerShape(currentThickness / 2))
                .background(trackColor)
        )

        // Tutulup Kaydırılan Başlık (Thumb)
        Box(
            modifier = Modifier
                .offset(y = with(androidx.compose.ui.platform.LocalDensity.current) { thumbOffsetPx.toDp() })
                .width(currentThickness)
                .height(with(androidx.compose.ui.platform.LocalDensity.current) { thumbHeightPx.toDp() })
                .clip(RoundedCornerShape(currentThickness / 2))
                .background(animatedThumbColor)
        )
    }
}
