package com.enoch02.aptinaija.feature.history

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.LoadingState
import com.enoch02.aptinaija.domain.model.AttemptMode

@Composable
fun HistoryScreen(
    onStartPractice: () -> Unit,
    onAttempt: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryContent(
        state = state,
        onStartPractice = onStartPractice,
        onAttempt = onAttempt
    )
}

@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onStartPractice: () -> Unit,
    onAttempt: (String) -> Unit
) {
    when {
        state.loading -> LoadingState(Modifier.testTag("history_loading"))

        state.entries.isEmpty() -> HistoryEmptyState(
            onStartPractice = onStartPractice
        )

        else -> HistoryBody(
            entries = state.entries,
            onAttempt = onAttempt
        )
    }
}

@Composable
private fun HistoryBody(
    entries: List<HistoryEntry>,
    onAttempt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TrendCard(scores = entries.take(10).map { it.percent }.reversed())

        entries.forEach { entry ->
            AttemptListItem(
                entry = entry,
                onClick = { onAttempt(entry.id) }
            )
        }
    }
}

@Composable
private fun TrendCard(scores: List<Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_trend_card"),
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
                text = stringResource(R.string.history_trend_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TrendSparkline(
                scores = scores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("history_sparkline")
            )
        }
    }
}

@Composable
private fun TrendSparkline(scores: List<Int>, modifier: Modifier = Modifier) {
    val strokeWidth = with(LocalDensity.current) { 3.dp.toPx() }
    val color = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Score trend sparkline over last ${scores.size} attempts"
        },
        onDraw = {
            if (scores.isEmpty()) return@Canvas

            val inset = strokeWidth / 2f
            val usableHeight = (size.height - strokeWidth).coerceAtLeast(0f)
            val points = scores.mapIndexed { index, score ->
                val x = if (scores.size == 1) {
                    size.width / 2f
                } else {
                    index * size.width / (scores.size - 1).toFloat()
                }
                val y = size.height - inset - (score / 100f) * usableHeight
                Offset(x = x, y = y.coerceIn(inset, size.height - inset))
            }

            if (points.size == 1) {
                drawCircle(color = color, radius = strokeWidth, center = points.first())
            } else {
                points.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    )
}

@Composable
private fun AttemptListItem(entry: HistoryEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .testTag("history_attempt_${entry.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .testTag("history_date_${entry.id}")
                    .padding(start = 4.dp)
            )
            val title = if (entry.subject.isBlank()) {
                stringResource(entry.mode.labelRes())
            } else {
                stringResource(entry.titleRes(), entry.subject)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .testTag("history_title_${entry.id}")
                    .padding(start = 4.dp)
            )
        }
        Text(
            text = "${entry.percent}%",
            style = MaterialTheme.typography.titleMedium.tabular(),
            modifier = Modifier.testTag("history_score_${entry.id}")
        )
        TrendIcon(
            trend = entry.trend,
            modifier = Modifier
                .size(20.dp)
                .testTag("history_trend_${entry.id}")
                .padding(end = 2.dp)
        )
    }
}

@Composable
private fun TrendIcon(trend: HistoryTrend, modifier: Modifier = Modifier) {
    when (trend) {
        HistoryTrend.UP -> Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = stringResource(R.string.history_trend_up),
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier
        )

        HistoryTrend.DOWN -> Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = stringResource(R.string.history_trend_down),
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier
        )

        HistoryTrend.FLAT -> Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = stringResource(R.string.history_trend_flat),
            tint = MaterialTheme.colorScheme.outline,
            modifier = modifier
        )

        HistoryTrend.NONE -> Spacer(modifier = modifier)
    }
}

@Composable
private fun HistoryEmptyState(onStartPractice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_completed_attempts),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.history_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onStartPractice,
            modifier = Modifier
                .padding(top = 8.dp)
                .heightIn(min = 48.dp)
                .testTag("history_start_practice")
        ) {
            Text(stringResource(R.string.start_practice))
        }
    }
}

@StringRes
private fun HistoryEntry.titleRes(): Int = when (mode) {
    AttemptMode.MOCK -> R.string.history_title_mock
    AttemptMode.DRILL -> R.string.history_title_drill
    AttemptMode.PRACTICE -> R.string.history_title_practice
}

@StringRes
private fun AttemptMode.labelRes(): Int = when (this) {
    AttemptMode.MOCK -> R.string.mode_mock
    AttemptMode.DRILL -> R.string.mode_drill
    AttemptMode.PRACTICE -> R.string.mode_practice
}

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Preview
@Composable
private fun HistoryContentPreview() {
    AptiNaijaTheme {
        HistoryContent(
            state = HistoryUiState(
                loading = false,
                entries = listOf(
                    HistoryEntry("1", "UBA GMAP", AttemptMode.MOCK, "18 Nov", 78, HistoryTrend.UP),
                    HistoryEntry(
                        "2",
                        "Numerical Reasoning",
                        AttemptMode.DRILL,
                        "15 Nov",
                        54,
                        HistoryTrend.DOWN
                    ),
                    HistoryEntry("3", "SHL-style", AttemptMode.MOCK, "12 Nov", 71, HistoryTrend.UP),
                    HistoryEntry("4", "UBA GMAP", AttemptMode.MOCK, "9 Nov", 66, HistoryTrend.NONE)
                )
            ),
            onStartPractice = {},
            onAttempt = {}
        )
    }
}
