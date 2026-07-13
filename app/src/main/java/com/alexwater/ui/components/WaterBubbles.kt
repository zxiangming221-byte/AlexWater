package com.alexwater.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alexwater.ui.theme.Water
import kotlin.math.roundToInt

@Composable
fun WaterBubbles(modifier: Modifier = Modifier) {
    val bubbleSpecs = listOf(
        BubbleSpec(24.dp, 0.7f, 0f),
        BubbleSpec(16.dp, 0.5f, 1.2f),
        BubbleSpec(32.dp, 0.6f, 2.5f),
        BubbleSpec(20.dp, 0.45f, 4f),
        BubbleSpec(28.dp, 0.55f, 5.5f),
    )

    Box(modifier = modifier.pointerInput(Unit) { /* pass through */ }) {
        bubbleSpecs.forEach { spec ->
            BubbleItem(spec)
        }
    }
}

private data class BubbleSpec(val size: androidx.compose.ui.unit.Dp, val speed: Float, val delay: Float)

@Composable
private fun BubbleItem(spec: BubbleSpec) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_${spec.delay}")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween((4000 / spec.speed).toInt(), easing = LinearEasing, delayMillis = (spec.delay * 1000).toInt()),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_y_${spec.delay}"
    )
    val xWobble by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween((3000 / spec.speed).toInt(), easing = EaseInOutSine, delayMillis = (spec.delay * 1000).toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_x_${spec.delay}"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(xWobble.roundToInt(), yOffset.roundToInt()) }
            .size(spec.size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                    )
                )
            )
    )
}
