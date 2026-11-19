package com.enoch02.aptinaija.feature.bookmarks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class BookmarksContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressAndHidesContent() {
        setContent(BookmarksUiState(loading = true))

        composeRule.onNodeWithTag("bookmarks_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_retest").assertDoesNotExist()
    }

    @Test
    fun populatedState_showsCountMetaAndStems() {
        setContent(populatedState())

        composeRule.onNodeWithText("Bookmarks").assertIsDisplayed()
        composeRule.onNodeWithText("2 bookmarked questions").assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_question_b1").assertIsDisplayed()
        composeRule.onNodeWithText("UBA GMAP · Numerical").assertIsDisplayed()
        composeRule.onNodeWithText("Profit on 30 units sold at ₦3,200…").assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_question_b2").assertIsDisplayed()
        composeRule.onNodeWithText("SHL-style · Inductive").assertIsDisplayed()
        composeRule.onNodeWithText("Which figure completes the pattern…").assertIsDisplayed()
    }

    @Test
    fun rowIcon_removesBookmark() {
        var removedId: String? = null
        setContent(populatedState(), onRemove = { removedId = it })

        composeRule.onNodeWithTag("bookmarks_remove_b2").performClick()

        composeRule.runOnIdle { assertTrue(removedId == "b2") }
    }

    @Test
    fun retestButton_invokesCallback() {
        var retested = false
        setContent(populatedState(), onRetest = { retested = true })

        composeRule.onNodeWithTag("bookmarks_retest").performClick()

        composeRule.runOnIdle { assertTrue(retested) }
    }

    @Test
    fun backButton_invokesCallback() {
        var wentBack = false
        setContent(populatedState(), onBack = { wentBack = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.runOnIdle { assertTrue(wentBack) }
    }

    @Test
    fun emptyState_showsCopyAndStartsPractice() {
        var startedPractice = false
        setContent(
            BookmarksUiState(loading = false),
            onStartPractice = { startedPractice = true }
        )

        composeRule.onNodeWithTag("bookmarks_empty_title").assertIsDisplayed()
        composeRule.onNodeWithText("No bookmarks yet").assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_empty_body").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Bookmark a question during a test and it will collect here for later review."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_retest").assertDoesNotExist()
        composeRule.onNodeWithTag("bookmarks_start_practice").performClick()

        composeRule.runOnIdle { assertTrue(startedPractice) }
    }

    @Test
    fun largeFontScale_keepsRetestReachable() {
        setContent(populatedState(), fontScale = 2f)

        composeRule.onNodeWithTag("bookmarks_count").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("bookmarks_retest").assertIsDisplayed()
    }

    private fun setContent(
        state: BookmarksUiState,
        onBack: () -> Unit = {},
        onRemove: (String) -> Unit = {},
        onRetest: () -> Unit = {},
        onStartPractice: () -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                BookmarksContent(
                    state = state,
                    onBack = onBack,
                    onRemove = onRemove,
                    onRetest = onRetest,
                    onStartPractice = onStartPractice
                )
            }
        }
    }

    private fun populatedState() = BookmarksUiState(
        loading = false,
        questions = listOf(
            Question(
                id = "b1",
                company = "UBA GMAP",
                section = "Numerical",
                difficulty = Difficulty.MEDIUM,
                stem = "Profit on 30 units sold at ₦3,200…",
                options = listOf("₦18,000", "₦21,000", "₦26,000"),
                answerIndex = 1,
                explanation = "Profit counts only the units sold."
            ),
            Question(
                id = "b2",
                company = "SHL-style",
                section = "Inductive",
                difficulty = Difficulty.MEDIUM,
                stem = "Which figure completes the pattern…",
                options = listOf("A", "B", "C", "D"),
                answerIndex = 2,
                explanation = "Look for the rotation rule."
            )
        )
    )
}
