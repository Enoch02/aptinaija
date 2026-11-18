package com.enoch02.aptinaija.feature.test

import androidx.compose.runtime.Composable
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun TestScreen(
    onBack: () -> Unit,
    onFinished: (String) -> Unit
) {
    PlaceholderScreen(title = "Test", onBack = onBack)
}
