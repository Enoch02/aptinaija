package com.enoch02.aptinaija.feature.companies

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R
import com.enoch02.aptinaija.core.ui.components.PlaceholderScreen

@Composable
fun CompaniesScreen(
    onCompany: (String) -> Unit
) {
    PlaceholderScreen(
        title = stringResource(R.string.companies),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    )
}
