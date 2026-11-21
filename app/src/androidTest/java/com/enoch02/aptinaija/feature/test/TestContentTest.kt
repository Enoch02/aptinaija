package com.enoch02.aptinaija.feature.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AttemptMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgress() {
        setTest(TestUiState(loading = true))

        composeRule.onNodeWithTag("test_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("test_progress").assertDoesNotExist()
    }

    @Test
    fun errorState_showsMessage() {
        setTest(TestUiState(loading = false, error = "This attempt no longer exists."))

        composeRule.onNodeWithTag("test_error").assertIsDisplayed()
        composeRule.onNodeWithText("This attempt no longer exists.").assertIsDisplayed()
    }

    @Test
    fun mockState_showsTimerInsteadOfModeLabel() {
        setTest(
            baseState().copy(
                mode = AttemptMode.MOCK,
                remainingSeconds = 2700L,
                totalSeconds = 4500L
            )
        )

        composeRule.onNodeWithTag("test_timer").assertIsDisplayed()
        composeRule.onNodeWithText("45:00").assertIsDisplayed()
        composeRule.onNodeWithTag("test_mode_label").assertDoesNotExist()
        composeRule.onNodeWithText("Question 1 of 3 · Numerical Reasoning").assertIsDisplayed()
    }

    @Test
    fun randomQuizState_showsTimerInsteadOfModeLabel() {
        setTest(
            baseState().copy(
                mode = AttemptMode.RANDOM_QUIZ,
                remainingSeconds = 1200L,
                totalSeconds = 1200L
            )
        )

        composeRule.onNodeWithTag("test_timer").assertIsDisplayed()
        composeRule.onNodeWithTag("test_mode_label").assertDoesNotExist()
    }

    @Test
    fun practiceState_showsUntimedLabelAndNoTimer() {
        setTest(baseState())

        composeRule.onNodeWithText("Practice · untimed").assertIsDisplayed()
        composeRule.onNodeWithTag("test_timer").assertDoesNotExist()
    }

    @Test
    fun drillState_showsDrillLabel() {
        setTest(baseState().copy(mode = AttemptMode.DRILL))

        composeRule.onNodeWithText("Section drill · untimed").assertIsDisplayed()
    }

    @Test
    fun mockState_withoutTemplateTime_showsUntimedLabel() {
        setTest(baseState().copy(mode = AttemptMode.MOCK))

        composeRule.onNodeWithText("Mock · untimed").assertIsDisplayed()
        composeRule.onNodeWithTag("test_timer").assertDoesNotExist()
    }

    @Test
    fun selectingOption_invokesCallbackWithOriginalIndex() {
        var chosen: Int? = null
        setTest(baseState(), onSelectOption = { chosen = it })

        // Options are shuffled: the first row maps back to original index 2.
        composeRule.onNodeWithTag("test_option_0").performClick()

        composeRule.runOnIdle {
            assertEquals(2, chosen)
        }
    }

    @Test
    fun mockState_keepsAnsweredQuestionEditable() {
        var chosen: Int? = null
        setTest(
            baseState().copy(mode = AttemptMode.MOCK).withSelection(question = 0, option = 0),
            onSelectOption = { chosen = it }
        )

        composeRule.onNodeWithTag("test_option_1").performClick()

        composeRule.runOnIdle {
            assertEquals(1, chosen)
        }
    }

    @Test
    fun randomQuizState_keepsAnsweredQuestionEditableAndHidesFeedback() {
        var chosen: Int? = null
        setTest(
            baseState().copy(mode = AttemptMode.RANDOM_QUIZ).withSelection(question = 0, option = 0),
            onSelectOption = { chosen = it }
        )

        composeRule.onNodeWithTag("test_option_1").performClick()

        composeRule.runOnIdle {
            assertEquals(1, chosen)
        }
        composeRule.onNodeWithTag("test_feedback").assertDoesNotExist()
    }

    @Test
    fun practiceState_locksAnsweredQuestionAndShowsFeedback() {
        var chosen: Int? = null
        setTest(
            baseState().withSelection(question = 0, option = 0),
            onSelectOption = { chosen = it }
        )

        composeRule.onNodeWithTag("test_option_0").assertHasNoClickAction()
        composeRule.onNodeWithTag("test_feedback").assertIsDisplayed()
        composeRule.onNodeWithText("Week 2 had the highest revenue", substring = true)
            .assertIsDisplayed()
        assertNull(chosen)
    }

    @Test
    fun practiceState_unansweredQuestionShowsNoFeedback() {
        setTest(baseState())

        composeRule.onNodeWithTag("test_feedback").assertDoesNotExist()
        composeRule.onNodeWithTag("test_option_0").assertHasClickAction()
    }

    @Test
    fun previous_isDisabledOnFirstQuestion() {
        setTest(baseState())

        composeRule.onNodeWithTag("test_previous").assertIsNotEnabled()
    }

    @Test
    fun previous_invokesCallbackOnLaterQuestion() {
        var previousCount = 0
        setTest(baseState().copy(currentIndex = 1), onPrevious = { previousCount++ })

        composeRule.onNodeWithTag("test_previous").assertHasClickAction()
        composeRule.onNodeWithTag("test_previous").performClick()

        composeRule.runOnIdle {
            assertEquals(1, previousCount)
        }
    }

    @Test
    fun lastQuestion_showsSubmitAndInvokesNextCallback() {
        var nextCount = 0
        setTest(baseState().copy(currentIndex = 2), onNext = { nextCount++ })

        composeRule.onNodeWithTag("test_submit").assertIsDisplayed()
        composeRule.onNodeWithTag("test_next").assertDoesNotExist()
        composeRule.onNodeWithText("Submit").assertIsDisplayed()
        composeRule.onNodeWithTag("test_submit").performClick()

        composeRule.runOnIdle {
            assertEquals(1, nextCount)
        }
    }

    @Test
    fun bookmarkButton_invokesCallback() {
        var toggled = false
        setTest(baseState(), onToggleBookmark = { toggled = true })

        composeRule.onNodeWithTag("test_bookmark").performClick()

        composeRule.runOnIdle {
            assertTrue(toggled)
        }
    }

    @Test
    fun close_invokesCallback() {
        var closed = false
        setTest(baseState(), onClose = { closed = true })

        composeRule.onNodeWithTag("test_close").performClick()

        composeRule.runOnIdle {
            assertTrue(closed)
        }
    }

    @Test
    fun progressIndicator_isShown() {
        setTest(baseState())

        composeRule.onNodeWithTag("test_progress").assertIsDisplayed()
    }

    @Test
    fun passage_collapsesByDefaultAndExpandsOnTap() {
        setTest(baseState().copy(questions = listOf(questionWithPassage())))

        composeRule.onNodeWithTag("test_passage").assertIsDisplayed()
        composeRule.onNodeWithTag("test_passage_body").assertDoesNotExist()

        composeRule.onNodeWithText("Reading passage").performClick()

        composeRule.onNodeWithTag("test_passage_body").assertIsDisplayed()
    }

    @Test
    fun largeFontScale_keepsQuestionContentReachable() {
        setTest(
            baseState().copy(currentIndex = 2).withSelection(question = 2, option = 0),
            fontScale = 2f
        )

        composeRule.onNodeWithTag("test_stem")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("test_feedback")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("test_progress").assertIsDisplayed()
    }

    @Test
    fun exitDialog_invokesCallbacks() {
        var keptGoing = false
        var submitted = false
        composeRule.setContent {
            AptiNaijaTheme {
                ExitTestDialog(
                    onKeepGoing = { keptGoing = true },
                    onSubmitAndExit = { submitted = true }
                )
            }
        }

        composeRule.onNodeWithText("Exit test?").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Your progress will be submitted and scored. Unanswered questions count as incorrect."
        ).assertIsDisplayed()

        composeRule.onNodeWithText("Keep going").performClick()
        composeRule.runOnIdle {
            assertTrue(keptGoing)
            assertFalse(submitted)
        }
    }

    @Test
    fun exitDialog_submitInvokesFinishCallback() {
        var submitted = false
        composeRule.setContent {
            AptiNaijaTheme {
                ExitTestDialog(
                    onKeepGoing = {},
                    onSubmitAndExit = { submitted = true }
                )
            }
        }

        composeRule.onNodeWithText("Submit and exit").performClick()

        composeRule.runOnIdle {
            assertTrue(submitted)
        }
    }

    private fun setTest(
        state: TestUiState,
        onClose: () -> Unit = {},
        onSelectOption: (Int) -> Unit = {},
        onPrevious: () -> Unit = {},
        onNext: () -> Unit = {},
        onToggleBookmark: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                TestContent(
                    state = state,
                    onClose = onClose,
                    onSelectOption = onSelectOption,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onToggleBookmark = onToggleBookmark
                )
            }
        }
    }

    private fun baseState() = TestUiState(
        loading = false,
        mode = AttemptMode.PRACTICE,
        title = "Practice",
        questions = listOf(
            question(
                id = "q1",
                position = 0,
                options = listOf(
                    TestOptionUi(originalIndex = 2, text = "₦26,000"),
                    TestOptionUi(originalIndex = 1, text = "₦21,000"),
                    TestOptionUi(originalIndex = 0, text = "₦18,000"),
                    TestOptionUi(originalIndex = 3, text = "₦96,000")
                ),
                correctOriginal = 1
            ),
            question(id = "q2", position = 1),
            question(id = "q3", position = 2)
        ),
        currentIndex = 0
    )

    private fun TestUiState.withSelection(question: Int, option: Int): TestUiState {
        val updated = questions.mapIndexed { index, item ->
            if (index == question) {
                item.copy(
                    selectedOriginal = option,
                    isCorrect = option == item.correctOriginal
                )
            } else {
                item
            }
        }
        return copy(questions = updated)
    }

    private fun questionWithPassage() = question(id = "qp", position = 0)
        .copy(passage = "A trader restocks every week and keeps 15% of revenue as running cost.")

    private fun question(
        id: String,
        position: Int,
        options: List<TestOptionUi> = listOf(
            TestOptionUi(originalIndex = 0, text = "Week 1"),
            TestOptionUi(originalIndex = 1, text = "Week 2"),
            TestOptionUi(originalIndex = 2, text = "Week 3")
        ),
        correctOriginal: Int = 0
    ) = TestQuestionUi(
        questionId = id,
        position = position,
        section = "Numerical Reasoning",
        passage = null,
        stem = "Which week had the highest running cost?",
        options = options,
        correctOriginal = correctOriginal,
        explanation = "Week 2 had the highest revenue, so 15% of it is the highest cost.",
        selectedOriginal = null,
        isCorrect = null
    )
}
