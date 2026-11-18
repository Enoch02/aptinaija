package com.enoch02.aptinaija.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.enoch02.aptinaija.R
import kotlin.reflect.KClass

@Composable
fun AppNavigationBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination.isOn(HomeRoute::class),
            onClick = { onNavigate(HomeRoute) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.home)) }
        )
        NavigationBarItem(
            selected = currentDestination.isOn(CompaniesRoute::class),
            onClick = { onNavigate(CompaniesRoute) },
            icon = { Icon(Icons.Filled.Business, contentDescription = null) },
            label = { Text(stringResource(R.string.companies)) }
        )
        NavigationBarItem(
            selected = currentDestination.isOn(HistoryRoute::class),
            onClick = { onNavigate(HistoryRoute) },
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
            label = { Text(stringResource(R.string.history)) }
        )
        NavigationBarItem(
            selected = currentDestination.isOn(WrongAnswersRoute::class),
            onClick = { onNavigate(WrongAnswersRoute) },
            icon = { Icon(Icons.Filled.ErrorOutline, contentDescription = null) },
            label = { Text(stringResource(R.string.wrong)) }
        )
        NavigationBarItem(
            selected = currentDestination.isOn(SettingsRoute::class),
            onClick = { onNavigate(SettingsRoute) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}

private fun <T : Any> NavDestination?.isOn(route: KClass<T>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } == true
