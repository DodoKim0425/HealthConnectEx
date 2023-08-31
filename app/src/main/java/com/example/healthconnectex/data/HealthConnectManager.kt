package com.example.healthconnectex.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.time.Instant

private const val TAG = "HealthConnectManager"

// 헬스 커넥트를 사용할 수 있는 최소 sdk
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

class HealthConnectManager(private val context: Context) {

    //healthConnectClient는 health connect api 의 진입점이다
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // 헬스 커넥트 지원 되는지 여부를 저장하는 변수
    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    // 현재 기기가 헬스 커넥트 지원이 되는지, 설치 되어있는지 확인
    fun checkAvailability() {
        //헬스 커넥트 지원 안되는 기기인 경우 NOT_SUPPORTED, 설치 안되어있으면 NOT_INSTALLED, 지원되고 설치되어있으면 SDK_AVAILABLE
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    // 지정한 모든 권한이 이미 부여되었는지 여부를 결정합니다.
    // 권한 흐름에서 먼저 [PermissionController.getGrantedPermissions]를 호출하는 것이 좋습니다.(지금 밑에 있는 함수)
    // 권한이 이미 부여된 경우 [PermissionController.requestPermissionResultContract]를 통해 권한을 요청할 필요가 없습니다.
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    // 권한 요청 함수
    // 권한이 이미 부여된 경우 이 함수를 통해 권한 요청할 필요 없다
    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    //특정 날짜의 수면 데이터를 가져오는 함수
    //ReadRecordsRequest는 헬스 데이터 기록을 읽는함수다
    //SleepSessionRecord를 SleepSessionData로 변경하여 반환한다
    //SleepSessinRcord에 대하여: https://developer.android.com/reference/androidx/health/connect/client/records/SleepSessionRecord
    suspend fun readSleepDataOneDay(date: Instant): SleepSessionData {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class, //어떤 데이터인지 정한다
            timeRangeFilter = TimeRangeFilter.between(date, date), //언제부터 언제까지 데이터를 가져올지 정한다
            ascendingOrder = false //내림차순 정렬하여 반환
            //dataOriginFilter = //특정 앱이 저장한 데이터만 골라서 가져올수있다, 패키지 이름을 적으면됨
        )
        val response = healthConnectClient.readRecords(request)
        var sleepSessionDataOneDay: SleepSessionData = SleepSessionData(
            uid = "",
            title = "",
            notes = "",
            startTime = date,
            startZoneOffset = null,
            endTime = date,
            endZoneOffset = null,
            duration = null,
            stages = listOf()
        )

        Log.d(TAG, "readSleepDataOneDay: ${response.records.size}")

        response.records.forEach { session ->
            val sessionTimeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
            val durationAggregateRequest = AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = sessionTimeFilter
            )
            val aggregateResponse = healthConnectClient.aggregate(durationAggregateRequest)
            sleepSessionDataOneDay =
                SleepSessionData(
                    uid = session.metadata.id,
                    title = session.title,
                    notes = session.notes,
                    startTime = session.startTime,
                    startZoneOffset = session.startZoneOffset,
                    endTime = session.endTime,
                    endZoneOffset = session.endZoneOffset,
                    duration = aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL],
                    stages = session.stages
                )

        }
        return sleepSessionDataOneDay
    }
//
//    // 집계 데이터를 가져오기 위해서는 healthConnectClient.aggregate사용한다
//    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
//        //집계 데이터를 요청하기 위해서 AggregateRequest를 만들어야됨
//        val request = AggregateRequest(
//            metrics = setOf(WeightRecord.WEIGHT_AVG),// 어떤 데이터 가져올지
//            timeRangeFilter = TimeRangeFilter.between(start, end)// 가져올 기간
//        )
//        val response = healthConnectClient.aggregate(request)
//        return response[WeightRecord.WEIGHT_AVG]
//    }
//
//    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
//        val request = ReadRecordsRequest(
//            recordType = ExerciseSessionRecord::class,
//            timeRangeFilter = TimeRangeFilter.between(start, end)
//        )
//        val response = healthConnectClient.readRecords(request)
//        return response.records
//    }
//
//    //특정 운동에 관련한 집계 데이터는 healthConnectClient.readRecord를 사용해 읽는다
//    suspend fun readAssociatedSessionData(
//        uid: String,
//    ): ExerciseSessionData {
//        val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
//        // Use the start time and end time from the session, for reading raw and aggregate data.
//        val timeRangeFilter = TimeRangeFilter.between(
//            startTime = exerciseSession.record.startTime,
//            endTime = exerciseSession.record.endTime
//        )
//        val aggregateDataTypes = setOf(
//            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
//            StepsRecord.COUNT_TOTAL,
//            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
//            HeartRateRecord.BPM_AVG,
//            HeartRateRecord.BPM_MAX,
//            HeartRateRecord.BPM_MIN,
//        )
//        // Limit the data read to just the application that wrote the session. This may or may not
//        // be desirable depending on the use case: In some cases, it may be useful to combine with
//        // data written by other apps.
//        val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
//        val aggregateRequest = AggregateRequest(
//            metrics = aggregateDataTypes,
//            timeRangeFilter = timeRangeFilter,
//            dataOriginFilter = dataOriginFilter
//        )
//        val aggregateData = healthConnectClient.aggregate(aggregateRequest)
//        val heartRateData = readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)
//
//        return ExerciseSessionData(
//            uid = uid,
//            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
//            totalSteps = aggregateData[StepsRecord.COUNT_TOTAL],
//            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
//            minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
//            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
//            avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG],
//            heartRateSeries = heartRateData,
//        )
//    }

    // 데이터 변경 사항을 가져오기 위해서는 토큰이 필요함,
    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class,
                    StepsRecord::class,
                    TotalCaloriesBurnedRecord::class,
                    HeartRateRecord::class,
                    WeightRecord::class
                )
            )
        )
    }

    // 앱이 마지막으로 헬스 커넥트와 동기화된 시점부터 지금까지의 변경사항을 가져오게 하려면
    // changes token과 getChanges함수를 사용한다
    // changes token 은 위에 있는 getChangesToken함수를 써서 가져온다
    // changes token 은 30일 까지 유효하다
    // 따라서 30일 이내에 정기적으로 데이터를 업데이트 하여 변경사항 반영해야되고
    // 토큰이 유효하지 않은 경우 처리가 필요하고
    // 필요한 데이터를 얻기 위한 대체 매커니즘이 있어야함
    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

    /**
     * Convenience function to reuse code for reading data.
     */
    private suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    // Represents the two types of messages that can be sent in a Changes flow.
    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}

// 헬스커넥트가 설치되어 있는지, 지원되는 버전의 기기인지 판단하는 값 클래스
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}