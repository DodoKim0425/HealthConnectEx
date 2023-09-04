package com.example.healthconnectex.ui.screen.sleepdata

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthconnectex.R
import com.example.healthconnectex.data.SleepSessionData
import java.util.UUID

private const val TAG = "SleepDataScreen"

@Composable
fun SleepDataScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    session: SleepSessionData,
    uiState: SleepSessionViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {
    // 마지막 에러 ID를 기억해야 합니다
    // 이는 컴포저블 요소가 리컴포지션될때 같은 에러에 대한 알림이 오는것을 막습니다
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is SleepSessionViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [SleepSessionViewModel.UiState] provides details of whether the last action was a
        // success or resulted in an error. Where an error occurred, for example in reading and
        // writing to Health Connect, the user is notified, and where the error is one that can be
        // recovered from, an attempt to do so is made.
        if (uiState is SleepSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }


    Column {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) { //퍼미션 없는 경우에 보여줄 버튼
                item {
                    Button(
                        onClick = {
                            Log.d(TAG, "SleepDataScreen: 퍼미션 ${permissions}")
                            onPermissionsLaunch(permissions) 
                        } //클릭하면 권한 요청
                    ) {
                        androidx.compose.material.Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                item {
                    Text(text = "${session}")
                }
            }
        }
    }

}