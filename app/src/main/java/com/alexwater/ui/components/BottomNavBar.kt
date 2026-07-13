package com.alexwater.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alexwater.ui.theme.*

private data class NavTab(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    NavTab("home", "主页", Icons.Outlined.Home),
    NavTab("reminders", "提醒", Icons.Outlined.Schedule),
    NavTab("stats", "统计", Icons.Outlined.BarChart),
    NavTab("history", "历史", Icons.Outlined.History),
    NavTab("settings", "设置", Icons.Outlined.Settings),
)

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 80.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 200f),
        label = "navOffset",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.roundToPx()) },
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            tabs.forEach { tab ->
                val isActive = currentTab == tab.key
                val color by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 200f),
                    label = "navColor",
                )
                val indicatorWidth by animateDpAsState(
                    targetValue = if (isActive) 20.dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 200f),
                    label = "navIndicator",
                )

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab.key) },
                        )
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Top indicator bar
                    Box(
                        modifier = Modifier
                            .width(indicatorWidth)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent),
                    )
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 6.dp),
                        tint = color,
                    )
                    Text(
                        text = tab.label,
                        style = AlexWaterTypography.bodySmall,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
