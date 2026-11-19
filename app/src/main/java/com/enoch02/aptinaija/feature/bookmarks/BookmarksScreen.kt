package com.enoch02.aptinaija.feature.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.core.ui.components.ScreenScaffold
import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onRetest: (String) -> Unit,
    onStartPractice: () -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.attemptId) {
        state.attemptId?.let { attemptId ->
            onRetest(attemptId)
            viewModel.consumeAttemptId()
        }
    }

    BookmarksContent(
        state = state,
        onBack = onBack,
        onRemove = viewModel::remove,
        onRetest = viewModel::startRetest,
        onStartPractice = onStartPractice
    )
}

@Composable
internal fun BookmarksContent(
    state: BookmarksUiState,
    onBack: () -> Unit,
    onRemove: (String) -> Unit,
    onRetest: () -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = stringResource(R.string.bookmarks),
        onBack = onBack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        when {
            state.loading -> LoadingState(modifier.testTag("bookmarks_loading"))

            state.questions.isEmpty() -> BookmarksEmptyState(
                onStartPractice = onStartPractice,
                modifier = modifier
            )

            else -> BookmarksList(
                state = state,
                onRemove = onRemove,
                onRetest = onRetest,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BookmarksList(
    state: BookmarksUiState,
    onRemove: (String) -> Unit,
    onRetest: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.bookmarked_questions_count,
                    state.questions.size,
                    state.questions.size
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("bookmarks_count")
            )

            state.error?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("bookmarks_error")
                )
            }

            state.questions.forEach { question ->
                BookmarkRow(question = question, onRemove = onRemove)
            }
        }

        Button(
            onClick = onRetest,
            enabled = !state.starting,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 48.dp)
                .testTag("bookmarks_retest")
        ) {
            Text(
                stringResource(
                    if (state.starting) R.string.starting else R.string.bookmarks_retest
                )
            )
        }
    }
}

@Composable
private fun BookmarkRow(
    question: Question,
    onRemove: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(vertical = 8.dp)
            .testTag("bookmarks_question_${question.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    R.string.bookmark_meta,
                    question.company,
                    question.section
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = question.stem,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = { onRemove(question.id) },
            modifier = Modifier
                .size(48.dp)
                .testTag("bookmarks_remove_${question.id}")
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = stringResource(R.string.remove_bookmark),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BookmarksEmptyState(
    onStartPractice: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.bookmarks_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("bookmarks_empty_title")
        )
        Text(
            text = stringResource(R.string.bookmarks_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("bookmarks_empty_body"),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onStartPractice,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("bookmarks_start_practice")
        ) {
            Text(stringResource(R.string.start_practice))
        }
    }
}

private fun previewQuestions() = listOf(
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

@Preview(name = "Bookmarks · populated")
@Composable
private fun BookmarksPopulatedPreview() {
    AptiNaijaTheme {
        BookmarksContent(
            state = BookmarksUiState(loading = false, questions = previewQuestions()),
            onBack = {},
            onRemove = {},
            onRetest = {},
            onStartPractice = {}
        )
    }
}

@Preview(name = "Bookmarks · empty")
@Composable
private fun BookmarksEmptyPreview() {
    AptiNaijaTheme {
        BookmarksContent(
            state = BookmarksUiState(loading = false),
            onBack = {},
            onRemove = {},
            onRetest = {},
            onStartPractice = {}
        )
    }
}
