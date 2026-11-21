package com.enoch02.aptinaija.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

@Composable
fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets
) {
    ScreenScaffold(
        title = title,
        onBack = onBack,
        contentWindowInsets = contentWindowInsets
    ) {
        MessageState(title)
    }
}
