package com.enoch02.aptinaija.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enoch02.aptinaija.R

@Composable
internal fun SummaryCard(
    title: String,
    questionCount: Int,
    minutes: Int?,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Metric(value = questionCount, label = stringResource(R.string.questions_metric))
                if (minutes != null) {
                    Metric(value = minutes, label = stringResource(R.string.minutes_metric))
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun Metric(value: Int, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall.tabular()
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionCountSelector(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Text(
        text = stringResource(R.string.questions),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
    val counts = listOf(10, 20, 40)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        counts.forEachIndexed { index, count ->
            SegmentedButton(
                selected = selected == count,
                onClick = { onSelected(count) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = counts.size),
                label = { Text(count.toString()) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("setup_questions_$count")
            )
        }
    }
}

@Composable
internal fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
