package com.enoch02.aptinaija.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AttemptMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setHistory(HistoryUiState(loading = true))

        composeRule.onNodeWithTag("history_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("history_trend_card").assertDoesNotExist()
    }

    @Test
    fun emptyState_showsMessageAndStartsPractice() {
        var started = false
        setHistory(
            HistoryUiState(loading = false),
            onStartPractice = { started = true }
        )

        composeRule.onNodeWithText("No completed attempts yet").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Finish a practice session or mock test and your scores will appear here."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("history_start_practice").performClick()

        composeRule.runOnIdle { assertTrue(started) }
    }

    @Test
    fun attempts_showDateTitleScoreAndTrendAndOpenResults() {
        var opened: String? = null
        setHistory(populatedState(), onAttempt = { opened = it })

        composeRule.onNodeWithText("Last 10 scores").assertIsDisplayed()
        composeRule.onNodeWithTag("history_sparkline").assertIsDisplayed()

        composeRule.onNodeWithTag("history_date_a4", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("18 Nov").assertIsDisplayed()
        composeRule.onNodeWithTag("history_title_a4", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("UBA GMAP · Mock").assertIsDisplayed()
        composeRule.onNodeWithTag("history_score_a4", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("78%").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Score improved").assertCountEquals(2)
        composeRule.onNodeWithContentDescription("Score dropped").assertIsDisplayed()

        composeRule.onNodeWithText("Numerical Reasoning drill").assertIsDisplayed()
        composeRule.onNodeWithText("UBA · Practice").assertIsDisplayed()

        composeRule.onNodeWithTag("history_attempt_a4").performClick()
        composeRule.runOnIdle { assertTrue(opened == "a4") }
    }

    @Test
    fun flatTrend_showsUnchangedIcon() {
        setHistory(
            HistoryUiState(
                loading = false,
                entries = listOf(
                    HistoryEntry("a2", "UBA", AttemptMode.MOCK, "12 Nov", 71, HistoryTrend.FLAT)
                )
            )
        )

        composeRule.onNodeWithContentDescription("Score unchanged").assertIsDisplayed()
    }

    @Test
    fun oldestAttempt_hasNoTrendIcon() {
        setHistory(populatedState())

        composeRule.onNodeWithTag("history_trend_a1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("66%").assertIsDisplayed()
    }

    @Test
    fun largeFontScale_keepsOlderAttemptsReachableByScrolling() {
        setHistory(populatedState(), fontScale = 2f)

        composeRule.onNodeWithTag("history_attempt_a1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("history_trend_card").performScrollTo().assertIsDisplayed()
    }

    private fun setHistory(
        state: HistoryUiState,
        onStartPractice: () -> Unit = {},
        onAttempt: (String) -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                HistoryContent(
                    state = state,
                    onStartPractice = onStartPractice,
                    onAttempt = onAttempt
                )
            }
        }
    }

    private fun populatedState() = HistoryUiState(
        loading = false,
        entries = listOf(
            HistoryEntry("a4", "UBA GMAP", AttemptMode.MOCK, "18 Nov", 78, HistoryTrend.UP),
            HistoryEntry("a3", "Numerical Reasoning", AttemptMode.DRILL, "15 Nov", 54, HistoryTrend.DOWN),
            HistoryEntry("a2", "SHL-style", AttemptMode.MOCK, "12 Nov", 71, HistoryTrend.UP),
            HistoryEntry("a1", "UBA", AttemptMode.PRACTICE, "9 Nov", 66, HistoryTrend.NONE)
        )
    )
}
