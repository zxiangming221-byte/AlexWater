package com.alexwater.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 字体族：系统无衬线 + 等宽
val InterFamily = FontFamily.Default
val MonoFamily = FontFamily.Monospace

val AlexWaterTypography = Typography(
    // 进度环百分比 (40sp, mono, bold)
    displayLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 40.sp,
    ),
    // 统计卡片数值 (28sp, mono)
    displayMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    ),
    // 快捷按钮数值 (22sp, mono)
    displaySmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    // 目标文字 / 标题 (18sp)
    headlineMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    // 正文 (15sp)
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    // 辅助文字 (13sp)
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    // 单位标签 (11sp)
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    ),
    // 标签 (13sp, semibold)
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
)
