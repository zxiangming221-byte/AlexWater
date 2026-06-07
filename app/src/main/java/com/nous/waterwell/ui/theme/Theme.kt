package com.nous.waterwell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.nous.waterwell.data.AppLanguage
import com.nous.waterwell.data.AppStrings

data class IosColors(
    val background: Color, val surface: Color, val secondaryBg: Color,
    val label: Color, val secondaryLabel: Color, val tertiaryLabel: Color,
    val placeholder: Color, val separator: Color, val opaqueSeparator: Color,
    val blue: Color, val blueLight: Color, val green: Color,
    val orange: Color, val red: Color, val progressEmpty: Color
)

private val LightIos = IosColors(
    background = Color(0xFFF2F2F7), surface = Color(0xFFFFFFFF),
    secondaryBg = Color(0xFFF9F9F9), label = Color(0xFF000000),
    secondaryLabel = Color(0xFF3C3C43).copy(alpha = 0.6f),
    tertiaryLabel = Color(0xFF3C3C43).copy(alpha = 0.3f),
    placeholder = Color(0xFF3C3C43).copy(alpha = 0.25f),
    separator = Color(0xFF3C3C43).copy(alpha = 0.2f), opaqueSeparator = Color(0xFFC6C6C8),
    blue = Color(0xFF007AFF), blueLight = Color(0xFFE8F2FF), green = Color(0xFF34C759),
    orange = Color(0xFFFF9500), red = Color(0xFFFF3B30), progressEmpty = Color(0xFFE5E5EA)
)

private val DarkIos = IosColors(
    background = Color(0xFF000000), surface = Color(0xFF1C1C1E),
    secondaryBg = Color(0xFF2C2C2E), label = Color(0xFFFFFFFF),
    secondaryLabel = Color(0xFFEBEBF5).copy(alpha = 0.6f),
    tertiaryLabel = Color(0xFFEBEBF5).copy(alpha = 0.3f),
    placeholder = Color(0xFFEBEBF5).copy(alpha = 0.25f),
    separator = Color(0xFF545458).copy(alpha = 0.6f), opaqueSeparator = Color(0xFF38383A),
    blue = Color(0xFF0A84FF), blueLight = Color(0xFF1A3A5C), green = Color(0xFF30D158),
    orange = Color(0xFFFF9F0A), red = Color(0xFFFF453A), progressEmpty = Color(0xFF3A3A3C)
)

val LocalIosColors = staticCompositionLocalOf { LightIos }
val LocalStrings = staticCompositionLocalOf { AppStrings.ZH_CN }

data class AccentColors(val primary: Color, val label: String)

val AccentPresets = listOf(
    AccentColors(Color(0xFF007AFF), "iOS Blue"), AccentColors(Color(0xFFA680DE), "Lavender"),
    AccentColors(Color(0xFF34C759), "Mint"), AccentColors(Color(0xFFFF9500), "Warm Orange"),
    AccentColors(Color(0xFFFF3B30), "Coral"), AccentColors(Color(0xFFAF52DE), "Violet"),
    AccentColors(Color(0xFF30B0C7), "Teal"), AccentColors(Color(0xFFFFD60A), "Sunshine")
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent, onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.12f), onPrimaryContainer = accent,
    secondary = accent, onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.12f), onSecondaryContainer = accent,
    tertiary = LightIos.orange, background = LightIos.background,
    onBackground = LightIos.label, surface = LightIos.surface,
    onSurface = LightIos.label, surfaceVariant = LightIos.secondaryBg,
    onSurfaceVariant = LightIos.secondaryLabel, outline = LightIos.separator,
    outlineVariant = LightIos.opaqueSeparator, error = LightIos.red, onError = Color.White
)

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = accent, onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.2f), onPrimaryContainer = accent,
    secondary = accent, onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.2f), onSecondaryContainer = accent,
    tertiary = DarkIos.orange, background = DarkIos.background,
    onBackground = DarkIos.label, surface = DarkIos.surface,
    onSurface = DarkIos.label, surfaceVariant = DarkIos.secondaryBg,
    onSurfaceVariant = DarkIos.secondaryLabel, outline = DarkIos.separator,
    outlineVariant = DarkIos.opaqueSeparator, error = DarkIos.red, onError = Color.White
)

@Composable
fun AlexWaterTheme(
    darkTheme: Boolean = false,
    accentColor: Color = Color(0xFF007AFF),
    language: String = "zh-CN",
    content: @Composable () -> Unit
) {
    val ios = (if (darkTheme) DarkIos else LightIos).copy(blue = accentColor, blueLight = accentColor.copy(alpha = 0.12f))
    val scheme = if (darkTheme) darkScheme(accentColor) else lightScheme(accentColor)
    val strings = AppStrings.forLanguage(AppLanguage.fromCode(language))

    CompositionLocalProvider(LocalIosColors provides ios, LocalStrings provides strings) {
        MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
    }
}
