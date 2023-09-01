package com.example.healthconnectex.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnectex.data.HealthConnectManager
import com.example.healthconnectex.ui.screen.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun HealthConnectNavigation(
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    scaffoldState: ScaffoldState
) {
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        val availability by healthConnectManager.availability
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                healthConnectAvailability = availability,
                onResumeAvailabilityCheck = {
                    healthConnectManager.checkAvailability()
                }
            )
        }

//        composable(Screen.SleepDataScreen.route) {
//            val viewModel: SleepSessionViewModel = viewModel(
//                factory = SleepSessionViewModelFactory(
//                    healthConnectManager = healthConnectManager
//                )
//            )
//            val permissionsGranted by viewModel.permissionsGranted
//            val sessionsList by viewModel.sessionsList
//            val permissions = viewModel.permissions
//            val onPermissionsResult = { viewModel.initialLoad() }
//            val permissionsLauncher =
//                rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
//                    onPermissionsResult()
//                }
//            SleepSessionScreen(
//                permissionsGranted = permissionsGranted,
//                permissions = permissions,
//                sessionsList = sessionsList,
//                uiState = viewModel.uiState,
//                onInsertClick = {
//                    viewModel.generateSleepData()
//                },
//                onError = { exception ->
//                    showExceptionSnackbar(scaffoldState, scope, exception)
//                },
//                onPermissionsResult = {
//                    viewModel.initialLoad()
//                },
//                onPermissionsLaunch = { values ->
//                    permissionsLauncher.launch(values)
//                }
//            )
//        }
    }
}