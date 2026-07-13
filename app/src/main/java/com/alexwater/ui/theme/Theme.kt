package com.alexwater.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Water,
    onPrimary = DarkFg,
    primaryContainer = WaterDark,
    onPrimaryContainer = DarkFg,
    secondary = WaterLight,
    onSecondary = DarkBg,
    background = DarkBg,
    onBackground = DarkFg,
    surface = DarkSurface,
    onSurface = DarkFg,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkFg2,
    outline = DarkBorder,
    outlineVariant = DarkBorderSoft,
    error = Danger,
    onError = DarkFg,
)

private val LightColorScheme = lightColorScheme(
    primary = LightWater,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF42A5F5),
    onSecondary = Color.White,
    background = Color(0xFFF0F4F8),
    onBackground = Color(0xFF263238),
    surface = Color(0xFFFAFBFC),
    onSurface = Color(0xFF37474F),
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = Color(0xFF607D8B),
    outline = Color(0xFFCFD8DC),
    outlineVariant = Color(0xFFECEFF1),
    error = Color(0xFFE53935),
    onError = Color.White,
)

@Composable
fun AlexWaterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 逐色过渡动画 — 比 Crossfade 轻量，不重复渲染
    val animatedScheme = animateColorScheme(targetScheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = animatedScheme.background.toArgb()
            window.navigationBarColor = animatedScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = AlexWaterTypography,
        shapes = AlexWaterShapes,
        content = content,
    )
}

/** 为所有颜色添加过渡动画（250ms），避免 Crossfade 双重渲染 */
@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    val primary by animateColorAsState(target.primary, tween(250), "p")
    val onPrimary by animateColorAsState(target.onPrimary, tween(250), "op")
    val primaryContainer by animateColorAsState(target.primaryContainer, tween(250), "pc")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, tween(250), "opc")
    val secondary by animateColorAsState(target.secondary, tween(250), "s")
    val onSecondary by animateColorAsState(target.onSecondary, tween(250), "os")
    val background by animateColorAsState(target.background, tween(250), "bg")
    val onBackground by animateColorAsState(target.onBackground, tween(250), "obg")
    val surface by animateColorAsState(target.surface, tween(250), "sf")
    val onSurface by animateColorAsState(target.onSurface, tween(250), "osf")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, tween(250), "sv")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, tween(250), "osv")
    val outline by animateColorAsState(target.outline, tween(250), "ol")
    val outlineVariant by animateColorAsState(target.outlineVariant, tween(250), "ov")
    return target.copy(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
        secondary = secondary, onSecondary = onSecondary,
        background = background, onBackground = onBackground,
        surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
        outline = outline, outlineVariant = outlineVariant,
    )
}
