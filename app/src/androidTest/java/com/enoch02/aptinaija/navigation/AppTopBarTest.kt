package com.enoch02.aptinaija.navigation

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTopBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeWithCount_showsBadgeAndInvokesBookmarks() {
        var clicked = false
        composeRule.setContent {
            AptiNaijaTheme {
                AppTopBar(
                    destination = TopLevelDestination.Home,
                    bookmarkCount = 3,
                    onBookmarks = { clicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("3 saved bookmarks")
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(clicked)
        }
    }

    @Test
    fun zeroBookmarks_showsActionWithoutBadge() {
        composeRule.setContent {
            AptiNaijaTheme {
                AppTopBar(
                    destination = TopLevelDestination.Home,
                    bookmarkCount = 0,
                    onBookmarks = {}
                )
            }
        }

        composeRule.onNodeWithText("0").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Saved Questions")
            .assertHasClickAction()
            .assertIsDisplayed()
    }

    @Test
    fun settings_hidesBookmarkAction() {
        composeRule.setContent {
            AptiNaijaTheme {
                AppTopBar(
                    destination = TopLevelDestination.Settings,
                    bookmarkCount = 3,
                    onBookmarks = {}
                )
            }
        }

        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("3 saved bookmarks").assertDoesNotExist()
    }

    @Test
    fun history_hidesBookmarkAction() {
        composeRule.setContent {
            AptiNaijaTheme {
                AppTopBar(
                    destination = TopLevelDestination.History,
                    bookmarkCount = 3,
                    onBookmarks = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("3 saved bookmarks").assertDoesNotExist()
    }
}
