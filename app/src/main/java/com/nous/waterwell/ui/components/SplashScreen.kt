package com.nous.waterwell.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.*
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Apple-style splash: clean white background, centered water-drop logo
 * with subtle scale + fade-in, then a gentle "AlexWater" title appears below.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Timeline
    var phase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(1200)  // icon animation
        phase = 1
        delay(800)   // title reveal
        delay(400)   // hold
        onFinished()
    }

    // Icon scale animation
    val iconScale by animateFloatAsState(
        targetValue = if (phase >= 0) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "icon"
    )

    // Title fade + slide up
    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "title"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (phase >= 1) 0f else 20f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "titleY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)),  // Apple light gray
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Water drop icon — Canvas drawn
            Canvas(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            ) {
                val w = size.width
                val h = size.height
                val dropW = w * 0.55f
                val dropH = h * 0.7f

                // Water drop shape
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w / 2, h * 0.05f)
                    cubicTo(w * 0.2f, h * 0.35f, w * 0.15f, h * 0.55f, w * 0.15f, h * 0.65f)
                    cubicTo(w * 0.15f, h * 0.85f, w * 0.3f, h * 0.95f, w / 2, h * 0.95f)
                    cubicTo(w * 0.7f, h * 0.95f, w * 0.85f, h * 0.85f, w * 0.85f, h * 0.65f)
                    cubicTo(w * 0.85f, h * 0.55f, w * 0.8f, h * 0.35f, w / 2, h * 0.05f)
                    close()
                }

                // Blue gradient fill
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF007AFF), Color(0xFF5856D6)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // White highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = dropW * 0.22f,
                    center = Offset(w * 0.38f, h * 0.42f)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Title
            Text(
                text = "AlexWater",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1F)
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha
                        translationY = titleOffset
                    }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Stay hydrated",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8E8E93)
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha * 0.8f
                        translationY = titleOffset * 0.7f
                    }
            )
        }
    }
}
