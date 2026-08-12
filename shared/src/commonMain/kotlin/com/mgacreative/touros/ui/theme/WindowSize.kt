package com.mgacreative.touros.ui.theme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * Adaptive UI yardımcıları.
 * 
 * Breakpoint'ler (V2 API — isWidthAtLeastBreakpoint):
 * - Compact  : < 600dp  → Telefon (portrait)
 * - Medium   : 600-839dp → Tablet (portrait), foldable
 * - Expanded : ≥ 840dp  → Tablet (landscape), masaüstü
 */

/** Ortak breakpoint değerleri */
private const val MEDIUM_WIDTH_BREAKPOINT = 600
private const val EXPANDED_WIDTH_BREAKPOINT = 840

/**
 * Ekran genişlik sınıfı (maxWidth Dp cinsinden).
 */
enum class WindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

fun getWindowWidthClass(width: Dp): WindowWidthClass {
    return when {
        width >= EXPANDED_WIDTH_BREAKPOINT.dp -> WindowWidthClass.EXPANDED
        width >= MEDIUM_WIDTH_BREAKPOINT.dp -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.COMPACT
    }
}

/**
 * Navigasyon tipini belirler.
 */
enum class NavigationType {
    BOTTOM_NAVIGATION,           // Compact (telefon)
    NAVIGATION_RAIL,             // Medium (tablet)
    PERMANENT_NAVIGATION_DRAWER  // Expanded (masaüstü)
}

/**
 * WindowSizeClass'a göre uygun NavigationType döndürür.
 * V2 API: isWidthAtLeastBreakpoint kullanır.
 */
@Suppress("DEPRECATION")
@Composable
fun calculateNavigationType(): NavigationType {
    val windowInfo = currentWindowAdaptiveInfo()
    val sizeClass = windowInfo.windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(EXPANDED_WIDTH_BREAKPOINT) ->
            NavigationType.PERMANENT_NAVIGATION_DRAWER
        sizeClass.isWidthAtLeastBreakpoint(MEDIUM_WIDTH_BREAKPOINT) ->
            NavigationType.NAVIGATION_RAIL
        else ->
            NavigationType.BOTTOM_NAVIGATION
    }
}

/**
 * İçerik düzeni tipini belirler.
 */
enum class ContentType {
    SINGLE_PANE,  // Compact
    DUAL_PANE     // Medium + Expanded
}

@Suppress("DEPRECATION")
@Composable
fun calculateContentType(): ContentType {
    val windowInfo = currentWindowAdaptiveInfo()
    val sizeClass = windowInfo.windowSizeClass
    return if (sizeClass.isWidthAtLeastBreakpoint(MEDIUM_WIDTH_BREAKPOINT)) {
        ContentType.DUAL_PANE
    } else {
        ContentType.SINGLE_PANE
    }
}
