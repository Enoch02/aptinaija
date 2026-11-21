package com.enoch02.aptinaija.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AppThemeMode
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.feature.bookmarks.BookmarksScreen
import com.enoch02.aptinaija.feature.companies.CompaniesScreen
import com.enoch02.aptinaija.feature.history.HistoryScreen
import com.enoch02.aptinaija.feature.home.HomeScreen
import com.enoch02.aptinaija.feature.randomquiz.RandomQuizSetupScreen
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
        hiltViewModel(
            checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            },
            null
        )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val shellViewModel: AppShellViewModel = hiltViewModel()
    val bookmarkCount by shellViewModel.bookmarkCount.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val topLevelDestination = currentDestination.topLevelDestination()

    val darkTheme = when (settings.themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AUTO -> isSystemInDarkTheme()
    }

    AptiNaijaTheme(darkTheme = darkTheme) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                topLevelDestination?.let { destination ->
                    AppTopBar(
                        destination = destination,
                        bookmarkCount = bookmarkCount,
                        onBookmarks = { navController.navigate(BookmarksRoute) }
                    )
                }
            },
            bottomBar = {
                if (topLevelDestination != null) {
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

private fun NavDestination?.topLevelDestination(): TopLevelDestination? =
    this?.let { destination ->
        TopLevelDestination.entries.firstOrNull { candidate ->
            destination.hierarchy.any { it.hasRoute(candidate.routeClass) }
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
                onStartRandomQuiz = {
                    navController.navigate(RandomQuizSetupRoute())
                },
                onDrillSection = { section ->
                    navController.navigate(
                        TestSetupRoute(mode = "DRILL", section = section)
                    )
                },
                onOpenLastResult = { attemptId ->
                    navController.navigate(ResultsRoute(attemptId))
                }
            )
        }

        composable<CompaniesRoute> {
            CompaniesScreen(
                onCompany = { companyId ->
                    navController.navigate(
                        TestSetupRoute(mode = "PRACTICE", company = companyId)
                    )
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

        composable<RandomQuizSetupRoute> {
            RandomQuizSetupScreen(
                onBack = { navController.popBackStack() },
                onStarted = { attemptId ->
                    navController.navigate(TestRoute(attemptId, fromRandomQuiz = true))
                }
            )
        }

        composable<TestRoute> { entry ->
            val route = entry.toRoute<TestRoute>()
            TestScreen(
                onBack = { navController.popBackStack() },
                onFinished = { attemptId ->
                    navController.navigate(
                        ResultsRoute(attemptId, hideTryAgain = route.hideTryAgain)
                    ) {
                        // Remove the entry the test was launched from so Results
                        // returns to its origin instead of the finished test.
                        when {
                            route.hideTryAgain -> popUpTo<TestRoute> { inclusive = true }
                            route.fromRandomQuiz -> popUpTo<RandomQuizSetupRoute> { inclusive = true }
                            else -> popUpTo<TestSetupRoute> { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<ResultsRoute> {
            ResultsScreen(
                onBack = { navController.popBackStack() },
                onTryAgain = { attempt ->
                    val setupRoute = if (attempt.mode == AttemptMode.RANDOM_QUIZ) {
                        RandomQuizSetupRoute(questionCount = attempt.totalQuestions)
                    } else {
                        TestSetupRoute(
                            mode = attempt.mode.name,
                            company = attempt.company,
                            section = attempt.section,
                            templateId = attempt.templateId,
                            questionCount = when (attempt.mode) {
                                AttemptMode.MOCK -> null
                                else -> attempt.totalQuestions
                            }
                        )
                    }
                    navController.navigate(setupRoute) {
                        // Drop the finished result so back never returns to a stale attempt.
                        popUpTo<ResultsRoute> { inclusive = true }
                    }
                },
                onReview = { attemptId -> navController.navigate(ReviewRoute(attemptId)) }
            )
        }

        composable<ReviewRoute> {
            ReviewScreen(onBack = { navController.popBackStack() })
        }

        composable<WrongAnswersRoute> {
            WrongAnswersScreen(
                onRetest = { attemptId ->
                    navController.navigate(TestRoute(attemptId, hideTryAgain = true))
                },
                onStartPractice = { navController.navigateToTopLevel(CompaniesRoute) }
            )
        }

        composable<BookmarksRoute> {
            BookmarksScreen(
                onBack = { navController.popBackStack() },
                onRetest = { attemptId ->
                    navController.navigate(TestRoute(attemptId, hideTryAgain = true))
                },
                onStartPractice = { navController.navigateToTopLevel(CompaniesRoute) }
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                onStartPractice = { navController.navigateToTopLevel(CompaniesRoute) },
                onAttempt = { attemptId -> navController.navigate(ResultsRoute(attemptId)) }
            )
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
