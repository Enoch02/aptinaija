package com.enoch02.aptinaija.feature.testsetup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.TemplateSection
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestSetupContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgress() {
        setSetup(TestSetupUiState(loading = true))

        composeRule.onNodeWithTag("setup_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_begin").assertDoesNotExist()
    }

    @Test
    fun practiceState_showsSummaryInfoAndQuestionOptions() {
        setSetup(baseState())

        composeRule.onNodeWithText("Practice · Seplat Energy").assertIsDisplayed()
        composeRule.onNodeWithText("Practice mode").assertIsDisplayed()
        composeRule.onNodeWithText("Questions").assertIsDisplayed()
        composeRule.onNodeWithText("Begin practice").assertIsDisplayed()
    }

    @Test
    fun modeSelector_invokesCallback() {
        var selectedMode: AttemptMode? = null
        setSetup(baseState(), onModeSelected = { selectedMode = it })

        composeRule.onNodeWithTag("setup_mode_mock").performClick()

        composeRule.runOnIdle {
            assertEquals(AttemptMode.MOCK, selectedMode)
        }
    }

    @Test
    fun mockState_showsTemplateSummaryAndInfo() {
        setSetup(
            baseState().copy(
                mode = AttemptMode.MOCK,
                selectedTemplateId = "SEPLAT_TECH_TRAINEE"
            )
        )

        composeRule.onNodeWithText("Mock test · Seplat Energy").assertIsDisplayed()
        composeRule.onNodeWithText("100").assertIsDisplayed()
        composeRule.onNodeWithText("80").assertIsDisplayed()
        composeRule.onNodeWithText("Strict timing").assertIsDisplayed()
        composeRule.onNodeWithText("What to expect").assertIsDisplayed()
        composeRule.onNodeWithText("Begin").assertIsDisplayed()
    }

    @Test
    fun modeSelector_doesNotOfferRandomQuiz() {
        setSetup(baseState())

        composeRule.onNodeWithTag("setup_mode_practice").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_mode_drill").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_mode_mock").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_mode_random_quiz").assertDoesNotExist()
    }

    @Test
    fun drillState_showsSectionsAndInvokesSectionCallback() {
        var selectedSection: String? = null
        setSetup(
            baseState().copy(mode = AttemptMode.DRILL),
            onSectionSelected = { selectedSection = it }
        )

        composeRule.onNodeWithText("Choose a section").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_section_General Knowledge").performClick()

        composeRule.runOnIdle {
            assertEquals("General Knowledge", selectedSection)
        }
    }

    @Test
    fun questionCount_invokesCallback() {
        var count: Int? = null
        setSetup(baseState(), onQuestionCountSelected = { count = it })

        composeRule.onNodeWithTag("setup_questions_40").performClick()

        composeRule.runOnIdle {
            assertEquals(40, count)
        }
    }

    @Test
    fun begin_invokesStartCallback() {
        var started = false
        setSetup(baseState(), onBegin = { started = true })

        composeRule.onNodeWithTag("setup_begin").performClick()

        composeRule.runOnIdle {
            assertTrue(started)
        }
    }

    @Test
    fun multipleTemplates_showsTemplateChooser() {
        var selectedTemplate: String? = null
        val second = template(
            id = "SECOND_MOCK",
            title = "Second Mock",
            sections = listOf(TemplateSection("Verbal Reasoning", 10, 10))
        )
        setSetup(
            baseState().copy(
                mode = AttemptMode.MOCK,
                templates = listOf(template(), second),
                selectedTemplateId = "SEPLAT_TECH_TRAINEE"
            ),
            onTemplateSelected = { selectedTemplate = it }
        )

        composeRule.onNodeWithText("Choose a mock test").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_template_SECOND_MOCK").performClick()

        composeRule.runOnIdle {
            assertEquals("SECOND_MOCK", selectedTemplate)
        }
    }

    @Test
    fun emptyPractice_disablesBeginAndShowsMessage() {
        setSetup(
            baseState().copy(
                sections = emptyList(),
                templates = emptyList(),
                selectedSection = null,
                selectedTemplateId = null
            )
        )

        composeRule.onNodeWithText("No sections available for this company.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("setup_begin").assertIsNotEnabled()
    }

    @Test
    fun emptyMock_disablesBeginAndShowsMessage() {
        setSetup(
            baseState().copy(
                mode = AttemptMode.MOCK,
                templates = emptyList(),
                selectedTemplateId = null
            )
        )

        composeRule.onNodeWithText("No mock tests available for this company.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("setup_begin").assertIsNotEnabled()
    }

    @Test
    fun errorState_showsMessage() {
        setSetup(baseState().copy(error = "No questions are available for this selection yet."))

        composeRule.onNodeWithTag("setup_error")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("No questions are available for this selection yet.")
            .assertIsDisplayed()
    }

    @Test
    fun startingState_showsStartingAndDisablesBegin() {
        setSetup(baseState().copy(starting = true))

        composeRule.onNodeWithText("Starting…").assertIsDisplayed()
        composeRule.onNodeWithTag("setup_begin").assertIsNotEnabled()
    }

    @Test
    fun largeFontScale_keepsMockContentReachable() {
        setSetup(
            baseState().copy(mode = AttemptMode.MOCK),
            fontScale = 2f
        )

        composeRule.onNodeWithText("What to expect")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("setup_begin").assertIsDisplayed()
    }

    private fun setSetup(
        state: TestSetupUiState,
        onModeSelected: (AttemptMode) -> Unit = {},
        onSectionSelected: (String) -> Unit = {},
        onTemplateSelected: (String) -> Unit = {},
        onQuestionCountSelected: (Int) -> Unit = {},
        onBegin: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                TestSetupContent(
                    state = state,
                    onBack = {},
                    onModeSelected = onModeSelected,
                    onSectionSelected = onSectionSelected,
                    onTemplateSelected = onTemplateSelected,
                    onQuestionCountSelected = onQuestionCountSelected,
                    onBegin = onBegin
                )
            }
        }
    }

    private fun baseState() = TestSetupUiState(
        loading = false,
        mode = AttemptMode.PRACTICE,
        companyId = "SEP",
        companyName = "Seplat Energy",
        sections = listOf(
            SectionSummary("General Knowledge", 60),
            SectionSummary("Numerical Reasoning", 20),
            SectionSummary("Verbal Reasoning", 20),
            SectionSummary("Logical Reasoning", 10),
            SectionSummary("Engineering/Technical", 50)
        ),
        templates = listOf(template()),
        selectedSection = "Numerical Reasoning",
        selectedTemplateId = "SEPLAT_TECH_TRAINEE",
        questionCount = 20
    )

    private fun template(
        id: String = "SEPLAT_TECH_TRAINEE",
        title: String = "Seplat Technical Trainee Mock",
        sections: List<TemplateSection> = listOf(
            TemplateSection("Numerical Reasoning", 20, 20),
            TemplateSection("Verbal Reasoning", 20, 18),
            TemplateSection("Logical Reasoning", 10, 12),
            TemplateSection("Engineering/Technical", 50, 30)
        )
    ) = TestTemplate(
        id = id,
        title = title,
        companyId = "SEP",
        company = "Seplat Energy",
        provider = "HCP (Human Capital Partners)",
        sourceUrl = null,
        defaults = TestDefaults(),
        sections = sections
    )
}
