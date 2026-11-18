package com.enoch02.aptinaija.feature.companies

import androidx.compose.runtime.Composable
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun CompanyDetailScreen(
    onBack: () -> Unit,
    onStartPractice: (String) -> Unit,
    onStartDrill: (String, String) -> Unit,
    onStartTemplate: (String) -> Unit
) {
    PlaceholderScreen(title = "Company", onBack = onBack)
}
