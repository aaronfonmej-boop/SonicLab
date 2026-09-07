package com.soniclab3d.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SonicPalette {
    val Void = Color(0xFF02050B)
    val DeepSpace = Color(0xFF050B14)
    val Panel = Color(0xFF081320)
    val PanelRaised = Color(0xFF0D1B2B)
    val PanelBright = Color(0xFF13273B)
    val Cyan = Color(0xFF43E6FF)
    val CyanSoft = Color(0xFF9BF2FF)
    val Violet = Color(0xFF9C8CFF)
    val Magenta = Color(0xFFFF4D82)
    val Green = Color(0xFF4CF2B1)
    val Amber = Color(0xFFFFD166)
    val Ice = Color(0xFFEAF7FF)
    val Muted = Color(0xFF8EA8BB)
    val Grid = Color(0xFF1B3850)
}

private val SonicColors = darkColorScheme(
    primary = SonicPalette.Cyan,
    onPrimary = Color(0xFF001F27),
    primaryContainer = Color(0xFF063847),
    onPrimaryContainer = SonicPalette.CyanSoft,
    secondary = SonicPalette.Violet,
    onSecondary = Color(0xFF17103F),
    secondaryContainer = Color(0xFF2B2459),
    onSecondaryContainer = Color(0xFFE5DFFF),
    tertiary = SonicPalette.Magenta,
    onTertiary = Color(0xFF400015),
    tertiaryContainer = Color(0xFF5A112C),
    onTertiaryContainer = Color(0xFFFFD9E3),
    background = SonicPalette.Void,
    surface = SonicPalette.DeepSpace,
    surfaceVariant = SonicPalette.PanelRaised,
    surfaceContainer = SonicPalette.Panel,
    surfaceContainerHigh = SonicPalette.PanelRaised,
    surfaceContainerHighest = SonicPalette.PanelBright,
    onBackground = SonicPalette.Ice,
    onSurface = SonicPalette.Ice,
    onSurfaceVariant = SonicPalette.Muted,
    outline = Color(0xFF315069),
    outlineVariant = Color(0xFF193249),
    error = Color(0xFFFF6B83)
)

private val SonicTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 27.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 23.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.7.sp
    )
)

private val SonicShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
)

@Composable
fun SonicLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SonicColors,
        typography = SonicTypography,
        shapes = SonicShapes,
        content = content
    )
}
