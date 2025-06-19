package com.codewithpk.palmpay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.codewithpk.palmpay.R // Make sure you have resources for fonts if using custom ones

// --- Custom Colors ---
// PhonePe inspired palette: teal, white, soft gray, green, navy blue
val TealPrimary = Color(0xFF007B8A) // A primary teal, similar to PhonePe's brand color
val TealLight = Color(0xFF26C6DA)
val TealDark = Color(0xFF004D40)
val White = Color(0xFFFFFFFF)
val GraySoft = Color(0xFFE0E0E0) // Light gray for backgrounds, cards
val GrayMedium = Color(0xFFB0B0B0) // For secondary text, borders
val GrayDark = Color(0xFF424242) // For dark text
val GreenSuccess = Color(0xFF4CAF50) // Bright green for success states
val RedFailure = Color(0xFFF44336) // Red for failure states
val NavyBlue = Color(0xFF1A237E) // Dark blue for accents or dark mode elements

// --- Color Schemes ---
// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = White,
    primaryContainer = TealLight,
    onPrimaryContainer = Color.Black,
    secondary = TealDark,
    onSecondary = White,
    secondaryContainer = GraySoft,
    onSecondaryContainer = Color.Black,
    tertiary = NavyBlue,
    onTertiary = White,
    background = GraySoft, // Soft gray background for overall app
    onBackground = GrayDark,
    surface = White, // White for cards, dialogs
    onSurface = GrayDark,
    error = RedFailure,
    onError = White,
    // Custom colors for specific use cases
    outline = GrayMedium, // For borders
    surfaceVariant = GraySoft, // Slightly darker than surface for variations
    onSurfaceVariant = GrayDark,
)

// Dark Color Scheme (simplified for hackathon, can be more detailed)
private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Color.Black,
    primaryContainer = TealDark,
    onPrimaryContainer = White,
    secondary = NavyBlue,
    onSecondary = White,
    secondaryContainer = GrayDark,
    onSecondaryContainer = White,
    tertiary = TealPrimary,
    onTertiary = White,
    background = Color(0xFF121212), // Dark background
    onBackground = White,
    surface = Color(0xFF1E1E1E), // Darker surface for cards
    onSurface = White,
    error = RedFailure,
    onError = White,
    outline = GrayMedium,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = White,
)

// --- Typography ---
// For Google Fonts like Roboto, you typically need to add them as font resources.
// If you don't have them as resources (e.g., in res/font/roboto_regular.ttf),
// stick to default system fonts or use a web font loader for actual web apps.
// For Android, we define FontFamilies.

// Example if you add Roboto fonts to res/font:
/*
val Roboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold)
)
*/
// For simplicity in a hackathon without adding font files, we'll simulate.
// If you want to use a custom font, add a `font` folder under `res` and put `.ttf` files there.
// Then define your FontFamily like the commented out `Roboto` above.
// For this example, we'll rely on default Compose font definitions, which usually map to Roboto on Android.

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold, // Often bolder for titles
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun PalmPayTheme(

darkTheme: Boolean = isSystemInDarkTheme(),
// Dynamic color is available on Android 12+
dynamicColor: Boolean = true, // Set to true to use dynamic color if available
content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}