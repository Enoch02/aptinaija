package com.enoch02.aptinaija.feature.companies

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.MessageState

@Composable
fun CompaniesScreen(
    onCompany: (String) -> Unit
) {
    MessageState(stringResource(R.string.companies))
}
