package com.breezy.weather.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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

val CardBackground = Color(0x33FFFFFF) // 20% 透明度毛玻璃
val CardBorder = Color(0x2AFFFFFF)     // 微弱白边框
val TextWhitePrimary = Color(0xFFFFFFFF)
val TextWhiteSecondary = Color(0xCCFFFFFF)
val TextWhiteTertiary = Color(0x99FFFFFF)
