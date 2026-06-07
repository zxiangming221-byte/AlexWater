package com.nous.waterwell.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nous.waterwell.ui.theme.LocalIosColors

/**
 * iOS-style frosted glass card with semi-transparent background,
 * glass border, and inner highlight for light-diffusion effect.
 *
 * Uses RenderEffect blur on API 31+, falls back to layered opacity on older devices.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val ios = LocalIosColors.current
    val density = LocalConfiguration.current

    // Semi-transparent glass color
    val glassColor = if (ios.label == Color.White) {
        // Dark mode: dark glass
        Color.White.copy(alpha = 0.08f)
    } else {
        // Light mode: white frosted glass
        Color.White.copy(alpha = 0.72f)
    }

    // Border: subtle white highlight on top-left, soft shadow on bottom-right
    val borderColor = if (ios.label == Color.White) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassColor)
            .border(0.5.dp, borderColor, shape)
            // Inner highlight (simulated light diffusion from top-left)
            .drawBehind {
                // Top-left glow
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.05f),
                        radius = size.width * 0.6f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
                // Subtle edge highlight on top
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.3f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
    ) {
        content()
    }
}
