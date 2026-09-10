package io.iamjosephmj.squishy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ApertureBg = Color(0xFF05070D)
val ApertureBg2 = Color(0xFF080B15)
val AperturePanel = Color(0xFF0B0F1C)
val AperturePanel2 = Color(0xFF0F1426)
val ApertureLine = Color(0xFF161C2E)
val ApertureLine2 = Color(0xFF232A44)
val Starlight = Color(0xFFD8C690)
val Aurora = Color(0xFF6AD8C2)
val NebulaPurple = Color(0xFFB48DFF)
val TextBright = Color(0xFFE9EAF0)
val TextSoft = Color(0xFFB3B9CB)
val TextMuted = Color(0xFF7D8497)
val TextMuted2 = Color(0xFF525A72)

private val ApertureScheme = darkColorScheme(
    primary = Starlight,
    onPrimary = Color(0xFF14171F),
    secondary = Aurora,
    onSecondary = Color(0xFF06231F),
    tertiary = NebulaPurple,
    background = ApertureBg,
    onBackground = TextBright,
    surface = AperturePanel,
    onSurface = TextBright,
    surfaceVariant = AperturePanel2,
    onSurfaceVariant = TextSoft,
    outline = ApertureLine2,
    outlineVariant = ApertureLine,
)

private val ApertureTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.9).sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = TextSoft,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = TextMuted,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.6.sp,
    ),
)

val SerifItalic = TextStyle(
    fontFamily = FontFamily.Serif,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
)

@Composable
fun SquishyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ApertureScheme,
        typography = ApertureTypography,
        content = content,
    )
}
