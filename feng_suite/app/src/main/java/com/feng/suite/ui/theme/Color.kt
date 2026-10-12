package com.feng.suite.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 主题基调色
val PrimaryIndigo = Color(0xFF4F46E5)
val PrimaryPurple = Color(0xFF7C3AED)
val BackgroundDark = Color(0xFF0F172A)
val CardBackgroundDark = Color(0xFF1E293B)
val CardBorderDark = Color(0xFF334155)

// 全局 4 色体系（与 Todo 完美对齐）
val BluePrimary = Color(0xFF2563EB)
val GreenPrimary = Color(0xFF16A34A)
val YellowPrimary = Color(0xFFD97706)
val RedPrimary = Color(0xFFDC2626)

val TagColors = listOf(
    "#2563EB", // 蓝
    "#16A34A", // 绿
    "#D97706", // 黄
    "#DC2626"  // 红
)


// 文本与毛玻璃高光
val TextWhitePrimary = Color(0xFFF8FAFC)
val TextWhiteSecondary = Color(0xFF94A3B8)
val TextWhiteTertiary = Color(0xFF64748B)

val CardBackground = Color(0x33FFFFFF)
val CardBorder = Color(0x2AFFFFFF)

// 沉浸式天气渐变色盘
val SunnyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF3A88E9), Color(0xFF67B0F8), Color(0xFF8EC5FC))
)

val NightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
)

val CloudyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF536976), Color(0xFF708796), Color(0xFF8B9DA8))
)

val RainyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF546E7A))
)

