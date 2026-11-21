package com.enoch02.aptinaija.feature.testsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.TemplateSection
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate

@Composable
fun TestSetupScreen(
    onBack: () -> Unit,
    onStarted: (String) -> Unit,
    viewModel: TestSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.attemptId) {
        state.attemptId?.let(onStarted)
    }

    TestSetupContent(
        state = state,
        onBack = onBack,
        onModeSelected = viewModel::selectMode,
        onSectionSelected = viewModel::selectSection,
        onTemplateSelected = viewModel::selectTemplate,
        onQuestionCountSelected = viewModel::selectQuestionCount,
        onBegin = viewModel::start
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TestSetupContent(
    state: TestSetupUiState,
    onBack: () -> Unit,
    onModeSelected: (AttemptMode) -> Unit,
    onSectionSelected: (String) -> Unit,
    onTemplateSelected: (String) -> Unit,
    onQuestionCountSelected: (Int) -> Unit,
    onBegin: () -> Unit
) {
    ScreenScaffold(
        title = state.companyName.ifBlank { stringResource(R.string.test_setup) },
        onBack = onBack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        if (state.loading) {
            LoadingState(Modifier.testTag("setup_loading"))
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
                ModeSelector(
                    selected = state.mode,
                    onSelected = onModeSelected
                )

                when (state.mode) {
                    AttemptMode.PRACTICE -> PracticeSetup(
                        state = state,
                        onQuestionCountSelected = onQuestionCountSelected
                    )

                    AttemptMode.DRILL -> DrillSetup(
                        state = state,
                        onSectionSelected = onSectionSelected,
                        onQuestionCountSelected = onQuestionCountSelected
                    )

                    AttemptMode.MOCK -> MockSetup(
                        state = state,
                        onTemplateSelected = onTemplateSelected
                    )

                    AttemptMode.RANDOM_QUIZ -> Unit
                }

                state.error?.let { message ->
                    ErrorBanner(message)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onBegin,
                    enabled = state.canBegin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("setup_begin")
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
                        Text(
                            when (state.mode) {
                                AttemptMode.PRACTICE -> stringResource(R.string.begin_practice)
                                AttemptMode.DRILL -> stringResource(R.string.begin_drill)
                                AttemptMode.MOCK -> stringResource(R.string.begin)
                                AttemptMode.RANDOM_QUIZ -> stringResource(R.string.begin)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSelector(
    selected: AttemptMode,
    onSelected: (AttemptMode) -> Unit
) {
    val modes = AttemptMode.entries.filter { it != AttemptMode.RANDOM_QUIZ }
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                label = {
                    Text(
                        when (mode) {
                            AttemptMode.PRACTICE -> stringResource(R.string.mode_practice)
                            AttemptMode.DRILL -> stringResource(R.string.mode_drill)
                            AttemptMode.MOCK -> stringResource(R.string.mock)
                            AttemptMode.RANDOM_QUIZ -> stringResource(R.string.mode_random_quiz)
                        }
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("setup_mode_${mode.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun PracticeSetup(
    state: TestSetupUiState,
    onQuestionCountSelected: (Int) -> Unit
) {
    if (state.sections.isEmpty()) {
        EmptyCard(stringResource(R.string.no_sections_available))
        return
    }
    SummaryCard(
        title = stringResource(R.string.practice_summary, state.companyName),
        questionCount = state.questionCount,
        minutes = null,
        description = sectionsDescription(state.sections.map { it.section })
    )
    InfoCard(
        title = stringResource(R.string.practice_mode),
        body = stringResource(R.string.practice_mode_body)
    )
    QuestionCountSelector(
        selected = state.questionCount,
        onSelected = onQuestionCountSelected
    )
}

@Composable
private fun DrillSetup(
    state: TestSetupUiState,
    onSectionSelected: (String) -> Unit,
    onQuestionCountSelected: (Int) -> Unit
) {
    Text(
        text = stringResource(R.string.choose_a_section),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
    if (state.sections.isEmpty()) {
        EmptyCard(stringResource(R.string.no_sections_available))
        return
    }
    state.sections.forEach { section ->
        SelectableRow(
            label = section.section,
            selected = state.selectedSection == section.section,
            onClick = { onSectionSelected(section.section) },
            testTag = "setup_section_${section.section}"
        )
    }
    QuestionCountSelector(
        selected = state.questionCount,
        onSelected = onQuestionCountSelected
    )
}

@Composable
private fun MockSetup(
    state: TestSetupUiState,
    onTemplateSelected: (String) -> Unit
) {
    if (state.templates.size > 1) {
        Text(
            text = stringResource(R.string.choose_a_mock),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        state.templates.forEach { template ->
            SelectableRow(
                label = template.title,
                supporting = template.provider,
                selected = state.selectedTemplateId == template.id,
                onClick = { onTemplateSelected(template.id) },
                testTag = "setup_template_${template.id}"
            )
        }
    }

    val template = state.selectedTemplate
    if (template == null) {
        EmptyCard(stringResource(R.string.no_mock_tests_available))
        return
    }
    SummaryCard(
        title = stringResource(
            R.string.mock_summary,
            template.company.ifBlank { state.companyName }
        ),
        questionCount = state.mockQuestionCount,
        minutes = state.mockMinutes,
        description = sectionsDescription(template.sections.map { it.section })
    )
    InfoCard(
        title = stringResource(R.string.strict_timing),
        body = stringResource(R.string.strict_timing_body)
    )
    InfoCard(
        title = stringResource(R.string.what_to_expect),
        body = stringResource(R.string.what_to_expect_body)
    )
}

@Composable
private fun SelectableRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    supporting: String? = null
) {
    val shape = MaterialTheme.shapes.medium
    val container = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .testTag(testTag),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionCircle(selected = selected)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = content
                )
                if (supporting != null) {
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = content.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("setup_error"),
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

@Composable
private fun sectionsDescription(sections: List<String>): String {
    if (sections.isEmpty()) return stringResource(R.string.shuffled_summary)
    return stringResource(
        R.string.sections_shuffled,
        sections.joinToString(", ")
    )
}

@Preview
@Composable
private fun TestSetupContentPreview() {
    AptiNaijaTheme {
        TestSetupContent(
            state = TestSetupUiState(
                loading = false,
                mode = AttemptMode.MOCK,
                companyId = "SEP",
                companyName = "Seplat Energy",
                sections = listOf(
                    SectionSummary("Numerical Reasoning", 20),
                    SectionSummary("Verbal Reasoning", 20),
                    SectionSummary("Logical Reasoning", 10),
                    SectionSummary("Engineering/Technical", 50)
                ),
                templates = listOf(
                    TestTemplate(
                        id = "SEPLAT_TECH_TRAINEE",
                        title = "Seplat Technical Trainee Mock",
                        companyId = "SEP",
                        company = "Seplat Energy",
                        provider = "HCP (Human Capital Partners)",
                        sourceUrl = null,
                        defaults = TestDefaults(),
                        sections = listOf(
                            TemplateSection("Numerical Reasoning", 20, 20),
                            TemplateSection("Verbal Reasoning", 20, 18),
                            TemplateSection("Logical Reasoning", 10, 12),
                            TemplateSection("Engineering/Technical", 50, 30)
                        )
                    )
                ),
                selectedTemplateId = "SEPLAT_TECH_TRAINEE",
                selectedSection = "Numerical Reasoning",
                questionCount = 20
            ),
            onBack = {},
            onModeSelected = {},
            onSectionSelected = {},
            onTemplateSelected = {},
            onQuestionCountSelected = {},
            onBegin = {}
        )
    }
}
