package com.enoch02.aptinaija.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enoch02.aptinaija.R

internal data class QuestionOption(
    val letter: String,
    val text: String,
    val value: Int,
    val selected: Boolean,
    val reveal: AnswerReveal? = null
)

@Composable
internal fun QuestionCard(
    meta: String,
    passage: String?,
    stem: String,
    options: List<QuestionOption>,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    testTagPrefix: String = "test",
    footer: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = meta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("${testTagPrefix}_meta")
        )

        if (passage != null) {
            PassageBlock(passage = passage, testTagPrefix = testTagPrefix)
        }

        Text(
            text = stem,
            style = stemStyle(stem),
            modifier = Modifier.testTag("${testTagPrefix}_stem")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_options"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { index, option ->
                AnswerOptionRow(
                    letter = option.letter,
                    text = option.text,
                    selected = option.selected,
                    reveal = option.reveal,
                    interactive = interactive,
                    onClick = { onSelectOption(option.value) },
                    modifier = Modifier.testTag("${testTagPrefix}_option_$index")
                )
            }
        }

        footer?.invoke()
    }
}

@Composable
private fun PassageBlock(passage: String, testTagPrefix: String) {
    var expanded by rememberSaveable(passage) { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_passage"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            val passageState = stringResource(
                if (expanded) R.string.passage_expanded else R.string.passage_collapsed
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .semantics { stateDescription = passageState },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reading_passage),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                Text(
                    text = passage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .testTag("${testTagPrefix}_passage_body")
                )
            }
        }
    }
}

@Composable
private fun stemStyle(stem: String): TextStyle {
    val base = if (stem.length <= 160) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.titleMedium
    }
    return base.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
}
