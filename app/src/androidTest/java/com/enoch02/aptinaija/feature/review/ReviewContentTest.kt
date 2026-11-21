package com.enoch02.aptinaija.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setReview(ReviewUiState(loading = true))

        composeRule.onNodeWithTag("review_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("review_wrong_only").assertDoesNotExist()
    }

    @Test
    fun missingAttempt_showsMessage() {
        setReview(ReviewUiState(loading = false, error = ReviewError.MISSING_ATTEMPT))

        composeRule.onNodeWithTag("review_error").assertIsDisplayed()
        composeRule.onNodeWithText("This attempt no longer exists.").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsMessage() {
        setReview(ReviewUiState(loading = false))

        composeRule.onNodeWithTag("review_empty").assertIsDisplayed()
        composeRule.onNodeWithText("No questions to review for this attempt.").assertIsDisplayed()
    }

    @Test
    fun wrongOnlyEmptyState_showsMessage() {
        setReview(ReviewUiState(loading = false, wrongOnly = true))

        composeRule.onNodeWithText("No wrong answers in this test.").assertIsDisplayed()
    }

    @Test
    fun answeredQuestion_showsRevealedOptionsAndDelta() {
        setReview(populatedState())

        composeRule.onNodeWithText("Review").assertIsDisplayed()
        composeRule.onNodeWithText("Wrong only").assertIsDisplayed()
        composeRule.onNodeWithText("3 questions").assertIsDisplayed()
        composeRule.onNodeWithText("Question 7 · Numerical Reasoning").assertIsDisplayed()
        composeRule.onNodeWithTag("review_stem").assertIsDisplayed()
        composeRule.onNodeWithTag("review_option_2").assertIsDisplayed()
        composeRule.onNodeWithText("Your answer: C", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Correct: B", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Profit counts only", substring = true).assertIsDisplayed()
    }

    @Test
    fun unansweredQuestion_showsNotAnswered() {
        setReview(populatedState(answered = false))

        composeRule.onNodeWithText("Not answered", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Your answer:", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Correct: B", substring = true).assertIsDisplayed()
    }

    @Test
    fun previous_isDisabledOnFirstQuestion() {
        setReview(populatedState())

        composeRule.onNodeWithTag("review_previous").assertIsNotEnabled()
        composeRule.onNodeWithTag("review_next").assertIsDisplayed()
    }

    @Test
    fun controls_invokeCallbacks() {
        var wrongOnly: Boolean? = null
        var previous = false
        var next = false
        var bookmarkedId: String? = null
        setReview(
            state = populatedState(),
            currentIndex = 1,
            onToggleWrongOnly = { wrongOnly = it },
            onPrevious = { previous = true },
            onNext = { next = true },
            onToggleBookmark = { bookmarkedId = it }
        )

        composeRule.onNodeWithTag("review_wrong_only").performClick()
        composeRule.onNodeWithTag("review_previous").performClick()
        composeRule.onNodeWithTag("review_next").performClick()
        composeRule.onNodeWithContentDescription("Bookmark question").performClick()

        composeRule.runOnIdle {
            assertTrue(wrongOnly == false)
            assertTrue(previous)
            assertTrue(next)
            assertTrue(bookmarkedId == "q2")
        }
    }

    @Test
    fun largeFontScale_keepsQuestionAndControlsReachable() {
        setReview(populatedState(), fontScale = 2f)

        composeRule.onNodeWithTag("review_stem").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("review_delta").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("review_next").assertIsDisplayed()
    }

    private fun setReview(
        state: ReviewUiState,
        currentIndex: Int = 0,
        onToggleWrongOnly: (Boolean) -> Unit = {},
        onPrevious: () -> Unit = {},
        onNext: () -> Unit = {},
        onToggleBookmark: (String) -> Unit = {},
        onBack: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                ReviewContent(
                    state = state,
                    currentIndex = currentIndex,
                    onBack = onBack,
                    onToggleWrongOnly = onToggleWrongOnly,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onToggleBookmark = onToggleBookmark
                )
            }
        }
    }

    private fun populatedState(
        answered: Boolean = true,
        wrongOnly: Boolean = true
    ) = ReviewUiState(
        loading = false,
        wrongOnly = wrongOnly,
        items = listOf(
            ReviewItemUi(
                questionId = "q1",
                position = 6,
                section = "Numerical Reasoning",
                passage = null,
                stem = "A trader buys 40 units at ₦2,500 each and sells 30 at ₦3,200 each. " +
                    "What is the profit on the units sold?",
                options = listOf("₦18,000", "₦21,000", "₦26,000"),
                selectedDisplayIndex = if (answered) 2 else null,
                correctDisplayIndex = 1,
                isCorrect = false,
                explanation = "Profit counts only the 30 units sold: ₦96,000 revenue minus ₦75,000 cost.",
                bookmarked = true
            ),
            ReviewItemUi(
                questionId = "q2",
                position = 7,
                section = "Verbal Reasoning",
                passage = null,
                stem = "Choose the correct word.",
                options = listOf("A", "B"),
                selectedDisplayIndex = 0,
                correctDisplayIndex = 0,
                isCorrect = true,
                explanation = "The sentence calls for a positive word.",
                bookmarked = false
            ),
            ReviewItemUi(
                questionId = "q3",
                position = 8,
                section = "Logical Reasoning",
                passage = null,
                stem = "Which figure comes next?",
                options = listOf("Circle", "Square"),
                selectedDisplayIndex = 1,
                correctDisplayIndex = 0,
                isCorrect = false,
                explanation = "The pattern alternates shapes.",
                bookmarked = false
            )
        )
    )
}
