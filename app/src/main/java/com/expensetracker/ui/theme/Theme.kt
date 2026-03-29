package com.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppColors {
    val Emerald       = Color(0xFF10B981)
    val EmeraldLight  = Color(0xFF34D399)
    val EmeraldDark   = Color(0xFF059669)
    val Coral         = Color(0xFFF97316)
    val CoralLight    = Color(0xFFFB923C)
    val Slate900      = Color(0xFF0F172A)
    val Slate800      = Color(0xFF1E293B)
    val Slate700      = Color(0xFF334155)
    val Slate600      = Color(0xFF475569)
    val Slate400      = Color(0xFF94A3B8)
    val Slate200      = Color(0xFFE2E8F0)
    val Slate100      = Color(0xFFF1F5F9)
    val Slate50       = Color(0xFFF8FAFC)
    val Success       = Color(0xFF22C55E)
    val Warning       = Color(0xFFF59E0B)
    val Error         = Color(0xFFEF4444)
    val Info          = Color(0xFF3B82F6)

    val Chart = listOf(
        Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B),
        Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFFA855F7),
        Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFFF97316),
        Color(0xFF84CC16), Color(0xFF06B6D4), Color(0xFF8B5CF6),
        Color(0xFF64748B)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary            = AppColors.Emerald,
    onPrimary          = Color.White,
    primaryContainer   = AppColors.EmeraldDark,
    onPrimaryContainer = AppColors.EmeraldLight,
    secondary          = AppColors.Coral,
    onSecondary        = Color.White,
    background         = AppColors.Slate900,
    onBackground       = AppColors.Slate100,
    surface            = AppColors.Slate800,
    onSurface          = AppColors.Slate100,
    surfaceVariant     = AppColors.Slate700,
    onSurfaceVariant   = AppColors.Slate400,
    error              = AppColors.Error,
    outline            = AppColors.Slate600,
)

private val LightColorScheme = lightColorScheme(
    primary            = AppColors.EmeraldDark,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD1FAE5),
    onPrimaryContainer = AppColors.EmeraldDark,
    secondary          = AppColors.Coral,
    onSecondary        = Color.White,
    background         = AppColors.Slate50,
    onBackground       = AppColors.Slate900,
    surface            = Color.White,
    onSurface          = AppColors.Slate900,
    surfaceVariant     = AppColors.Slate100,
    onSurfaceVariant   = AppColors.Slate600,
    error              = AppColors.Error,
    outline            = AppColors.Slate200,
)

val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,  fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 45.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}