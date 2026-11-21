package com.enoch02.aptinaija.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.AnswerDeltaRow
import com.enoch02.aptinaija.core.ui.components.AnswerReveal
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.core.ui.components.MessageState
import com.enoch02.aptinaija.core.ui.components.QuestionCard
import com.enoch02.aptinaija.core.ui.components.QuestionOption
import com.enoch02.aptinaija.core.ui.components.ScreenScaffold

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var requestedIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.wrongOnly) {
        requestedIndex = 0
    }

    val currentIndex = requestedIndex.coerceIn(0, maxOf(0, state.items.lastIndex))
    ReviewContent(
        state = state,
        currentIndex = currentIndex,
        onBack = onBack,
        onToggleWrongOnly = viewModel::setWrongOnly,
        onPrevious = { requestedIndex = (currentIndex - 1).coerceAtLeast(0) },
        onNext = { requestedIndex = (currentIndex + 1).coerceAtMost(state.items.lastIndex) },
        onToggleBookmark = viewModel::toggleBookmark
    )
}

@Composable
internal fun ReviewContent(
    state: ReviewUiState,
    currentIndex: Int,
    onBack: () -> Unit,
    onToggleWrongOnly: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    val current = state.items.getOrNull(currentIndex)

    ScreenScaffold(
        title = stringResource(R.string.review),
        onBack = onBack,
        actions = {
            if (current != null) {
                IconButton(
                    onClick = { onToggleBookmark(current.questionId) },
                    modifier = Modifier.testTag("review_bookmark")
                ) {
                    Icon(
                        imageVector = if (current.bookmarked) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = stringResource(
                            if (current.bookmarked) {
                                R.string.remove_bookmark
                            } else {
                                R.string.bookmark_question
                            }
                        ),
                        tint = if (current.bookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        when {
            state.loading -> LoadingState(Modifier.testTag("review_loading"))

            state.error != null -> MessageState(
                message = stringResource(R.string.review_missing_attempt),
                modifier = Modifier.testTag("review_error")
            )

            state.items.isEmpty() -> MessageState(
                message = stringResource(
                    if (state.wrongOnly) R.string.review_empty_wrong else R.string.review_empty
                ),
                modifier = Modifier.testTag("review_empty")
            )

            current != null -> ReviewQuestion(
                state = state,
                current = current,
                currentIndex = currentIndex,
                onToggleWrongOnly = onToggleWrongOnly,
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun ReviewQuestion(
    state: ReviewUiState,
    current: ReviewItemUi,
    currentIndex: Int,
    onToggleWrongOnly: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.wrongOnly,
                    onClick = { onToggleWrongOnly(!state.wrongOnly) },
                    label = { Text(stringResource(R.string.review_wrong_only)) },
                    leadingIcon = if (state.wrongOnly) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.testTag("review_wrong_only")
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.review_questions,
                        state.items.size,
                        state.items.size
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("review_count")
                )
            }

            QuestionCard(
                meta = stringResource(
                    R.string.review_question_meta,
                    current.position + 1,
                    current.section
                ),
                passage = current.passage,
                stem = current.stem,
                options = current.options.mapIndexed { index, text ->
                    QuestionOption(
                        letter = displayLetter(index),
                        text = text,
                        value = index,
                        selected = current.selectedDisplayIndex == index,
                        reveal = when (index) {
                            current.correctDisplayIndex -> AnswerReveal.CORRECT
                            current.selectedDisplayIndex -> AnswerReveal.WRONG
                            else -> null
                        }
                    )
                },
                onSelectOption = {},
                interactive = false,
                testTagPrefix = "review",
                footer = {
                    AnswerDeltaRow(
                        selectedLetter = current.selectedDisplayIndex?.let(::displayLetter),
                        correctLetter = displayLetter(current.correctDisplayIndex),
                        answeredCorrectly = current.isCorrect == true,
                        explanation = current.explanation,
                        testTag = "review_delta",
                        lineTestTag = "review_delta_line"
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("review_previous")
            ) {
                Text(stringResource(R.string.previous))
            }
            Button(
                onClick = onNext,
                enabled = currentIndex < state.items.lastIndex,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("review_next")
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

private fun displayLetter(index: Int): String =
    if (index in 0..25) ('A' + index).toString() else "?"

@Preview
@Composable
private fun ReviewContentPreview() {
    AptiNaijaTheme {
        ReviewContent(
            state = ReviewUiState(
                loading = false,
                items = listOf(
                    ReviewItemUi(
                        questionId = "q1",
                        position = 6,
                        section = "Numerical Reasoning",
                        passage = null,
                        stem = "A trader buys 40 units at ₦2,500 each and sells 30 at ₦3,200 each. " +
                            "What is the profit on the units sold?",
                        options = listOf("₦18,000", "₦21,000", "₦26,000"),
                        selectedDisplayIndex = 2,
                        correctDisplayIndex = 1,
                        isCorrect = false,
                        explanation = "Profit counts only the 30 units sold: ₦96,000 revenue minus ₦75,000 cost.",
                        bookmarked = true
                    )
                )
            ),
            currentIndex = 0,
            onBack = {},
            onToggleWrongOnly = {},
            onPrevious = {},
            onNext = {},
            onToggleBookmark = {}
        )
    }
}
