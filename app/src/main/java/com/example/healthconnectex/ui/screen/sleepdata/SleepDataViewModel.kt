package com.example.healthconnectex.ui.screen.sleepdata

import android.os.RemoteException
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectex.data.HealthConnectManager
import com.example.healthconnectex.data.SleepSessionData
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TAG = "SleepDataViewModel"

class SleepSessionViewModel(private val healthConnectManager: HealthConnectManager) :
    ViewModel() {

    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    var permissionsGranted = mutableStateOf(false)
        private set

    var session: MutableState<SleepSessionData> = mutableStateOf(
        SleepSessionData(
            startTime = null,
            startZoneOffset = null,
            endTime = null,
            endZoneOffset = null,
            duration = null
        )
    )
        private set

    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set

    // 권한 요청을 실행하는 함수
    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    fun initialLoad() {
        val today = ZonedDateTime.now()
        viewModelScope.launch {
            //수면 데이터를 가져올때 퍼미션이 허용 되었는지 확인한 후에 값을 가져옵니다
            tryWithPermissionsCheck {
                //session.value = healthConnectManager.readSleepDataOneDay(today)
                Log.d(TAG, "initialLoad: 읽는다")
                healthConnectManager.readSleepDataOneDay(today)
            }
        }
    }

    /*
    * 헬스 커넥트에 대해 퍼미션 확인과 에러 핸들링은 suspend함수로 해야됩니다
    * 퍼미션은 [block]에서 맨 처음에 체크되어야 합니다
    * 그리고 모든 퍼미션이 허락되지 않은 경우 [block]이 실행되면 안됩니다
    * 그리고 [permissionsGranted]가 false로 세팅되어야 합니다
    * [permissionsGranted]를 통해서 퍼미션 버튼을 어떻게 보여줄지 정합니다
    * 에러가 잡히면 [uiState]가 [UiState.Error]로 세팅됩니다
    * [uiState]를 통해서 에러 메시지를 보여주는 스낵바가 보여질지 결정됩니다
    * */
    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        //퍼미션이 이미 허용되었는지 확인합니다
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
        //퍼미션 허용 되었으면 매개변수로 받은 block을 실행합니다
        uiState = try {
            if (permissionsGranted.value) {
                block()
            }
            UiState.Done
        } catch (remoteException: RemoteException) {
            UiState.Error(remoteException)
        } catch (securityException: SecurityException) {
            UiState.Error(securityException)
        } catch (ioException: IOException) {
            UiState.Error(ioException)
        } catch (illegalStateException: IllegalStateException) {
            UiState.Error(illegalStateException)
        }
    }

    sealed class UiState {
        object Uninitialized : UiState()
        object Done : UiState()

        // 오류를 고유하게 식별할 수 있도록 각 오류 개체에 랜덤 UUID가 사용되며
        // 재구성 시 여러 스낵바가 발생하지 않습니다.
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }
}

class SleepSessionViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepSessionViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}