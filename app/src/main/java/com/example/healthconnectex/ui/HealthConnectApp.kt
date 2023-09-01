package com.example.healthconnectex.ui

import android.annotation.SuppressLint
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthconnectex.R
import com.example.healthconnectex.data.HealthConnectManager
import com.example.healthconnectex.ui.navigation.Drawer
import com.example.healthconnectex.ui.navigation.HealthConnectNavigation
import com.example.healthconnectex.ui.navigation.Screen
import com.example.healthconnectex.ui.theme.HealthConnectExTheme
import kotlinx.coroutines.launch

const val TAG = "Health Connect sample"

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HealthConnectApp(healthConnectManager: HealthConnectManager) {
    HealthConnectExTheme {
        val scaffoldState = rememberScaffoldState()
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val availability by healthConnectManager.availability

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = {
                        val titleId = when (currentRoute) {
                            Screen.HomeScreen.route -> Screen.HomeScreen.titleId
                            Screen.SleepDataScreen.route -> Screen.SleepDataScreen.titleId
                            else -> R.string.app_name
                        }
                        Text(stringResource(titleId))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                // 헬스 커넥트 사용 가능한 경우에만 Drawer 버튼이 사용되도록함
                                if (availability == SDK_AVAILABLE) {
                                    scope.launch {
                                        scaffoldState.drawerState.open()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                stringResource(id = R.string.menu)
                            )
                        }
                    }
                )
            },
            drawerContent = {
                if (availability == HealthConnectClient.SDK_AVAILABLE) {
                    Drawer(
                        scope = scope,
                        scaffoldState = scaffoldState,
                        navController = navController
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(it) { data -> Snackbar(snackbarData = data) }
            }
        ) {
            HealthConnectNavigation(
                healthConnectManager = healthConnectManager,
                navController = navController,
                scaffoldState = scaffoldState
            )
        }
    }
}