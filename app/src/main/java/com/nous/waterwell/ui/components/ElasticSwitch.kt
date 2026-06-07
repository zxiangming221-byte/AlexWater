package com.nous.waterwell.ui.components
import kotlin.math.sin

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.waterwell.ui.theme.LocalIosColors

/**
 * iOS-style elastic toggle with spring bounce + rainbow shimmer on tap.
 */
@Composable
fun ElasticSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trackWidth: Dp = 52.dp,
    trackHeight: Dp = 32.dp
) {
    val ios = LocalIosColors.current

    // Spring animation for the thumb position
    val thumbTarget = if (checked) 1f else 0f
    val thumbAnim = remember { Animatable(thumbTarget) }
    var showRainbow by remember { mutableStateOf(false) }

    LaunchedEffect(checked) {
        showRainbow = true
        thumbAnim.animateTo(
            thumbTarget,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = 500f
            )
        )
    }

    // Rainbow shimmer timer
    LaunchedEffect(showRainbow) {
        if (showRainbow) {
            kotlinx.coroutines.delay(600)
            showRainbow = false
        }
    }

    // Track colors
    val trackColor by animateColorAsState(
        targetValue = if (checked) ios.blue else Color.Gray.copy(alpha = 0.25f),
        animationSpec = tween(300), label = "track"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(color = ios.label),
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        // Custom switch track + thumb
        val thumbValue by thumbAnim.asState()
        val thumbOffset = thumbValue * (trackWidth.value - trackHeight.value)

        Box(
            modifier = Modifier
                .size(trackWidth, trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .graphicsLayer {
                    // Subtle scale bounce on track
                    val bounceExtra = if (showRainbow) {
                        // Quick pulse during rainbow
                        sin(thumbValue * 6.28f).toFloat() * 0.02f
                    } else 0f
                    scaleX = 1f + bounceExtra
                    scaleY = 1f + bounceExtra
                }
        ) {
            // Track background
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = trackColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                )

                // Rainbow shimmer effect
                if (showRainbow) {
                    val shimmerProgress = (System.currentTimeMillis() % 600) / 600f
                    val shimmerX = size.width * 0.3f + shimmerProgress * size.width * 0.4f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(shimmerX, size.height / 2),
                            radius = size.height * 0.8f
                        ),
                        radius = size.height * 0.8f,
                        center = Offset(shimmerX, size.height / 2)
                    )
                    // Tiny rainbow particles
                    for (i in 0..4) {
                        val px = shimmerX + (i - 2) * 8f
                        val py = size.height / 2 + (i % 2 * 2f - 1f) * 6f
                        val hue = (shimmerProgress * 360 + i * 60) % 360
                        drawCircle(
                            color = hslToColorS(hue, 0.7f, 0.6f).copy(alpha = 0.5f),
                            radius = 2f,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Thumb with spring bounce
            val thumbSpringOffset by animateDpAsState(
                targetValue = (thumbOffset).dp,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                label = "thumbX"
            )

            Box(
                modifier = Modifier
                    .offset(x = (thumbValue * (trackWidth.value - trackHeight.value)).dp)
                    .padding(2.dp)
                    .size(trackHeight - 4.dp)
                    .graphicsLayer {
                        val thumbBounce = if (showRainbow) {
                            sin(thumbValue * 6.28f + 1.5f).toFloat() * 0.08f
                        } else 0f
                        scaleX = 1f + thumbBounce
                        scaleY = 1f + thumbBounce
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Thumb shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = size.minDimension / 2 + 1f,
                        center = Offset(size.width / 2 + 1f, size.height / 2 + 1.5f)
                    )
                    // Thumb body
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
            }
        }
    }
}

private fun hslToColorS(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60 -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}
