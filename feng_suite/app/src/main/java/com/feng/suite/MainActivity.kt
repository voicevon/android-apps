package com.feng.suite

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.feng.suite.feature.clock.ui.screens.AlarmScreen
import com.feng.suite.feature.clock.ui.viewmodel.AlarmViewModel
import com.feng.suite.feature.todo.ui.TodoListScreen
import com.feng.suite.feature.todo.ui.TodoViewModel
import com.feng.suite.feature.weather.ui.screens.WeatherScreen
import com.feng.suite.feature.weather.ui.viewmodel.WeatherViewModel
import com.feng.suite.ui.dashboard.DashboardScreen
import com.feng.suite.ui.navigation.NavItem
import com.feng.suite.ui.theme.BackgroundDark
import com.feng.suite.ui.theme.FengSuiteTheme

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels()

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            weatherViewModel.locateCurrentCity(this) {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FengSuiteTheme {
                MainContainerScreen(
                    weatherViewModel = weatherViewModel,
                    alarmViewModel = alarmViewModel,
                    todoViewModel = todoViewModel,
                    onRequestLocationPermission = {
                        requestLocationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MainContainerScreen(
    weatherViewModel: WeatherViewModel,
    alarmViewModel: AlarmViewModel,
    todoViewModel: TodoViewModel,
    onRequestLocationPermission: () -> Unit
) {
    var currentItem by remember { mutableStateOf(NavItem.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                NavItem.entries.forEach { item ->
                    val selected = currentItem == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentItem = item },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF81D4FA),
                            indicatorColor = Color(0xFF2563EB).copy(alpha = 0.4f),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentItem, label = "suite_navigation_crossfade") { tab ->
                when (tab) {
                    NavItem.DASHBOARD -> DashboardScreen(
                        weatherViewModel = weatherViewModel,
                        alarmViewModel = alarmViewModel,
                        todoViewModel = todoViewModel,
                        onNavigateTo = { currentItem = it }
                    )
                    NavItem.CLOCK -> AlarmScreen(
                        viewModel = alarmViewModel
                    )
                    NavItem.WEATHER -> WeatherScreen(
                        viewModel = weatherViewModel,
                        onRequestLocationPermission = onRequestLocationPermission
                    )
                    NavItem.TODO -> TodoListScreen(
                        viewModel = todoViewModel
                    )
                }
            }
        }
    }
}
