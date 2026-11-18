package com.enoch02.aptinaija.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    AptiNaijaTheme(fontScale = settings.fontScale) {
        AptiNaijaNavHost(navController = navController)
    }
}

@Composable
private fun AptiNaijaNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onBrowseCompanies = { navController.navigate(CompaniesRoute) },
                onStartTemplate = { templateId ->
                    navController.navigate(
                        TestSetupRoute(mode = "MOCK", templateId = templateId)
                    )
                },
                onHistory = { navController.navigate(HistoryRoute) },
                onWrongAnswers = { navController.navigate(WrongAnswersRoute) },
                onBookmarks = { navController.navigate(BookmarksRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
                onOpenLastResult = { attemptId ->
                    navController.navigate(ResultsRoute(attemptId))
                }
            )
        }

        composable<CompaniesRoute> {
            CompaniesScreen(
                onBack = { navController.popBackStack() },
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
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
