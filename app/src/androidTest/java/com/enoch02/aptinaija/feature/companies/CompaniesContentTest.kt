package com.enoch02.aptinaija.feature.companies

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompaniesContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesList() {
        setCompanies(CompaniesUiState(loading = true))

        composeRule.onNodeWithTag("companies_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("companies_list").assertDoesNotExist()
    }

    @Test
    fun populatedState_showsCompanyRowsAndSummaries() {
        setCompanies(populatedState())

        composeRule.onNodeWithText("UBA").assertIsDisplayed()
        composeRule.onNodeWithText("UB").assertIsDisplayed()
        composeRule.onNodeWithText("In-house / MS Forms · 64 q · 75 min")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Cross-industry (Dragnet clients)").assertIsDisplayed()
        composeRule.onNodeWithText("CI").assertIsDisplayed()
        composeRule.onNodeWithText("Dragnet · 2 papers").assertIsDisplayed()
    }

    @Test
    fun companyRow_invokesCallbackWithCompany() {
        var selectedCompany: String? = null
        setCompanies(populatedState(), onCompany = { selectedCompany = it })

        composeRule.onNodeWithTag("company_row_UBA").performClick()

        composeRule.runOnIdle {
            assertEquals("UBA", selectedCompany)
        }
    }

    @Test
    fun sectionChip_invokesCallback() {
        var selectedSection: String? = null
        setCompanies(
            populatedState(),
            onSectionSelected = { selectedSection = it }
        )

        composeRule.onNodeWithTag("companies_section_Numerical Reasoning")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("Numerical Reasoning", selectedSection)
        }
    }

    @Test
    fun selectedSections_showSelectedState() {
        setCompanies(
            populatedState().copy(
                selectedSections = setOf("Numerical Reasoning", "Verbal Reasoning")
            )
        )

        composeRule.onNodeWithTag("companies_section_Numerical Reasoning")
            .performScrollTo()
            .assertIsSelected()
        composeRule.onNodeWithTag("companies_section_Verbal Reasoning")
            .performScrollTo()
            .assertIsSelected()
    }

    @Test
    fun providerChip_listsProvidersAndTogglesMultiple() {
        val toggled = mutableListOf<String>()
        setCompanies(
            populatedState(),
            onProviderToggled = { toggled += it }
        )

        composeRule.onNodeWithTag("companies_provider_chip")
            .performClick()
        composeRule.onNodeWithText("Dragnet").performClick()
        composeRule.onNodeWithText("In-house / MS Forms").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("Dragnet", "In-house / MS Forms"), toggled)
        }
    }

    @Test
    fun selectedProviders_showCountInChip() {
        setCompanies(
            populatedState().copy(
                selectedProviders = setOf("Dragnet", "In-house / MS Forms")
            )
        )

        composeRule.onNodeWithTag("companies_provider_chip").assertIsSelected()
        composeRule.onNodeWithText("2 providers").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsMessageAndClearFilters() {
        var cleared = false
        setCompanies(
            state = CompaniesUiState(
                loading = false,
                companies = emptyList(),
                sections = listOf("Verbal Reasoning"),
                providers = listOf("Dragnet"),
                selectedSections = setOf("Verbal Reasoning")
            ),
            onClearFilters = { cleared = true }
        )

        composeRule.onNodeWithText("No companies match these filters.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("companies_clear_filters").performClick()

        composeRule.runOnIdle {
            assertTrue(cleared)
        }
    }

    @Test
    fun largeFontScale_keepsCompanyRowsReachable() {
        val companies = (1..20).map { index ->
            companyItem(company = "Company $index")
        }
        setCompanies(
            state = populatedState().copy(companies = companies),
            fontScale = 2f
        )

        composeRule.onNodeWithTag("companies_list")
            .performScrollToNode(hasTestTag("company_row_Company 20"))
        composeRule.onNodeWithTag("company_row_Company 20").assertIsDisplayed()
    }

    private fun setCompanies(
        state: CompaniesUiState,
        onCompany: (String) -> Unit = {},
        onSectionSelected: (String) -> Unit = {},
        onProviderToggled: (String) -> Unit = {},
        onClearProviders: () -> Unit = {},
        onClearFilters: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                CompaniesContent(
                    state = state,
                    onCompany = onCompany,
                    onSectionSelected = onSectionSelected,
                    onProviderToggled = onProviderToggled,
                    onClearProviders = onClearProviders,
                    onClearFilters = onClearFilters
                )
            }
        }
    }

    private fun populatedState() = CompaniesUiState(
        loading = false,
        companies = listOf(
            companyItem(company = "UBA"),
            companyItem(
                companyId = "DRAGNET",
                company = "Cross-industry (Dragnet clients)",
                initials = "CI",
                templateCount = 2,
                questionCount = 95,
                totalMinutes = 95,
                sections = setOf(
                    "Abstract Reasoning",
                    "Logical Reasoning",
                    "Numerical Reasoning",
                    "Verbal Reasoning"
                ),
                providers = setOf("Dragnet")
            )
        ),
        sections = listOf(
            "Abstract Reasoning",
            "Logical Reasoning",
            "Numerical Reasoning",
            "Verbal Reasoning"
        ),
        providers = listOf("Dragnet", "In-house / MS Forms")
    )

    private fun companyItem(
        company: String,
        companyId: String = company,
        initials: String = "UB",
        templateCount: Int = 1,
        questionCount: Int = 64,
        totalMinutes: Int = 75,
        sections: Set<String> = setOf(
            "Numerical Reasoning",
            "Verbal Reasoning",
            "Logical Reasoning"
        ),
        providers: Set<String> = setOf("In-house / MS Forms")
    ) = CompanyListItem(
        companyId = companyId,
        company = company,
        initials = initials,
        templateCount = templateCount,
        questionCount = questionCount,
        totalMinutes = totalMinutes,
        sections = sections,
        providers = providers
    )
}
