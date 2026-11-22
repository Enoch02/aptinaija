package com.enoch02.aptinaija.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsContent(
        settings = settings,
        onThemeMode = viewModel::setThemeMode,
        onShuffleQuestions = viewModel::setShuffleQuestions,
        onShuffleOptions = viewModel::setShuffleOptions,
        onDefaultCount = viewModel::setDefaultQuestionCount
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    settings: AppSettings,
    onThemeMode: (AppThemeMode) -> Unit,
    onShuffleQuestions: (Boolean) -> Unit,
    onShuffleOptions: (Boolean) -> Unit,
    onDefaultCount: (Int) -> Unit
) {
    var showAdsDialog by rememberSaveable { mutableStateOf(false) }
    var showLibrariesSheet by rememberSaveable { mutableStateOf(false) }
    val libraries by produceLibraries(R.raw.aboutlibraries)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ThemeModeRow(selected = settings.themeMode, onSelected = onThemeMode)

        SwitchRow(
            title = stringResource(R.string.settings_shuffle_questions),
            subtitle = stringResource(R.string.settings_shuffle_questions_body),
            checked = settings.shuffleQuestions,
            onCheckedChange = onShuffleQuestions,
            testTag = "settings_shuffle_questions"
        )

        SwitchRow(
            title = stringResource(R.string.settings_shuffle_options),
            subtitle = null,
            checked = settings.shuffleOptions,
            onCheckedChange = onShuffleOptions,
            testTag = "settings_shuffle_options"
        )

        DefaultCountRow(selected = settings.defaultQuestionCount, onSelected = onDefaultCount)

        TextRow(
            title = stringResource(R.string.settings_ads_consent),
            subtitle = stringResource(R.string.settings_ads_manage),
            testTag = "settings_ads_consent",
            onClick = { showAdsDialog = true }
        )

        TextRow(
            title = stringResource(R.string.settings_open_source_licenses),
            subtitle = stringResource(R.string.settings_open_source_licenses_subtitle),
            testTag = "settings_open_source_licenses",
            onClick = { showLibrariesSheet = true }
        )
    }

    if (showAdsDialog) {
        AlertDialog(
            onDismissRequest = { showAdsDialog = false },
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(R.string.settings_ads_dialog_title)) },
            text = { Text(stringResource(R.string.settings_ads_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = { showAdsDialog = false },
                    modifier = Modifier.testTag("settings_ads_ok")
                ) {
                    Text(stringResource(R.string.settings_ok))
                }
            }
        )
    }

    if (showLibrariesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLibrariesSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_open_source_licenses_sheet")
            )
        }
    }
}

@Composable
private fun ThemeModeRow(selected: AppThemeMode, onSelected: (AppThemeMode) -> Unit) {
    Column(modifier = Modifier.testTag("settings_theme")) {
        Text(
            text = stringResource(R.string.settings_dark_mode),
            style = MaterialTheme.typography.titleMedium
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, AppThemeMode.entries.size),
                    label = {
                        Text(
                            stringResource(
                                when (mode) {
                                    AppThemeMode.LIGHT -> R.string.settings_theme_light
                                    AppThemeMode.DARK -> R.string.settings_theme_dark
                                    AppThemeMode.AUTO -> R.string.settings_theme_auto
                                }
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_theme_${mode.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = if (subtitle != null) {
            {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            null
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag("${testTag}_row")
    )
}

@Composable
private fun DefaultCountRow(selected: Int, onSelected: (Int) -> Unit) {
    Column(modifier = Modifier.testTag("settings_default_count")) {
        Text(
            text = stringResource(R.string.settings_default_count),
            style = MaterialTheme.typography.titleMedium
        )
        val counts = listOf(10, 20, 40)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            counts.forEachIndexed { index, count ->
                SegmentedButton(
                    selected = selected == count,
                    onClick = { onSelected(count) },
                    shape = SegmentedButtonDefaults.itemShape(index, counts.size),
                    label = { Text(count.toString()) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_count_$count")
                )
            }
        }
    }
}

@Composable
private fun TextRow(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp)
            .testTag(testTag)
    )
}

@Preview
@Composable
private fun SettingsContentPreview() {
    AptiNaijaTheme {
        SettingsContent(
            settings = AppSettings(themeMode = AppThemeMode.AUTO),
            onThemeMode = {},
            onShuffleQuestions = {},
            onShuffleOptions = {},
            onDefaultCount = {}
        )
    }
}
