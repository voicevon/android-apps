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

// 卡片容器与高反差边框（对齐 TodoList 的高对比度深色质感）
val CardBackground = Color(0xFF1E293B)
val CardBorder = Color(0xFF334155)

// 沉浸式高反差天气渐变底色（深邃背景，让卡片与文字极度醒目）
val SunnyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF172554), Color(0xFF1E3A8A))
)

val NightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))
)

val CloudyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
)

val RainyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
)

