package com.enoch02.aptinaija.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp

internal enum class AnswerReveal { CORRECT, WRONG }

@Composable
internal fun AnswerOptionRow(
    letter: String,
    text: String,
    selected: Boolean,
    reveal: AnswerReveal?,
    interactive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium

    val container: Color
    val contentColor: Color
    when (reveal) {
        AnswerReveal.CORRECT -> {
            container = colors.primaryContainer
            contentColor = colors.onPrimaryContainer
        }

        AnswerReveal.WRONG -> {
            container = colors.errorContainer
            contentColor = colors.onErrorContainer
        }

        null -> when {
            selected -> {
                container = colors.primaryContainer
                contentColor = colors.onPrimaryContainer
            }

            else -> {
                container = colors.surfaceContainerLow
                contentColor = colors.onSurface
            }
        }
    }

    val border = if (reveal == null && selected) {
        BorderStroke(1.dp, colors.outline)
    } else {
        null
    }

    val letterBorder = when (reveal) {
        AnswerReveal.CORRECT -> colors.primary
        AnswerReveal.WRONG -> colors.error
        null -> if (selected) colors.primary else colors.outline
    }
    val letterFilled = reveal == null && selected

    // Revealed rows stay readable, so they keep semantics without a click action.
    val interaction = if (interactive) {
        Modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick
        )
    } else {
        Modifier.semantics(mergeDescendants = true) {
            role = Role.RadioButton
            this.selected = selected
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(interaction)
            .background(container, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    if (letterFilled) {
                        Modifier.background(colors.primary)
                    } else {
                        Modifier.border(1.dp, letterBorder, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.labelLarge,
                color = if (letterFilled) colors.onPrimary else contentColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        if (reveal != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (reveal == AnswerReveal.CORRECT) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
