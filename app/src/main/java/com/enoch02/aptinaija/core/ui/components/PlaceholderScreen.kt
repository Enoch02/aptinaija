package com.enoch02.aptinaija.core.ui.components

import androidx.compose.runtime.Composable

@Composable
fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null
) {
    ScreenScaffold(title = title, onBack = onBack) {
        MessageState(title)
    }
}
