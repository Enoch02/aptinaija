package com.enoch02.aptinaija.feature.companies

import androidx.compose.runtime.Composable
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun CompaniesScreen(
    onBack: () -> Unit,
    onCompany: (String) -> Unit
) {
    PlaceholderScreen(title = "Companies", onBack = onBack)
}
