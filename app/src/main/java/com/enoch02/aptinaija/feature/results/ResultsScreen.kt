package com.enoch02.aptinaija.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.AdSlot
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.core.ui.components.MessageState
import com.enoch02.aptinaija.core.ui.components.ScreenScaffold
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.ScoreSummary
import com.enoch02.aptinaija.domain.model.SectionScore

@Composable
fun ResultsScreen(
    onBack: () -> Unit,
    onTryAgain: (Attempt) -> Unit,
    onReview: (String) -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResultsContent(
        state = state,
        onBack = onBack,
        onTryAgain = { state.attempt?.let(onTryAgain) },
        onReview = { state.summary?.let { onReview(it.attemptId) } }
    )
}

@Composable
internal fun ResultsContent(
    state: ResultsUiState,
    onBack: () -> Unit,
    onTryAgain: () -> Unit,
    onReview: () -> Unit
) {
    ScreenScaffold(
        title = stringResource(R.string.results),
        onBack = onBack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        when {
            state.loading -> LoadingState(Modifier.testTag("results_loading"))

            state.error != null -> MessageState(
                message = stringResource(R.string.results_missing_attempt),
                modifier = Modifier.testTag("results_error")
            )

            state.summary != null -> ResultsBody(
                state = state,
                onTryAgain = onTryAgain,
                onReview = onReview
            )
        }
    }
}

@Composable
private fun ResultsBody(
    state: ResultsUiState,
    onTryAgain: () -> Unit,
    onReview: () -> Unit
) {
    val summary = requireNotNull(state.summary)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreHeader(summary = summary, timeLimitSeconds = state.timeLimitSeconds)

            if (summary.sections.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.results_no_sections),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                summary.sections.forEach { section ->
                    SectionBreakdownRow(section = section)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.showTryAgain) {
                    OutlinedButton(
                        onClick = onTryAgain,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("results_try_again")
                    ) {
                        Text(stringResource(R.string.results_try_again))
                    }
                }
                Button(
                    onClick = onReview,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("results_review")
                ) {
                    Text(stringResource(R.string.results_review_answers))
                }
            }
        }

        AdSlot(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            testTag = "results_ad_slot"
        )
    }
}

@Composable
private fun ScoreHeader(summary: ScoreSummary, timeLimitSeconds: Long?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("results_score_header"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${summary.percent}%",
                style = MaterialTheme.typography.displaySmall.tabular(),
                modifier = Modifier.testTag("results_score")
            )

            val containerColor = if (summary.passed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            val contentColor = if (summary.passed) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            }
            Badge(
                containerColor = containerColor,
                contentColor = contentColor,
                modifier = Modifier.testTag("results_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (summary.passed) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (summary.passed) {
                            stringResource(R.string.results_passed, summary.passMarkPercent)
                        } else {
                            stringResource(R.string.results_failed, summary.passMarkPercent)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.testTag("results_badge_text")
                    )
                }
            }

            val used = formatDuration(summary.timeUsedSeconds)
            Text(
                text = if (timeLimitSeconds != null) {
                    stringResource(
                        R.string.results_score_timed,
                        summary.correct,
                        summary.total,
                        used,
                        formatDuration(timeLimitSeconds)
                    )
                } else {
                    stringResource(R.string.results_score_untimed, summary.correct, summary.total, used)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("results_time")
            )
        }
    }
}

@Composable
private fun SectionBreakdownRow(section: SectionScore) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("results_section_${section.section}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.section,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${section.correct}/${section.total}",
                    style = MaterialTheme.typography.labelLarge.tabular(),
                    modifier = Modifier.testTag("results_section_score_${section.section}")
                )
            }
            LinearProgressIndicator(
                progress = { section.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .testTag("results_progress_${section.section}"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

private fun formatDuration(seconds: Long): String =
    "%d:%02d".format(seconds / 60, seconds % 60)

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Preview
@Composable
private fun ResultsContentPreview() {
    AptiNaijaTheme {
        ResultsContent(
            state = ResultsUiState(
                loading = false,
                summary = ScoreSummary(
                    attemptId = "attempt-1",
                    total = 64,
                    correct = 50,
                    percent = 78,
                    sections = listOf(
                        SectionScore("Numerical Reasoning", 22, 15),
                        SectionScore("Verbal Reasoning", 22, 21),
                        SectionScore("Logical Reasoning", 20, 14)
                    ),
                    timeUsedSeconds = 4092,
                    passMarkPercent = 70
                ),
                attempt = Attempt(
                    id = "attempt-1",
                    templateId = "UBA_GMAP",
                    mode = AttemptMode.MOCK,
                    company = "UBA",
                    section = null,
                    startedAt = 0L,
                    completedAt = 4_092_000L,
                    deadlineAt = 4_500_000L,
                    totalQuestions = 64,
                    correctCount = 50,
                    status = AttemptStatus.COMPLETED
                ),
                timeLimitSeconds = 4500
            ),
            onBack = {},
            onTryAgain = {},
            onReview = {}
        )
    }
}
