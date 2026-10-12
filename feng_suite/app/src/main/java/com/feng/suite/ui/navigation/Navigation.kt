package com.feng.suite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("概览", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    CLOCK("时钟", Icons.Filled.Alarm, Icons.Outlined.Alarm),
    WEATHER("天气", Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    TODO("待办", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
}
