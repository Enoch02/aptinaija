package com.enoch02.aptinaija.feature.home

import androidx.compose.runtime.Composable
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun HomeScreen(
    onBrowseCompanies: () -> Unit,
    onStartTemplate: (String) -> Unit,
    onHistory: () -> Unit,
    onWrongAnswers: () -> Unit,
    onBookmarks: () -> Unit,
    onSettings: () -> Unit,
    onOpenLastResult: (String) -> Unit
) {
    PlaceholderScreen(title = "AptiNaija")
}
