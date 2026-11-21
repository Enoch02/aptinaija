package com.enoch02.aptinaija.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.enoch02.aptinaija.R

val provider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// IBM Plex Sans — single family for all type roles (design decision).
// All four weights must be registered explicitly: M3 baseline styles request
// Medium (500) for labels/titles, and Bold (700) for emphasized styles.
// With only 400 registered, Compose would silently render every style as Regular.
val plexSans = FontFamily(
    Font(
        googleFont = GoogleFont("IBM Plex Sans"),
        fontProvider = provider,
        weight = FontWeight.Normal,
    ),
    Font(
        googleFont = GoogleFont("IBM Plex Sans"),
        fontProvider = provider,
        weight = FontWeight.Medium,
    ),
    Font(
        googleFont = GoogleFont("IBM Plex Sans"),
        fontProvider = provider,
        weight = FontWeight.SemiBold,
    ),
    Font(
        googleFont = GoogleFont("IBM Plex Sans"),
        fontProvider = provider,
        weight = FontWeight.Bold,
    ),
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = plexSans),
    displayMedium = baseline.displayMedium.copy(fontFamily = plexSans),
    displaySmall = baseline.displaySmall.copy(fontFamily = plexSans),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = plexSans),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = plexSans),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = plexSans),
    titleLarge = baseline.titleLarge.copy(fontFamily = plexSans),
    titleMedium = baseline.titleMedium.copy(fontFamily = plexSans),
    titleSmall = baseline.titleSmall.copy(fontFamily = plexSans),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = plexSans),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = plexSans),
    bodySmall = baseline.bodySmall.copy(fontFamily = plexSans),
    labelLarge = baseline.labelLarge.copy(fontFamily = plexSans),
    labelMedium = baseline.labelMedium.copy(fontFamily = plexSans),
    // NFR-08 deviation: M3 labelSmall is 11sp; our spec requires min 12sp.
    labelSmall = baseline.labelSmall.copy(fontFamily = plexSans, fontSize = 12.sp),
)
