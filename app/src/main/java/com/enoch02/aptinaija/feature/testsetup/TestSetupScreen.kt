package com.enoch02.aptinaija.feature.testsetup

import androidx.compose.runtime.Composable
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun TestSetupScreen(
    onBack: () -> Unit,
    onStarted: (String) -> Unit
) {
    PlaceholderScreen(title = "Test setup", onBack = onBack)
}
