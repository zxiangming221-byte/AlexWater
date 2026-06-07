package com.nous.waterwell.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nous.waterwell.ui.theme.*

@Composable
fun WaterProgressRing(
    progress: Float,
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 18.dp
) {
    val ios = LocalIosColors.current
    val animatedProgress by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(800), label = "progress")

    // Cache the gradient brush — avoids allocation every frame
    val progressBrush = remember(ios.blue, ios.green) {
        Brush.sweepGradient(listOf(ios.blue, ios.green, ios.blue))
    }
    val textColor by remember(animatedProgress) {
        derivedStateOf { if (animatedProgress >= 1f) ProgressFull else ios.blue }
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sp = strokeWidth.toPx()
            val tl = Offset(sp / 2, sp / 2)
            val cw = this.size.width; val ch = this.size.height
            val arc = Size(cw - sp, ch - sp)

            drawArc(color = ios.progressEmpty, startAngle = -90f, sweepAngle = 360f,
                useCenter = false, topLeft = tl, size = arc, style = Stroke(width = sp, cap = StrokeCap.Round))
            drawArc(brush = progressBrush, startAngle = -90f, sweepAngle = 360f * animatedProgress,
                useCenter = false, topLeft = tl, size = arc, style = Stroke(width = sp, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${currentMl}",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = textColor))
            Text("/ ${targetMl} ml",
                style = MaterialTheme.typography.bodyMedium.copy(color = ios.secondaryLabel))
            if (animatedProgress >= 1f)
                Text("🎉 目标达成！",
                    style = MaterialTheme.typography.labelLarge.copy(color = ProgressFull, fontWeight = FontWeight.SemiBold))
        }
    }
}
