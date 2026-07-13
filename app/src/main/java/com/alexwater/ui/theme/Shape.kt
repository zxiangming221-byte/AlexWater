package com.alexwater.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AlexWaterShapes = Shapes(
    // 2dp — Ferrari 精准棱角（主 CTA）
    extraSmall = RoundedCornerShape(2.dp),
    // 8dp — 输入框、分段选择器
    small = RoundedCornerShape(8.dp),
    // 14dp — 卡片
    medium = RoundedCornerShape(14.dp),
    // 20dp — 大按钮
    large = RoundedCornerShape(20.dp),
    // 胶囊
    extraLarge = RoundedCornerShape(50.dp),
)
