package com.aram.benchpress13week.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF9A3412),
    onPrimary = Color(0xFFFFF8F2),
    primaryContainer = Color(0xFFFFD9C5),
    onPrimaryContainer = Color(0xFF3E1300),
    secondary = Color(0xFF355E5A),
    onSecondary = Color(0xFFF2FFFC),
    secondaryContainer = Color(0xFFBCECE4),
    onSecondaryContainer = Color(0xFF0B1F1C),
    tertiary = Color(0xFF2D3C55),
    onTertiary = Color(0xFFF6F8FF),
    background = Color(0xFFF4EFE8),
    onBackground = Color(0xFF1C1A18),
    surface = Color(0xFFFFFBF7),
    onSurface = Color(0xFF1C1A18),
    surfaceVariant = Color(0xFFE8DED4),
    onSurfaceVariant = Color(0xFF4D463F),
    outline = Color(0xFF81776F),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB68C),
    onPrimary = Color(0xFF552000),
    primaryContainer = Color(0xFF7A2B00),
    onPrimaryContainer = Color(0xFFFFD9C5),
    secondary = Color(0xFFA0D0C9),
    onSecondary = Color(0xFF033734),
    secondaryContainer = Color(0xFF1E4A46),
    onSecondaryContainer = Color(0xFFBCECE4),
    tertiary = Color(0xFFB9C8E8),
    onTertiary = Color(0xFF16263D),
    background = Color(0xFF131313),
    onBackground = Color(0xFFE9E2DB),
    surface = Color(0xFF1A1816),
    onSurface = Color(0xFFE9E2DB),
    surfaceVariant = Color(0xFF4D463F),
    onSurfaceVariant = Color(0xFFD0C4BA),
    outline = Color(0xFF9B8F86),
    error = Color(0xFFF2B8B5),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
)

@Composable
fun BenchPressTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
