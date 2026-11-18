package com.enoch02.aptinaija.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.MessageState

@Composable
fun HistoryScreen() {
    MessageState(stringResource(R.string.history))
}
