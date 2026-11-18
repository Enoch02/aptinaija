package com.enoch02.aptinaija.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.enoch02.aptinaija.R
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val titleRes: Int,
    val labelRes: Int,
    val icon: ImageVector,
    val showBookmarksAction: Boolean
) {
    Home(
        route = HomeRoute,
        routeClass = HomeRoute::class,
        titleRes = R.string.home,
        labelRes = R.string.home,
        icon = Icons.Filled.Home,
        showBookmarksAction = true
    ),
    Companies(
        route = CompaniesRoute,
        routeClass = CompaniesRoute::class,
        titleRes = R.string.companies,
        labelRes = R.string.companies,
        icon = Icons.Filled.Business,
        showBookmarksAction = true
    ),
    History(
        route = HistoryRoute,
        routeClass = HistoryRoute::class,
        titleRes = R.string.history,
        labelRes = R.string.history,
        icon = Icons.Filled.History,
        showBookmarksAction = false
    ),
    Wrong(
        route = WrongAnswersRoute,
        routeClass = WrongAnswersRoute::class,
        titleRes = R.string.wrong_answers,
        labelRes = R.string.wrong,
        icon = Icons.Filled.ErrorOutline,
        showBookmarksAction = true
    ),
    Settings(
        route = SettingsRoute,
        routeClass = SettingsRoute::class,
        titleRes = R.string.settings,
        labelRes = R.string.settings,
        icon = Icons.Filled.Settings,
        showBookmarksAction = false
    )
}
