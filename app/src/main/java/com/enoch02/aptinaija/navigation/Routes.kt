package com.enoch02.aptinaija.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object CompaniesRoute

@Serializable
data class CompanyRoute(val companyId: String)

@Serializable
data class TestSetupRoute(
    val mode: String,
    val company: String? = null,
    val section: String? = null,
    val templateId: String? = null
)

@Serializable
data class TestRoute(val attemptId: String)

@Serializable
data class ResultsRoute(val attemptId: String)

@Serializable
data class ReviewRoute(val attemptId: String)

@Serializable
data object WrongAnswersRoute

@Serializable
data object BookmarksRoute

@Serializable
data object HistoryRoute

@Serializable
data object SettingsRoute
