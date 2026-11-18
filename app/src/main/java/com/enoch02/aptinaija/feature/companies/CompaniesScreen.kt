package com.enoch02.aptinaija.feature.companies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.designsystem.AptiNaijaTheme
import com.enoch02.aptinaija.core.ui.components.LoadingState

@Composable
fun CompaniesScreen(
    onCompany: (String) -> Unit,
    viewModel: CompaniesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CompaniesContent(
        state = state,
        onCompany = onCompany,
        onSectionSelected = viewModel::toggleSection,
        onProviderToggled = viewModel::toggleProvider,
        onClearProviders = viewModel::clearProviders,
        onClearFilters = viewModel::clearFilters
    )
}

@Composable
internal fun CompaniesContent(
    state: CompaniesUiState,
    onCompany: (String) -> Unit,
    onSectionSelected: (String) -> Unit,
    onProviderToggled: (String) -> Unit,
    onClearProviders: () -> Unit,
    onClearFilters: () -> Unit
) {
    if (state.loading) {
        LoadingState(Modifier.testTag("companies_loading"))
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companies_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (state.sections.isNotEmpty() || state.providers.isNotEmpty()) {
            item {
                FilterRow(
                    state = state,
                    onSectionSelected = onSectionSelected,
                    onProviderToggled = onProviderToggled,
                    onClearProviders = onClearProviders,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        if (state.companies.isEmpty()) {
            item {
                EmptyCompanies(
                    hasActiveFilters = state.hasActiveFilters,
                    onClearFilters = onClearFilters
                )
            }
        } else {
            items(state.companies, key = { it.companyId }) { company ->
                CompanyRow(
                    item = company,
                    onClick = { onCompany(company.companyId) }
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    state: CompaniesUiState,
    onSectionSelected: (String) -> Unit,
    onProviderToggled: (String) -> Unit,
    onClearProviders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.sections.forEach { section ->
                val selected = section in state.selectedSections
                FilterChip(
                    selected = selected,
                    onClick = { onSectionSelected(section) },
                    label = { Text(section) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.testTag("companies_section_$section")
                )
            }
        }

        if (state.providers.isNotEmpty()) {
            ProviderFilter(
                selectedProviders = state.selectedProviders,
                providers = state.providers,
                onProviderToggled = onProviderToggled,
                onClearProviders = onClearProviders,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ProviderFilter(
    selectedProviders: Set<String>,
    providers: List<String>,
    onProviderToggled: (String) -> Unit,
    onClearProviders: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when {
        selectedProviders.isEmpty() -> stringResource(R.string.provider)
        selectedProviders.size == 1 -> selectedProviders.first()
        else -> pluralStringResource(
            R.plurals.providers_selected,
            selectedProviders.size,
            selectedProviders.size
        )
    }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedProviders.isNotEmpty(),
            onClick = { expanded = true },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.testTag("companies_provider_chip")
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.all_providers)) },
                leadingIcon = {
                    if (selectedProviders.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                onClick = onClearProviders,
                modifier = Modifier.semantics {
                    selected = selectedProviders.isEmpty()
                }
            )
            providers.forEach { provider ->
                val selected = provider in selectedProviders
                DropdownMenuItem(
                    text = { Text(provider) },
                    leadingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    onClick = { onProviderToggled(provider) },
                    modifier = Modifier.semantics { this.selected = selected }
                )
            }
        }
    }
}

@Composable
private fun CompanyRow(
    item: CompanyListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("company_row_${item.companyId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.initials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.company,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = companySummary(item),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun companySummary(item: CompanyListItem): String {
    val provider = if (item.providers.size == 1) {
        item.providers.first()
    } else {
        stringResource(R.string.multiple_providers)
    }

    return if (item.templateCount == 1) {
        pluralStringResource(
            R.plurals.company_summary_single,
            item.questionCount,
            provider,
            item.questionCount,
            item.totalMinutes
        )
    } else {
        stringResource(
            R.string.company_summary_multiple,
            provider,
            pluralStringResource(
                R.plurals.papers,
                item.templateCount,
                item.templateCount
            )
        )
    }
}

@Composable
private fun EmptyCompanies(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                if (hasActiveFilters) {
                    R.string.no_companies_match
                } else {
                    R.string.no_companies_available
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasActiveFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.testTag("companies_clear_filters")
            ) {
                Text(stringResource(R.string.clear_filters))
            }
        }
    }
}

@Preview
@Composable
private fun CompaniesContentPreview() {
    AptiNaijaTheme {
        CompaniesContent(
            state = CompaniesUiState(
                loading = false,
                companies = listOf(
                    CompanyListItem(
                        companyId = "UBA",
                        company = "UBA",
                        initials = "UB",
                        templateCount = 1,
                        questionCount = 64,
                        totalMinutes = 75,
                        sections = setOf(
                            "Numerical Reasoning",
                            "Verbal Reasoning",
                            "Logical Reasoning"
                        ),
                        providers = setOf("In-house / MS Forms")
                    ),
                    CompanyListItem(
                        companyId = "DRAGNET",
                        company = "Cross-industry (Dragnet clients)",
                        initials = "CI",
                        templateCount = 2,
                        questionCount = 45,
                        totalMinutes = 45,
                        sections = setOf("Logical Reasoning", "Verbal Reasoning"),
                        providers = setOf("Dragnet")
                    )
                ),
                sections = listOf(
                    "Logical Reasoning",
                    "Numerical Reasoning",
                    "Verbal Reasoning"
                ),
                providers = listOf("Dragnet", "In-house / MS Forms")
            ),
            onCompany = {},
            onSectionSelected = {},
            onProviderToggled = {},
            onClearProviders = {},
            onClearFilters = {}
        )
    }
}
