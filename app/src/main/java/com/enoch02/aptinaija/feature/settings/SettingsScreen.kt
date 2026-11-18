package com.enoch02.aptinaija.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.MessageState

@Composable
fun SettingsScreen() {
    MessageState(stringResource(R.string.settings))
}
