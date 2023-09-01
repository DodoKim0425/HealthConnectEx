package com.example.healthconnectex.ui.navigation

import com.example.healthconnectex.R

const val UID_NAV_ARGUMENT = "uid"

/**
 * 앱의 모든 화면을 표시합니다.
 *
 * @paramroute 탐색 구성에 사용되는 경로 문자열
 * @param titleId 제목으로 표시할 문자열 리소스의 ID
 * @param hasMenuItem 이 화면을 왼쪽 메뉴의 메뉴 항목으로 표시해야 하는지 여부(아니요)
 * 탐색 그래프의 모든 화면은 메뉴에서 직접 도달하도록 되어 있습니다.).
 */
enum class Screen (val route: String, val titleId: Int, val hasMenuItem: Boolean = true) {
    HomeScreen("home_screen", R.string.home_screen,false),
    SleepDataScreen("sleep_data_screen", R.string.sleep_data_screen, false)
}