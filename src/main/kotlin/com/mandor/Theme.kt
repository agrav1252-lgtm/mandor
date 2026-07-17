package com.mandor

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Modern Color Palette
val PrimaryColor = Color(0xFF6366F1) // Vibrant Indigo
val PrimaryVariantColor = Color(0xFF4F46E5)
val SecondaryColor = Color(0xFFF43F5E) // Pastel Rose/Coral
val BackgroundColor = Color(0xFF0B0F19) // Very dark blue/black
val SurfaceColor = Color(0xFF1E293B) // Dark Slate Blue
val AccentCyan = Color(0xFF06B6D4) // Bright Cyan
val AccentYellow = Color(0xFFFBBF24) // Soft Amber

val AppColors: Colors = darkColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryVariantColor,
    secondary = SecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFE2E8F0)
)

val AppTypography = Typography(
    h4 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.25.sp,
        color = Color.White
    ),
    h5 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        color = Color.White
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
        color = Color(0xFF94A3B8)
    ),
    button = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 1.25.sp,
        color = Color.White
    )
)

@Composable
fun MandorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppColors,
        typography = AppTypography,
        content = content
    )
}
