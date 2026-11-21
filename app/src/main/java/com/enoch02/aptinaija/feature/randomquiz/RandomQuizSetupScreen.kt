package com.enoch02.aptinaija.feature.randomquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.InfoCard
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.core.ui.components.QuestionCountSelector
import com.enoch02.aptinaija.core.ui.components.ScreenScaffold
import com.enoch02.aptinaija.core.ui.components.SummaryCard

@Composable
fun RandomQuizSetupScreen(
    onBack: () -> Unit,
    onStarted: (String) -> Unit,
    viewModel: RandomQuizSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.attemptId) {
        state.attemptId?.let(onStarted)
    }

    RandomQuizSetupContent(
        state = state,
        onBack = onBack,
        onQuestionCountSelected = viewModel::selectQuestionCount,
        onBegin = viewModel::start
    )
}

@Composable
internal fun RandomQuizSetupContent(
    state: RandomQuizSetupUiState,
    onBack: () -> Unit,
    onQuestionCountSelected: (Int) -> Unit,
    onBegin: () -> Unit
) {
    ScreenScaffold(
        title = stringResource(R.string.random_quiz),
        onBack = onBack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        if (state.loading) {
            LoadingState(Modifier.testTag("random_quiz_loading"))
            return@ScreenScaffold
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = stringResource(R.string.random_quiz_summary),
                    questionCount = state.questionCount,
                    minutes = state.minutes,
                    description = stringResource(R.string.shuffled_summary)
                )
                InfoCard(
                    title = stringResource(R.string.random_quiz),
                    body = stringResource(R.string.random_quiz_mode_body)
                )
                InfoCard(
                    title = stringResource(R.string.strict_timing),
                    body = stringResource(R.string.strict_timing_body)
                )
                QuestionCountSelector(
                    selected = state.questionCount,
                    onSelected = onQuestionCountSelected
                )
                state.error?.let { message -> ErrorBanner(message) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onBegin,
                    enabled = !state.starting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("random_quiz_begin")
                ) {
                    if (state.starting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.starting))
                    } else {
                        Text(stringResource(R.string.begin))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("random_quiz_error"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun RandomQuizSetupContentPreview() {
    AptiNaijaTheme {
        RandomQuizSetupContent(
            state = RandomQuizSetupUiState(loading = false, questionCount = 20),
            onBack = {},
            onQuestionCountSelected = {},
            onBegin = {}
        )
    }
}
