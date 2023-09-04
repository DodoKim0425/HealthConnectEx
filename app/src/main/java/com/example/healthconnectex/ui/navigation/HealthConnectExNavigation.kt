package com.example.healthconnectex.ui.navigation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnectex.data.HealthConnectManager
import com.example.healthconnectex.showExceptionSnackbar
import com.example.healthconnectex.ui.screen.home.HomeScreen
import com.example.healthconnectex.ui.screen.sleepdata.SleepDataScreen
import com.example.healthconnectex.ui.screen.sleepdata.SleepSessionViewModel
import com.example.healthconnectex.ui.screen.sleepdata.SleepSessionViewModelFactory

private const val TAG = "HealthConnectExNavigati"
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

        composable(Screen.SleepDataScreen.route) {
            val viewModel: SleepSessionViewModel = viewModel(
                factory = SleepSessionViewModelFactory(
                    healthConnectManager = healthConnectManager
                )
            )
            val permissionsGranted by viewModel.permissionsGranted
            val session by viewModel.session
            val permissions = viewModel.permissions
            val onPermissionsResult = {
                Log.d(TAG, "HealthConnectNavigation: 왜안함")
                viewModel.initialLoad() 
            }
            val permissionsLauncher =
                rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) {
                    onPermissionsResult()
                }
            SleepDataScreen(
                permissions = permissions, //필요한 권한들
                permissionsGranted = permissionsGranted, //이미 권한이 허락되었는지 여부
                session = session, //화면에 처음 진입했을때 보여줄 수면 데이터
                uiState = viewModel.uiState, //수면 데이터 화면에서 이미 퍼미션 체크되었는지, 체크시 에러가 났는지 상태를 저장
                onError = { exception -> //수면 데이터 권한 처리할때 에러가 났을때 처리
                    showExceptionSnackbar(scaffoldState, scope, exception)
                },
                onPermissionsResult = { //수면 데이터 권한 처리가 완료된 상태일때 초기값
                    viewModel.initialLoad()
                },
                onPermissionsLaunch = { values -> //수면데이터 권한 처리가 되지 않은 경우 권한을 받도록 해주는 함수
                    Log.d(TAG, "HealthConnectNavigation: 런치~~~~~~~~~~~~~~~~~~~~~")
                    for(element in values){
                        Log.d(TAG, "HealthConnectNavigation: ${element}")
                    }
                    permissionsLauncher.launch(values)
                }
            )
        }
    }
}