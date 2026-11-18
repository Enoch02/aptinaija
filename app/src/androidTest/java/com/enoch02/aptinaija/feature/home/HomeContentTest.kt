package com.enoch02.aptinaija.feature.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_showsPlaceholderAndAdSlot() {
        setHome(HomeUiState(loading = false))

        composeRule.onNodeWithText("Last score").assertIsDisplayed()
        composeRule.onNodeWithText("No completed attempts yet").assertIsDisplayed()
        composeRule.onNodeWithText("Answer some questions to see your weakest sections.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_ad_slot").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setHome(HomeUiState(loading = true))

        composeRule.onNodeWithTag("home_loading").assertIsDisplayed()
        composeRule.onNodeWithText("Last score").assertDoesNotExist()
    }

    @Test
    fun populatedState_showsScoreBenchmarkAndWeakSections() {
        setHome(populatedState())

        composeRule.onNodeWithText("Last score · UBA GMAP Mock").assertIsDisplayed()
        composeRule.onNodeWithText("78%").assertIsDisplayed()
        composeRule.onNodeWithText("Passed (benchmark 70%) · 68:12 used").assertIsDisplayed()
        composeRule.onNodeWithText("Numerical Reasoning").assertIsDisplayed()
        composeRule.onNodeWithText("54%").assertIsDisplayed()
        composeRule.onNodeWithText("Data Interpretation").assertIsDisplayed()
        composeRule.onNodeWithText("60%").assertIsDisplayed()
        composeRule.onNodeWithTag("home_progress_Numerical Reasoning")
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.54f, 0f..1f))
        composeRule.onNodeWithTag("home_progress_Data Interpretation")
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.60f, 0f..1f))
    }

    @Test
    fun failingAttempt_showsFailedBenchmark() {
        setHome(populatedState(correctCount = 50))

        composeRule.onNodeWithText("50%").assertIsDisplayed()
        composeRule.onNodeWithText("Failed (benchmark 70%) · 68:12 used").assertIsDisplayed()
    }

    @Test
    fun quickStartButtons_invokeCallbacks() {
        var startPractice = false
        var startMock = false
        setHome(
            state = HomeUiState(loading = false),
            onStartPractice = { startPractice = true },
            onStartMock = { startMock = true }
        )

        composeRule.onNodeWithText("Start practice").performClick()
        composeRule.onNodeWithText("Mock test").performClick()

        composeRule.runOnIdle {
            assertTrue(startPractice)
            assertTrue(startMock)
        }
    }

    @Test
    fun drillThis_invokesCallbackWithSection() {
        var drilledSection: String? = null
        setHome(
            state = HomeUiState(
                loading = false,
                weakSections = listOf(
                    SectionWeakness(
                        section = "Numerical Reasoning",
                        wrong = 10,
                        total = 22
                    )
                )
            ),
            onDrillSection = { drilledSection = it }
        )

        composeRule.onNodeWithTag("home_drill_Numerical Reasoning").performClick()

        composeRule.runOnIdle {
            assertEquals("Numerical Reasoning", drilledSection)
        }
    }

    @Test
    fun lastScoreCard_invokesOpenResult() {
        var openedAttemptId: String? = null
        setHome(
            state = populatedState(),
            onOpenLastResult = { openedAttemptId = it }
        )

        composeRule.onNodeWithTag("home_last_score")
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("attempt-1", openedAttemptId)
        }
    }

    @Test
    fun lastScoreCard_hasNoClickActionWithoutAttempt() {
        setHome(HomeUiState(loading = false))

        composeRule.onNodeWithTag("home_last_score").assert(
            SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick)
        )
    }

    @Test
    fun largeFontScale_keepsContentReachableByScrolling() {
        setHome(populatedState(), fontScale = 2f)

        composeRule.onNodeWithText("Start practice")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_drill_Numerical Reasoning")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_ad_slot").assertIsDisplayed()
    }

    private fun setHome(
        state: HomeUiState,
        onStartPractice: () -> Unit = {},
        onStartMock: () -> Unit = {},
        onDrillSection: (String) -> Unit = {},
        onOpenLastResult: (String) -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                HomeContent(
                    state = state,
                    onStartPractice = onStartPractice,
                    onStartMock = onStartMock,
                    onDrillSection = onDrillSection,
                    onOpenLastResult = onOpenLastResult
                )
            }
        }
    }

    private fun populatedState(correctCount: Int = 78) = HomeUiState(
        loading = false,
        lastAttempt = Attempt(
            id = "attempt-1",
            templateId = "UBA_GMAP",
            mode = AttemptMode.MOCK,
            company = "UBA",
            section = null,
            startedAt = 0L,
            completedAt = 4_092_000L,
            deadlineAt = null,
            totalQuestions = 100,
            correctCount = correctCount,
            status = AttemptStatus.COMPLETED
        ),
        weakSections = listOf(
            SectionWeakness(
                section = "Numerical Reasoning",
                wrong = 10,
                total = 22
            ),
            SectionWeakness(
                section = "Data Interpretation",
                wrong = 9,
                total = 23
            )
        ),
        templates = listOf(
            TestTemplate(
                id = "UBA_GMAP",
                title = "UBA GMAP Mock",
                company = "UBA",
                provider = "In-house / MS Forms",
                sourceUrl = null,
                defaults = TestDefaults(),
                sections = emptyList()
            )
        ),
        passMarkPercent = 70
    )
}
