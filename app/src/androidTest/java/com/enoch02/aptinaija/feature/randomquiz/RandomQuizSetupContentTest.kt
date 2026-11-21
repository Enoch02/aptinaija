package com.enoch02.aptinaija.feature.randomquiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RandomQuizSetupContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setSetup(RandomQuizSetupUiState(loading = true))

        composeRule.onNodeWithTag("random_quiz_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("random_quiz_begin").assertDoesNotExist()
    }

    @Test
    fun defaultState_showsSummaryTimingAndInfo() {
        setSetup(RandomQuizSetupUiState(loading = false, questionCount = 20))

        composeRule.onNodeWithText("Questions and options shuffled").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Questions are drawn at random from the whole question bank. You get one minute per question."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Strict timing").assertIsDisplayed()
        composeRule.onNodeWithText("Begin").assertIsDisplayed()
    }

    @Test
    fun questionCount_invokesCallback() {
        var count: Int? = null
        setSetup(
            RandomQuizSetupUiState(loading = false, questionCount = 20),
            onQuestionCountSelected = { count = it }
        )

        composeRule.onNodeWithTag("setup_questions_40").performClick()

        composeRule.runOnIdle {
            assertEquals(40, count)
        }
    }

    @Test
    fun begin_invokesStartCallback() {
        var started = false
        setSetup(
            RandomQuizSetupUiState(loading = false, questionCount = 20),
            onBegin = { started = true }
        )

        composeRule.onNodeWithTag("random_quiz_begin").performClick()

        composeRule.runOnIdle {
            assertTrue(started)
        }
    }

    @Test
    fun errorState_showsMessage() {
        setSetup(
            RandomQuizSetupUiState(
                loading = false,
                questionCount = 20,
                error = "No questions are available for this selection yet."
            )
        )

        composeRule.onNodeWithTag("random_quiz_error")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("No questions are available for this selection yet.")
            .assertIsDisplayed()
    }

    @Test
    fun startingState_showsStartingAndDisablesBegin() {
        setSetup(RandomQuizSetupUiState(loading = false, questionCount = 20, starting = true))

        composeRule.onNodeWithText("Starting…").assertIsDisplayed()
        composeRule.onNodeWithTag("random_quiz_begin").assertIsNotEnabled()
    }

    @Test
    fun largeFontScale_keepsContentReachable() {
        setSetup(RandomQuizSetupUiState(loading = false, questionCount = 20), fontScale = 2f)

        composeRule.onNodeWithText("Strict timing")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("random_quiz_begin").assertIsDisplayed()
    }

    private fun setSetup(
        state: RandomQuizSetupUiState,
        onQuestionCountSelected: (Int) -> Unit = {},
        onBegin: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                RandomQuizSetupContent(
                    state = state,
                    onBack = {},
                    onQuestionCountSelected = onQuestionCountSelected,
                    onBegin = onBegin
                )
            }
        }
    }
}
