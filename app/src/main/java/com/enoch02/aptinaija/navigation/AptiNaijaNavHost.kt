package com.enoch02.aptinaija.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.feature.bookmarks.BookmarksScreen
import com.enoch02.aptinaija.feature.companies.CompaniesScreen
import com.enoch02.aptinaija.feature.companies.CompanyDetailScreen
import com.enoch02.aptinaija.feature.history.HistoryScreen
import com.enoch02.aptinaija.feature.home.HomeScreen
import com.enoch02.aptinaija.feature.results.ResultsScreen
import com.enoch02.aptinaija.feature.review.ReviewScreen
import com.enoch02.aptinaija.feature.settings.SettingsScreen
import com.enoch02.aptinaija.feature.settings.SettingsViewModel
import com.enoch02.aptinaija.feature.test.TestScreen
import com.enoch02.aptinaija.feature.testsetup.TestSetupScreen
import com.enoch02.aptinaija.feature.wronganswers.WrongAnswersScreen

@Composable
fun AptiNaijaApp(navController: NavHostController = rememberNavController()) {
    val settingsViewModel: SettingsViewModel =
        androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(
            checkNotNull<androidx.lifecycle.ViewModelStoreOwner>(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            },
            null
        )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevelDestination = currentDestination?.hierarchy?.any { destination ->
        destination.hasRoute(HomeRoute::class) ||
                destination.hasRoute(CompaniesRoute::class) ||
                destination.hasRoute(HistoryRoute::class) ||
                destination.hasRoute(WrongAnswersRoute::class) ||
                destination.hasRoute(SettingsRoute::class)
    } == true

    AptiNaijaTheme(fontScale = settings.fontScale) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (isTopLevelDestination) {
                    AppNavigationBar(
                        currentDestination = currentDestination,
                        onNavigate = navController::navigateToTopLevel
                    )
                }
            }
        ) { innerPadding ->
            AptiNaijaNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AptiNaijaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onStartPractice = { navController.navigateToTopLevel(CompaniesRoute) },
                onStartMock = { navController.navigateToTopLevel(CompaniesRoute) },
                onDrillSection = { section ->
                    navController.navigate(
                        TestSetupRoute(mode = "DRILL", section = section)
                    )
                },
                onBookmarks = { navController.navigate(BookmarksRoute) },
                onOpenLastResult = { attemptId ->
                    navController.navigate(ResultsRoute(attemptId))
                }
            )
        }

        composable<CompaniesRoute> {
            CompaniesScreen(
                onCompany = { company -> navController.navigate(CompanyRoute(company)) }
            )
        }

        composable<CompanyRoute> {
            CompanyDetailScreen(
                onBack = { navController.popBackStack() },
                onStartPractice = { company ->
                    navController.navigate(TestSetupRoute(mode = "PRACTICE", company = company))
                },
                onStartDrill = { company, section ->
                    navController.navigate(
                        TestSetupRoute(mode = "DRILL", company = company, section = section)
                    )
                },
                onStartTemplate = { templateId ->
                    navController.navigate(TestSetupRoute(mode = "MOCK", templateId = templateId))
                }
            )
        }

        composable<TestSetupRoute> {
            TestSetupScreen(
                onBack = { navController.popBackStack() },
                onStarted = { attemptId ->
                    navController.navigate(TestRoute(attemptId))
                }
            )
        }

        composable<TestRoute> {
            TestScreen(
                onBack = { navController.popBackStack() },
                onFinished = { attemptId ->
                    navController.navigate(ResultsRoute(attemptId)) {
                        popUpTo<TestSetupRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<ResultsRoute> {
            ResultsScreen(
                onReview = { attemptId -> navController.navigate(ReviewRoute(attemptId)) },
                onHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo<HomeRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<ReviewRoute> {
            ReviewScreen(onBack = { navController.popBackStack() })
        }

        composable<WrongAnswersRoute> {
            WrongAnswersScreen(onBack = { navController.popBackStack() })
        }

        composable<BookmarksRoute> {
            BookmarksScreen(onBack = { navController.popBackStack() })
        }

        composable<HistoryRoute> {
            HistoryScreen()
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}

private fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
