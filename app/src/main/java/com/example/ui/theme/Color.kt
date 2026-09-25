package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// NetGuard Cyber Dark Theme Palette
val CyberNavyDark = Color(0xFF070D19)
val CyberNavySurface = Color(0xFF0E172A)
val CyberNavyCard = Color(0xFF131F37)
val CyberNavyBorder = Color(0xFF1E2D4A)

// Accent and State Colors
val CyberCyan = Color(0xFF00F0FF)
val CyberCyanLight = Color(0xFF70F3FF)
val CyberBlue = Color(0xFF2563EB)
val CyberBlueLight = Color(0xFF38BDF8)

val StatusOnlineGreen = Color(0xFF10B981)
val StatusOfflineRed = Color(0xFFEF4444)
val StatusWarningAmber = Color(0xFFF59E0B)
val StatusRogueCrimson = Color(0xFFFF0055)

// Bandwidth Chart (Recharts / D3 styling)
val ChartRxCyan = Color(0xFF00E5FF)
val ChartRxCyanDim = Color(0x3300E5FF)
val ChartTxPurple = Color(0xFFC084FC)
val ChartTxPurpleDim = Color(0x33C084FC)
val ChartTxMagenta = Color(0xFFF43F5E)

// Text and Neutral Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val TerminalBackground = Color(0xFF030712)
val TerminalGreen = Color(0xFF22C55E)

// Enterprise Light Theme Palette (وضع عادي / نهاري)
val LightCanvasBackground = Color(0xFFF8FAFC)
val LightCanvasSurface = Color(0xFFFFFFFF)
val LightCanvasCard = Color(0xFFFFFFFF)
val LightCanvasBorder = Color(0xFFE2E8F0)
val LightTextDarkPrimary = Color(0xFF0F172A)
val LightTextDarkSecondary = Color(0xFF475569)
val LightTextDarkMuted = Color(0xFF64748B)

val LightAccentCyan = Color(0xFF0284C7)
val LightAccentCyanLight = Color(0xFFE0F2FE)
val LightAccentBlue = Color(0xFF2563EB)
val LightAccentBlueLight = Color(0xFFDBEAFE)

val LightStatusGreen = Color(0xFF059669)
val LightStatusRed = Color(0xFFDC2626)
val LightStatusAmber = Color(0xFFD97706)
val LightStatusCrimson = Color(0xFFE11D48)

/**
 * Responsive color helper that yields the right color token depending on active dark/light mode.
 */
data class NetGuardThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val statusGreen: Color,
    val statusRed: Color,
    val statusAmber: Color,
    val statusCrimson: Color
)

fun getNetGuardColors(isDark: Boolean): NetGuardThemeColors = if (isDark) {
    NetGuardThemeColors(
        isDark = true,
        background = CyberNavyDark,
        surface = CyberNavySurface,
        cardBackground = CyberNavyCard,
        cardBorder = CyberNavyBorder,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextMuted,
        primaryAccent = CyberCyan,
        secondaryAccent = CyberBlueLight,
        statusGreen = StatusOnlineGreen,
        statusRed = StatusOfflineRed,
        statusAmber = StatusWarningAmber,
        statusCrimson = StatusRogueCrimson
    )
} else {
    NetGuardThemeColors(
        isDark = false,
        background = LightCanvasBackground,
        surface = LightCanvasSurface,
        cardBackground = LightCanvasCard,
        cardBorder = LightCanvasBorder,
        textPrimary = LightTextDarkPrimary,
        textSecondary = LightTextDarkSecondary,
        textMuted = LightTextDarkMuted,
        primaryAccent = LightAccentCyan,
        secondaryAccent = LightAccentBlue,
        statusGreen = LightStatusGreen,
        statusRed = LightStatusRed,
        statusAmber = LightStatusAmber,
        statusCrimson = LightStatusCrimson
    )
}


