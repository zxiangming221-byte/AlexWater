package com.alexwater.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexwater.ui.theme.*

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    goalMl: Int,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
) {
    if (data.isEmpty()) return
    val maxValue = maxOf(data.maxOf { it.second }, goalMl).toFloat()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, (label, value) ->
            val barFraction = (value / maxValue).coerceIn(0.02f, 1f)

            val animatedHeight by animateFloatAsState(
                targetValue = barFraction,
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = index * 50,
                    easing = EaseOutCubic
                ),
                label = "bar_$index"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // 数值标签
                Text(
                    text = "${value}ml",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (value >= goalMl) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                // 柱子
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(animatedHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (value >= goalMl) Success else MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(Modifier.height(6.dp))
                // 底部标签
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
