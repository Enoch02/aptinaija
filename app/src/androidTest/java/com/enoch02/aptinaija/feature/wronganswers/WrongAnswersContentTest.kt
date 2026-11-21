package com.enoch02.aptinaija.feature.wronganswers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WrongAnswersContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setContent(WrongAnswersUiState(loading = true))

        composeRule.onNodeWithTag("wrong_answers_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("wrong_answers_retest").assertDoesNotExist()
    }

    @Test
    fun populatedState_showsCountAndQuestionRows() {
        setContent(populatedState())

        composeRule.onNodeWithText("2 questions · each clears once you answer it correctly")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("wrong_answers_question_q1").assertIsDisplayed()
        composeRule.onNodeWithText("Numerical Reasoning").assertIsDisplayed()
        composeRule.onNodeWithText("Profit on 30 units sold at ₦3,200…").assertIsDisplayed()
        composeRule.onNodeWithTag("wrong_answers_question_q2").assertIsDisplayed()
        composeRule.onNodeWithText("Data Interpretation").assertIsDisplayed()
        composeRule.onNodeWithText("Which quarter had the highest growth…").assertIsDisplayed()
    }

    @Test
    fun retestButton_invokesCallback() {
        var retested = false
        setContent(populatedState(), onRetest = { retested = true })

        composeRule.onNodeWithTag("wrong_answers_retest").performClick()

        composeRule.runOnIdle { assertTrue(retested) }
    }

    @Test
    fun inlineError_isShownAboveTheList() {
        setContent(populatedState().copy(error = "Could not start the retest."))

        composeRule.onNodeWithTag("wrong_answers_error").assertIsDisplayed()
        composeRule.onNodeWithText("Could not start the retest.").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsCopyAndStartsPractice() {
        var startedPractice = false
        setContent(
            WrongAnswersUiState(loading = false),
            onStartPractice = { startedPractice = true }
        )

        composeRule.onNodeWithTag("wrong_answers_empty_title").assertIsDisplayed()
        composeRule.onNodeWithText("Nothing to redeem yet").assertIsDisplayed()
        composeRule.onNodeWithTag("wrong_answers_empty_body").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Questions you get wrong in practice or mock tests will collect here " +
                "until you answer them correctly."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("wrong_answers_retest").assertDoesNotExist()
        composeRule.onNodeWithTag("wrong_answers_start_practice").performClick()

        composeRule.runOnIdle { assertTrue(startedPractice) }
    }

    @Test
    fun largeFontScale_keepsRetestReachableByScrolling() {
        setContent(populatedState(), fontScale = 2f)

        composeRule.onNodeWithTag("wrong_answers_count").performScrollTo().assertIsDisplayed()
        // The retest button is pinned outside the scrolling list, so it stays
        // reachable without a scroll action at larger font scales.
        composeRule.onNodeWithTag("wrong_answers_retest").assertIsDisplayed()
    }

    private fun setContent(
        state: WrongAnswersUiState,
        onRetest: () -> Unit = {},
        onStartPractice: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                WrongAnswersContent(
                    state = state,
                    onRetest = onRetest,
                    onStartPractice = onStartPractice
                )
            }
        }
    }

    private fun populatedState() = WrongAnswersUiState(
        loading = false,
        questions = listOf(
            Question(
                id = "q1",
                company = "Shell",
                section = "Numerical Reasoning",
                difficulty = Difficulty.MEDIUM,
                stem = "Profit on 30 units sold at ₦3,200…",
                options = listOf("₦18,000", "₦21,000", "₦26,000"),
                answerIndex = 1,
                explanation = "Profit counts only the units sold."
            ),
            Question(
                id = "q2",
                company = "UBA",
                section = "Data Interpretation",
                difficulty = Difficulty.MEDIUM,
                stem = "Which quarter had the highest growth…",
                options = listOf("Q1", "Q2", "Q3", "Q4"),
                answerIndex = 2,
                explanation = "Compare quarter-on-quarter growth."
            )
        )
    )
}
