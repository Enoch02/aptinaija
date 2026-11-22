package com.enoch02.aptinaija.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun controls_reflectCurrentSettings() {
        setSettings(
            AppSettings(
                themeMode = AppThemeMode.DARK,
                shuffleQuestions = false,
                shuffleOptions = true,
                defaultQuestionCount = 40
            )
        )

        composeRule.onNodeWithTag("settings_theme_dark").assertIsSelected()
        composeRule.onNodeWithTag("settings_shuffle_questions").assertIsOff()
        composeRule.onNodeWithTag("settings_shuffle_options").assertIsOn()
        composeRule.onNodeWithTag("settings_count_40").assertIsSelected()
    }

    @Test
    fun themeButtons_invokeCallbackWithMode() {
        var mode: AppThemeMode? = null
        setSettings(AppSettings(), onThemeMode = { mode = it })

        composeRule.onNodeWithTag("settings_theme_auto").performClick()
        composeRule.onNodeWithTag("settings_theme_light").performClick()
        composeRule.onNodeWithTag("settings_theme_dark").performClick()

        composeRule.runOnIdle {
            assertEquals(AppThemeMode.DARK, mode)
            assert(mode != null)
        }
    }

    @Test
    fun switches_invokeCallbacks() {
        var questions: Boolean? = null
        var options: Boolean? = null
        setSettings(
            AppSettings(),
            onShuffleQuestions = { questions = it },
            onShuffleOptions = { options = it }
        )

        composeRule.onNodeWithTag("settings_shuffle_questions").performClick()
        composeRule.onNodeWithTag("settings_shuffle_options").performClick()

        composeRule.runOnIdle {
            assert(questions == false)
            assert(options == false)
        }
    }

    @Test
    fun defaultCountButtons_invokeCallback() {
        var count: Int? = null
        setSettings(AppSettings(), onDefaultCount = { count = it })

        composeRule.onNodeWithTag("settings_count_10").performClick()
        composeRule.onNodeWithTag("settings_count_40").performClick()

        composeRule.runOnIdle {
            assertEquals(40, count)
        }
    }

    @Test
    fun adsConsent_opensInformationalDialogAndDismisses() {
        setSettings(AppSettings())

        composeRule.onNodeWithTag("settings_ads_consent").performClick()
        composeRule.onNodeWithText("Ads are not configured in this build.", substring = true)
            .assertIsDisplayed()

        composeRule.onNodeWithTag("settings_ads_ok").performClick()
        composeRule.onNodeWithText("Ads are not configured in this build.", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun openSourceLicensesRow_opensBottomSheet() {
        setSettings(AppSettings())

        composeRule.onNodeWithTag("settings_open_source_licenses_sheet").assertDoesNotExist()

        composeRule.onNodeWithTag("settings_open_source_licenses")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithTag("settings_open_source_licenses_sheet").assertIsDisplayed()
    }

    @Test
    fun openSourceLicensesSheet_dismissesOnSwipeDown() {
        setSettings(AppSettings())

        composeRule.onNodeWithTag("settings_open_source_licenses")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("settings_open_source_licenses_sheet").assertIsDisplayed()

        composeRule.onNodeWithTag("settings_open_source_licenses_sheet").performTouchInput {
            swipeDown(startY = top + 1f, endY = bottom, durationMillis = 300)
        }

        composeRule.onNodeWithTag("settings_open_source_licenses_sheet").assertDoesNotExist()
    }

    @Test
    fun largeFontScale_keepsAllRowsReachableByScrolling() {
        setSettings(AppSettings(), fontScale = 2f)

        composeRule.onNodeWithTag("settings_theme").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_shuffle_questions_row")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings_ads_consent").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_open_source_licenses")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun setSettings(
        settings: AppSettings,
        onThemeMode: (AppThemeMode) -> Unit = {},
        onShuffleQuestions: (Boolean) -> Unit = {},
        onShuffleOptions: (Boolean) -> Unit = {},
        onDefaultCount: (Int) -> Unit = {},
        fontScale: Float = 1f
    ) {
        composeRule.setContent {
            AptiNaijaTheme(fontScale = fontScale) {
                SettingsContent(
                    settings = settings,
                    onThemeMode = onThemeMode,
                    onShuffleQuestions = onShuffleQuestions,
                    onShuffleOptions = onShuffleOptions,
                    onDefaultCount = onDefaultCount
                )
            }
        }
    }
}
