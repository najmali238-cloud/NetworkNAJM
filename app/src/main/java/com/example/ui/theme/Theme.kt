package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage

val LocalThemeIsDark = compositionLocalOf { true }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ARABIC }

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF030712),
    primaryContainer = Color(0xFF0C2A4A),
    onPrimaryContainer = CyberCyanLight,
    secondary = CyberBlueLight,
    onSecondary = Color(0xFF030712),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFBFDBFE),
    tertiary = StatusOnlineGreen,
    onTertiary = Color(0xFF030712),
    error = StatusOfflineRed,
    onError = Color.White,
    background = CyberNavyDark,
    onBackground = TextPrimary,
    surface = CyberNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = CyberNavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentCyan,
    onPrimary = Color.White,
    primaryContainer = LightAccentCyanLight,
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LightAccentBlue,
    onSecondary = Color.White,
    secondaryContainer = LightAccentBlueLight,
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = LightStatusGreen,
    onTertiary = Color.White,
    error = LightStatusRed,
    onError = Color.White,
    background = LightCanvasBackground,
    onBackground = LightTextDarkPrimary,
    surface = LightCanvasSurface,
    onSurface = LightTextDarkPrimary,
    surfaceVariant = LightCanvasCard,
    onSurfaceVariant = LightTextDarkSecondary,
    outline = LightCanvasBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    language: AppLanguage = AppLanguage.ARABIC,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalThemeIsDark provides darkTheme,
        LocalAppLanguage provides language,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


