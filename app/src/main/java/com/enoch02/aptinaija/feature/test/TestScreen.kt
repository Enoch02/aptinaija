package com.enoch02.aptinaija.feature.test

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.AnswerDeltaRow
import com.enoch02.aptinaija.core.ui.components.AnswerReveal
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.core.ui.components.QuestionCard
import com.enoch02.aptinaija.core.ui.components.QuestionOption
import com.enoch02.aptinaija.core.ui.components.MessageState
import com.enoch02.aptinaija.domain.model.AttemptMode

@Composable
fun TestScreen(
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    viewModel: TestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.finishedAttemptId) {
        state.finishedAttemptId?.let(onFinished)
    }

    val running = !state.loading &&
        state.error == null &&
        state.questions.isNotEmpty() &&
        state.finishedAttemptId == null

    // Navigation flow §7: back during a test opens the exit dialog.
    BackHandler(enabled = running) { showExitDialog = true }

    TestContent(
        state = state,
        onClose = { if (running) showExitDialog = true else onBack() },
        onSelectOption = viewModel::selectOption,
        onPrevious = viewModel::previous,
        onNext = viewModel::next,
        onToggleBookmark = viewModel::toggleBookmark
    )

    if (showExitDialog) {
        ExitTestDialog(
            onKeepGoing = { showExitDialog = false },
            onSubmitAndExit = {
                showExitDialog = false
                viewModel.finish()
            }
        )
    }
}

@Composable
internal fun TestContent(
    state: TestUiState,
    onClose: () -> Unit,
    onSelectOption: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val error = state.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .semantics { paneTitle = state.title }
    ) {
        TestTopBar(
            state = state,
            onClose = onClose,
            onToggleBookmark = onToggleBookmark
        )

        when {
            state.loading -> LoadingState(
                Modifier
                    .weight(1f)
                    .testTag("test_loading")
            )

            error != null -> MessageState(
                error,
                Modifier
                    .weight(1f)
                    .testTag("test_error")
            )

            else -> {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(4.dp)
                        .testTag("test_progress"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )

                val question = state.current
                if (question == null) {
                    MessageState(
                        stringResource(R.string.attempt_empty),
                        Modifier
                            .weight(1f)
                            .testTag("test_error")
                    )
                } else {
                    QuestionBody(
                        state = state,
                        question = question,
                        onSelectOption = onSelectOption,
                        modifier = Modifier.weight(1f)
                    )
                    TestBottomBar(
                        isLast = state.isLast,
                        isAtStart = state.currentIndex == 0,
                        onPrevious = onPrevious,
                        onNext = onNext
                    )
                }
            }
        }
    }
}

@Composable
private fun TestTopBar(
    state: TestUiState,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose, modifier = Modifier.testTag("test_close")) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.close_test)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val remaining = state.remainingSeconds
        if (remaining != null && state.totalSeconds != null) {
            TimerPill(
                remainingSeconds = remaining,
                level = state.timerLevel ?: TimerLevel.NORMAL,
                milestone = state.timerMilestone,
                modifier = Modifier.testTag("test_timer")
            )
        } else {
            Text(
                text = when (state.mode) {
                    AttemptMode.PRACTICE -> stringResource(R.string.practice_untimed)
                    AttemptMode.DRILL -> stringResource(R.string.drill_untimed)
                    AttemptMode.MOCK -> stringResource(R.string.mock_untimed)
                    AttemptMode.RANDOM_QUIZ -> stringResource(R.string.random_quiz_untimed)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("test_mode_label")
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onToggleBookmark, modifier = Modifier.testTag("test_bookmark")) {
            Icon(
                imageVector = if (state.isBookmarked) {
                    Icons.Filled.Bookmark
                } else {
                    Icons.Outlined.BookmarkBorder
                },
                contentDescription = stringResource(
                    if (state.isBookmarked) R.string.remove_bookmark else R.string.bookmark_question
                ),
                tint = if (state.isBookmarked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * [DEVIATION D4/D5] M3 has no countdown component (see design system §2.4).
 * Warning adds outline + a bolder label; critical pulses every 1.5s.
 */
@Composable
internal fun TimerPill(
    remainingSeconds: Long,
    level: TimerLevel,
    milestone: TimerMilestone?,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val critical = level == TimerLevel.CRITICAL
    val border = if (level == TimerLevel.WARNING) {
        BorderStroke(1.5.dp, colors.primary)
    } else {
        null
    }
    val style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = if (level == TimerLevel.WARNING) FontWeight.Bold else FontWeight.Medium,
        fontFeatureSettings = "tnum"
    )
    val pulseAlpha = rememberPulseAlpha(enabled = critical)

    Box(modifier = modifier) {
        Surface(
            shape = CircleShape,
            color = if (critical) colors.errorContainer else colors.primaryContainer,
            contentColor = if (critical) colors.onErrorContainer else colors.onPrimaryContainer,
            border = border,
            modifier = Modifier.alpha(pulseAlpha)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Update,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(text = formatClock(remainingSeconds), style = style)
            }
        }

        // FR-06 / NFR-08: TalkBack hears only the 20% and 10% warnings, never the seconds.
        Text(
            text = when (milestone) {
                TimerMilestone.TWENTY_PERCENT -> stringResource(R.string.timer_warning_20)
                TimerMilestone.TEN_PERCENT -> stringResource(R.string.timer_warning_10)
                null -> " "
            },
            fontSize = 1.sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(1.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

@Composable
private fun rememberPulseAlpha(enabled: Boolean): Float {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
    if (!enabled || !animationsEnabled) return 1f

    val transition = rememberInfiniteTransition(label = "timer_pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse"
    )
    return alpha
}

@Composable
private fun QuestionBody(
    state: TestUiState,
    question: TestQuestionUi,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    QuestionCard(
        meta = stringResource(
            R.string.question_meta,
            question.position + 1,
            state.total,
            question.section
        ),
        passage = question.passage,
        stem = question.stem,
        options = question.options.mapIndexed { index, option ->
            val selected = option.originalIndex == question.selectedOriginal
            val reveal = when {
                !state.revealFeedback -> null
                option.originalIndex == question.correctOriginal -> AnswerReveal.CORRECT
                selected -> AnswerReveal.WRONG
                else -> null
            }
            QuestionOption(
                letter = ('A' + index).toString(),
                text = option.text,
                value = option.originalIndex,
                selected = selected,
                reveal = reveal
            )
        },
        onSelectOption = onSelectOption,
        interactive = state.canChangeAnswer,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .testTag("test_content")
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp),
        footer = if (state.revealFeedback) {
            {
                AnswerDeltaRow(
                    selectedLetter = optionLetter(question, question.selectedOriginal),
                    correctLetter = optionLetter(question, question.correctOriginal),
                    answeredCorrectly = question.selectedOriginal == question.correctOriginal,
                    explanation = question.explanation,
                    testTag = "test_feedback",
                    lineTestTag = "test_feedback_line"
                )
            }
        } else {
            null
        }
    )
}

@Composable
private fun TestBottomBar(
    isLast: Boolean,
    isAtStart: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isAtStart,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag("test_previous")
        ) {
            Text(stringResource(R.string.previous))
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag(if (isLast) "test_submit" else "test_next")
        ) {
            Text(stringResource(if (isLast) R.string.submit else R.string.next))
        }
    }
}

@Composable
internal fun ExitTestDialog(
    onKeepGoing: () -> Unit,
    onSubmitAndExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepGoing,
        shape = MaterialTheme.shapes.large,
        title = {
            Text(
                text = stringResource(R.string.exit_test_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.exit_test_body),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        dismissButton = {
            TextButton(onClick = onKeepGoing, modifier = Modifier.testTag("test_exit_keep")) {
                Text(stringResource(R.string.keep_going))
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmitAndExit,
                modifier = Modifier.testTag("test_exit_submit")
            ) {
                Text(stringResource(R.string.submit_and_exit))
            }
        }
    )
}

private fun optionLetter(question: TestQuestionUi, originalIndex: Int?): String {
    val index = question.options.indexOfFirst { it.originalIndex == originalIndex }
    return if (index >= 0) ('A' + index).toString() else "?"
}

private fun formatClock(totalSeconds: Long): String {
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

private fun sampleQuestions(selectedOriginal: Int? = null): List<TestQuestionUi> = listOf(
    TestQuestionUi(
        questionId = "q1",
        position = 0,
        section = "Numerical Reasoning",
        passage = null,
        stem = "A trader buys 40 units at ₦2,500 each and sells 30 of them at ₦3,200 each. " +
            "What is the profit on the units sold?",
        options = listOf(
            TestOptionUi(originalIndex = 0, text = "₦18,000"),
            TestOptionUi(originalIndex = 1, text = "₦21,000"),
            TestOptionUi(originalIndex = 2, text = "₦26,000"),
            TestOptionUi(originalIndex = 3, text = "₦96,000")
        ),
        correctOriginal = 1,
        explanation = "Profit is on the 30 units sold: revenue ₦96,000 minus cost ₦75,000 gives ₦21,000.",
        selectedOriginal = selectedOriginal,
        isCorrect = selectedOriginal?.let { it == 1 }
    ),
    TestQuestionUi(
        questionId = "q2",
        position = 1,
        section = "Numerical Reasoning",
        passage = "A trader restocks every week and keeps 15% of revenue as running cost.",
        stem = "Which week had the highest running cost?",
        options = listOf(
            TestOptionUi(originalIndex = 0, text = "Week 2"),
            TestOptionUi(originalIndex = 1, text = "Week 4"),
            TestOptionUi(originalIndex = 2, text = "Week 5")
        ),
        correctOriginal = 0,
        explanation = "Week 2 had the highest revenue, so 15% of it is the highest cost.",
        selectedOriginal = null,
        isCorrect = null
    )
)

@Preview(name = "Mock · dark", showBackground = true)
@Composable
private fun TestContentMockPreview() {
    AptiNaijaTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TestContent(
                state = TestUiState(
                    loading = false,
                    mode = AttemptMode.MOCK,
                    title = "UBA GMAP mock test",
                    questions = sampleQuestions(),
                    currentIndex = 0,
                    remainingSeconds = 2700L,
                    totalSeconds = 4500L
                ),
                onClose = {},
                onSelectOption = {},
                onPrevious = {},
                onNext = {},
                onToggleBookmark = {}
            )
        }
    }
}

@Preview(name = "Practice feedback · light", showBackground = true)
@Composable
private fun TestContentPracticePreview() {
    AptiNaijaTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TestContent(
                state = TestUiState(
                    loading = false,
                    mode = AttemptMode.PRACTICE,
                    title = "Practice",
                    questions = sampleQuestions(selectedOriginal = 2),
                    currentIndex = 0
                ),
                onClose = {},
                onSelectOption = {},
                onPrevious = {},
                onNext = {},
                onToggleBookmark = {}
            )
        }
    }
}
