package com.enoch02.aptinaija.feature.results

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.ScoreSummary
import com.enoch02.aptinaija.domain.model.SectionScore
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setResults(ResultsUiState(loading = true))

        composeRule.onNodeWithTag("results_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("results_score").assertDoesNotExist()
    }

    @Test
    fun missingAttempt_showsMessage() {
        setResults(ResultsUiState(loading = false, error = ResultsError.MISSING_ATTEMPT))

        composeRule.onNodeWithTag("results_error").assertIsDisplayed()
        composeRule.onNodeWithText("This attempt no longer exists.").assertIsDisplayed()
    }

    @Test
    fun passedAttempt_showsScoreBadgeAndBreakdown() {
        setResults(passedState())

        composeRule.onNodeWithText("Results").assertIsDisplayed()
        composeRule.onNodeWithTag("results_score").assertIsDisplayed()
        composeRule.onNodeWithText("78%").assertIsDisplayed()
        composeRule.onNodeWithText("Pass · benchmark 70%").assertIsDisplayed()
        composeRule.onNodeWithText("50 of 64 correct · 68:12 of 75:00 used").assertIsDisplayed()
        composeRule.onNodeWithText("Numerical Reasoning").assertIsDisplayed()
        composeRule.onNodeWithText("15/22").assertIsDisplayed()
        composeRule.onNodeWithTag("results_progress_Numerical Reasoning")
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.68f, 0f..1f))
        composeRule.onNodeWithTag("results_ad_slot").assertIsDisplayed()
    }

    @Test
    fun failedAttempt_showsFailBadge() {
        setResults(passedState(percent = 50, correct = 32))

        composeRule.onNodeWithText("50%").assertIsDisplayed()
        composeRule.onNodeWithText("Fail · benchmark 70%").assertIsDisplayed()
    }

    @Test
    fun untimedAttempt_showsOnlyTimeUsed() {
        setResults(passedState(timeLimitSeconds = null))

        composeRule.onNodeWithText("50 of 64 correct · 68:12 used").assertIsDisplayed()
    }

    @Test
    fun buttons_andBack_invokeCallbacks() {
        var triedAgain = false
        var reviewed = false
        var wentBack = false
        setResults(
            passedState(),
            onTryAgain = { triedAgain = true },
            onReview = { reviewed = true },
            onBack = { wentBack = true }
        )

        composeRule.onNodeWithTag("results_try_again").performClick()
        composeRule.onNodeWithTag("results_review").performClick()
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.runOnIdle {
            assertTrue(triedAgain)
            assertTrue(reviewed)
            assertTrue(wentBack)
        }
    }

    @Test
    fun largeFontScale_keepsControlsReachableByScrolling() {
        setResults(passedState(), fontScale = 2f)

        composeRule.onNodeWithTag("results_try_again").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("results_review").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("results_ad_slot").assertIsDisplayed()
    }

    private fun setResults(
        state: ResultsUiState,
        onTryAgain: () -> Unit = {},
        onReview: () -> Unit = {},
        onBack: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                ResultsContent(
                    state = state,
                    onBack = onBack,
                    onTryAgain = onTryAgain,
                    onReview = onReview
                )
            }
        }
    }

    private fun passedState(
        percent: Int = 78,
        correct: Int = 50,
        timeLimitSeconds: Long? = 4_500L
    ) = ResultsUiState(
        loading = false,
        summary = ScoreSummary(
            attemptId = "attempt-1",
            total = 64,
            correct = correct,
            percent = percent,
            sections = listOf(
                SectionScore("Numerical Reasoning", 22, 15),
                SectionScore("Verbal Reasoning", 22, 21),
                SectionScore("Logical Reasoning", 20, 14)
            ),
            timeUsedSeconds = 4_092L,
            passMarkPercent = 70
        ),
        attempt = Attempt(
            id = "attempt-1",
            templateId = "UBA_GMAP",
            mode = AttemptMode.MOCK,
            company = "UBA",
            section = null,
            startedAt = 0L,
            completedAt = 4_092_000L,
            deadlineAt = 4_500_000L,
            totalQuestions = 64,
            correctCount = correct,
            status = AttemptStatus.COMPLETED
        ),
        timeLimitSeconds = timeLimitSeconds
    )
}
