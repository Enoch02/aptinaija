package com.enoch02.aptinaija.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enoch02.aptinaija.R

private const val MARK_YOUR_ANSWER = "your_answer_mark"
private const val MARK_CORRECT_ANSWER = "correct_answer_mark"

@Composable
internal fun AnswerDeltaRow(
    selectedLetter: String?,
    correctLetter: String,
    answeredCorrectly: Boolean,
    explanation: String,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    lineTestTag: String? = null
) {
    val colors = MaterialTheme.colorScheme
    val lineColor = if (answeredCorrectly) colors.primary else colors.error

    val delta = buildAnnotatedString {
        if (selectedLetter == null) {
            append(stringResource(R.string.review_not_answered))
        } else {
            append(stringResource(R.string.delta_your_answer, selectedLetter))
            appendInlineContent(MARK_YOUR_ANSWER, if (answeredCorrectly) "✓" else "✗")
        }
        if (!answeredCorrectly) {
            append(" · ")
            append(stringResource(R.string.delta_correct, correctLetter))
            appendInlineContent(MARK_CORRECT_ANSWER, "✓")
        }
    }

    val placeholder = Placeholder(
        width = 16.sp,
        height = 16.sp,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
    )
    val inlineContent = mapOf(
        MARK_YOUR_ANSWER to InlineTextContent(placeholder) {
            Icon(
                imageVector = if (answeredCorrectly) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = lineColor,
                modifier = Modifier.size(16.dp)
            )
        },
        MARK_CORRECT_ANSWER to InlineTextContent(placeholder) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = delta,
                style = MaterialTheme.typography.titleSmall,
                color = lineColor,
                inlineContent = inlineContent,
                modifier = if (lineTestTag != null) Modifier.testTag(lineTestTag) else Modifier
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
