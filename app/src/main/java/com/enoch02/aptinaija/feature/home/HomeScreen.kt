package com.enoch02.aptinaija.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate

@Composable
fun HomeScreen(
    onStartPractice: () -> Unit,
    onStartMock: () -> Unit,
    onDrillSection: (String) -> Unit,
    onOpenLastResult: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onStartPractice = onStartPractice,
        onStartMock = onStartMock,
        onDrillSection = onDrillSection,
        onOpenLastResult = onOpenLastResult
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onStartPractice: () -> Unit,
    onStartMock: () -> Unit,
    onDrillSection: (String) -> Unit,
    onOpenLastResult: (String) -> Unit
) {
    if (state.loading) {
        LoadingState(Modifier.testTag("home_loading"))
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LastScoreCard(
                state = state,
                onOpenLastResult = onOpenLastResult
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.start_practice))
                }
                OutlinedButton(
                    onClick = onStartMock,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.mock_test))
                }
            }

            Text(
                text = stringResource(R.string.weakest_sections),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (state.weakSections.isEmpty()) {
                EmptyWeakSectionsCard()
            } else {
                state.weakSections.forEach { weakness ->
                    WeakSectionCard(
                        weakness = weakness,
                        onDrill = { onDrillSection(weakness.section) }
                    )
                }
            }
        }

        AdSlot(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .testTag("home_ad_slot")
        )
    }
}

@Composable
private fun LastScoreCard(
    state: HomeUiState,
    onOpenLastResult: (String) -> Unit
) {
    val attempt = state.lastAttempt
    val template = attempt?.let { last ->
        state.templates.firstOrNull { it.id == last.templateId }
    }
    val shape = MaterialTheme.shapes.medium

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (attempt != null) {
                    Modifier.clickable { onOpenLastResult(attempt.id) }
                } else {
                    Modifier
                }
            )
            .testTag("home_last_score"),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (attempt == null) {
                    stringResource(R.string.last_score)
                } else {
                    stringResource(
                        R.string.last_score_with_title,
                        template?.title ?: attempt.titleFallback()
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = attempt?.let { "${it.percent}%" } ?: "—",
                style = MaterialTheme.typography.displaySmall.tabular()
            )
            Text(
                text = if (attempt == null) {
                    stringResource(R.string.no_completed_attempts)
                } else {
                    val timeUsed = formatDuration(attempt.startedAt, attempt.completedAt)
                    if (attempt.percent >= state.passMarkPercent) {
                        stringResource(
                            R.string.passed_benchmark,
                            state.passMarkPercent,
                            timeUsed
                        )
                    } else {
                        stringResource(
                            R.string.failed_benchmark,
                            state.passMarkPercent,
                            timeUsed
                        )
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeakSectionCard(
    weakness: SectionWeakness,
    onDrill: () -> Unit
) {
    val percent = if (weakness.total == 0) {
        0
    } else {
        ((weakness.total - weakness.wrong) * 100) / weakness.total
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = weakness.section,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleSmall.tabular()
                )
            }
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .testTag("home_progress_${weakness.section}"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            AssistChip(
                onClick = onDrill,
                label = { Text(stringResource(R.string.drill_this)) },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("home_drill_${weakness.section}")
            )
        }
    }
}

@Composable
private fun EmptyWeakSectionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Text(
            text = stringResource(R.string.no_weak_sections),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun AdSlot(modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 4.dp.toPx())
                        )
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.ad_slot),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Attempt.titleFallback(): String = when (mode) {
    AttemptMode.MOCK -> stringResource(R.string.mode_mock)
    AttemptMode.DRILL -> stringResource(R.string.mode_drill)
    AttemptMode.PRACTICE -> stringResource(R.string.mode_practice)
}

private fun formatDuration(startedAt: Long, completedAt: Long?): String {
    val seconds = ((completedAt ?: startedAt) - startedAt).coerceAtLeast(0) / 1000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Preview
@Composable
private fun HomeContentPreview() {
    HomeContent(
        state = HomeUiState(
            loading = false,
            lastAttempt = Attempt(
                id = "attempt-1",
                templateId = "UBA_GMAP",
                mode = AttemptMode.MOCK,
                company = "UBA",
                section = null,
                startedAt = 0,
                completedAt = 4_092_000,
                deadlineAt = null,
                totalQuestions = 100,
                correctCount = 78,
                status = AttemptStatus.COMPLETED
            ),
            weakSections = listOf(
                SectionWeakness(section = "Numerical Reasoning", wrong = 10, total = 22),
                SectionWeakness(section = "Data Interpretation", wrong = 9, total = 23)
            ),
            templates = listOf(
                TestTemplate(
                    id = "UBA_GMAP",
                    title = "UBA GMAP Mock",
                    company = "UBA",
                    provider = "In-house / MS Forms",
                    sourceUrl = null,
                    defaults = TestDefaults(),
                    sections = emptyList()
                )
            )
        ),
        onStartPractice = {},
        onStartMock = {},
        onDrillSection = {},
        onOpenLastResult = {}
    )
}
