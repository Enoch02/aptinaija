package com.enoch02.aptinaija.feature.wronganswers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question

@Composable
fun WrongAnswersScreen(
    onRetest: (String) -> Unit,
    onStartPractice: () -> Unit,
    viewModel: WrongAnswersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.attemptId) {
        state.attemptId?.let { attemptId ->
            onRetest(attemptId)
            viewModel.consumeAttemptId()
        }
    }

    WrongAnswersContent(
        state = state,
        onRetest = viewModel::startRetest,
        onStartPractice = onStartPractice
    )
}

@Composable
internal fun WrongAnswersContent(
    state: WrongAnswersUiState,
    onRetest: () -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.loading -> LoadingState(modifier.testTag("wrong_answers_loading"))

        state.questions.isEmpty() -> WrongAnswersEmptyState(
            onStartPractice = onStartPractice,
            modifier = modifier
        )

        else -> WrongAnswersList(
            state = state,
            onRetest = onRetest,
            modifier = modifier
        )
    }
}

@Composable
private fun WrongAnswersList(
    state: WrongAnswersUiState,
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
                    R.plurals.wrong_answers_count,
                    state.questions.size,
                    state.questions.size
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("wrong_answers_count")
            )

            state.error?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("wrong_answers_error")
                )
            }

            state.questions.forEach { question ->
                WrongQuestionRow(question = question)
            }
        }

        Button(
            onClick = onRetest,
            enabled = !state.starting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 48.dp)
                .testTag("wrong_answers_retest")
        ) {
            Text(
                stringResource(
                    if (state.starting) R.string.starting else R.string.wrong_answers_retest
                )
            )
        }
    }
}

@Composable
private fun WrongQuestionRow(question: Question) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(vertical = 8.dp)
            .testTag("wrong_answers_question_${question.id}")
    ) {
        Text(
            text = question.section,
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
}

@Composable
private fun WrongAnswersEmptyState(
    onStartPractice: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wrong_answers_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("wrong_answers_empty_title")
        )
        Text(
            text = stringResource(R.string.wrong_answers_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("wrong_answers_empty_body"),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onStartPractice,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("wrong_answers_start_practice")
        ) {
            Text(stringResource(R.string.start_practice))
        }
    }
}

private fun previewQuestions() = listOf(
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

@Preview(name = "Wrong answers · populated")
@Composable
private fun WrongAnswersPopulatedPreview() {
    AptiNaijaTheme {
        WrongAnswersContent(
            state = WrongAnswersUiState(
                loading = false,
                questions = previewQuestions()
            ),
            onRetest = {},
            onStartPractice = {}
        )
    }
}

@Preview(name = "Wrong answers · empty")
@Composable
private fun WrongAnswersEmptyPreview() {
    AptiNaijaTheme {
        WrongAnswersContent(
            state = WrongAnswersUiState(loading = false),
            onRetest = {},
            onStartPractice = {}
        )
    }
}
