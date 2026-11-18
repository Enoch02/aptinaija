package com.enoch02.aptinaija.feature.history

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun HistoryScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.history),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    )
}
