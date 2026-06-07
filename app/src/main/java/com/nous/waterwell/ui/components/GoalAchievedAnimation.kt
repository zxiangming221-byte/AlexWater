package com.nous.waterwell.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.nous.waterwell.ui.theme.ProgressFull
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Smooth handwriting-style "Goal Achieved" animation.
 * A glowing dot sweeps from left to right revealing the text,
 * with particle trails behind it.
 */
@Composable
fun GoalAchievedAnimation(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val text = "Goal Achieved!"
    val textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ProgressFull)
    val textLayout = remember(text) { textMeasurer.measure(text = AnnotatedString(text), style = textStyle) }
    val textWidth = textLayout.size.width.toFloat()
    val textHeight = textLayout.size.height.toFloat()

    // Main sweep animation: 0f → 1f, the drawing progress
    val sweepProgress by rememberInfiniteTransition(label = "sweep").animateFloat(
        initialValue = 0f, targetValue = 1.25f,  // 1.25 so there's a pause at the end
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sweep"
    )

    // The leading dot offset (left-to-right)
    val clampedProgress = sweepProgress.coerceIn(0f, 1f)
    val dotX = clampedProgress * textWidth

    // Dot pulse
    val dotRadius = if (clampedProgress < 0.02f) clampedProgress / 0.02f * 6f
        else 6f + sin(sweepProgress * 20f).toFloat() * 2f

    // Particle state
    val particles = remember { List(40) { ParticleState() } }

    Box(modifier = modifier.height(textHeight.dp + 40.dp).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val startX = (canvasW - textWidth) / 2f
            val startY = (canvasH - textHeight) / 2f

            // ── Glow trail behind the dot ──
            val trailLength = textWidth * 0.35f
            val trailStart = (dotX - trailLength).coerceAtLeast(0f)
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    trailStart / textWidth to ProgressFull.copy(alpha = 0.0f),
                    (trailStart + trailLength * 0.5f) / textWidth to ProgressFull.copy(alpha = 0.15f),
                    dotX / textWidth to ProgressFull.copy(alpha = 0.5f),
                    startX = startX, endX = startX + textWidth
                ),
                topLeft = Offset(startX, startY - 12f),
                size = Size(textWidth, textHeight + 24f)
            )

            // ── Revealed text (clipped by sweep progress) ──
            val clipRight = startX + dotX
            if (clipRight > startX) {
                // Draw the revealed portion of text
                // We use saveLayer + clipRect to only show up to the dot position
                drawContext.canvas.saveLayer(
                    Rect(startX, 0f, clipRight, canvasH),
                    Paint().apply { alpha = 1f }
                )
                // Draw text
                textLayout.let { layout ->
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(startX, startY),
                        color = ProgressFull
                    )
                }
                drawContext.canvas.restore()

                // Subtle glow via translucent offset copy
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(startX + 1f, startY + 1f),
                    color = ProgressFull.copy(alpha = 0.25f)
                )
            }

            // ── Leading dot ──
            val dotCenter = Offset(startX + dotX, startY + textHeight / 2f)
            // Outer glow
            drawCircle(
                color = ProgressFull.copy(alpha = 0.3f),
                radius = dotRadius * 3f,
                center = dotCenter
            )
            drawCircle(
                color = ProgressFull.copy(alpha = 0.5f),
                radius = dotRadius * 1.8f,
                center = dotCenter
            )
            // Core dot
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White, ProgressFull),
                    center = dotCenter,
                    radius = dotRadius
                ),
                radius = dotRadius,
                center = dotCenter
            )

            // ── Particle trails ──
            particles.forEachIndexed { i, p ->
                val particleProgress = (clampedProgress - p.delay).coerceIn(0f, 1f)
                if (particleProgress <= 0f) return@forEachIndexed

                val px = startX + particleProgress * textWidth
                val py = startY + textHeight / 2f + p.offsetY
                val life = (1f - particleProgress).coerceAtLeast(0f)
                val alpha = (life * p.alpha).coerceAtLeast(0f)

                drawCircle(
                    color = ProgressFull.copy(alpha = alpha),
                    radius = p.radius * life,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private class ParticleState {
    val delay: Float = Random.nextFloat() * 0.8f
    val offsetY: Float = (Random.nextFloat() - 0.5f) * 40f
    val radius: Float = 1.5f + Random.nextFloat() * 3f
    val alpha: Float = 0.3f + Random.nextFloat() * 0.5f
}
