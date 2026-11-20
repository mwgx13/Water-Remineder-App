package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HydroDarkColorScheme =
  darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = OnPrimaryDeep,
    primaryContainer = OceanicCyan,
    onPrimaryContainer = OnPrimaryContainerDeep,
    secondary = SecondaryAqua,
    onSecondary = OnSecondaryDeep,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerAqua,
    tertiary = TertiaryMist,
    onTertiary = OnTertiaryDeep,
    tertiaryContainer = TertiaryContainerMist,
    onTertiaryContainer = OnTertiaryContainerDeep,
    background = AbyssSurface,
    onBackground = OnSurfaceIce,
    surface = AbyssSurface,
    onSurface = OnSurfaceIce,
    surfaceVariant = AbyssSurfaceHighest,
    onSurfaceVariant = OnSurfaceVariantMist,
    surfaceContainerLowest = AbyssSurfaceLowest,
    surfaceContainerLow = AbyssSurfaceLow,
    surfaceContainer = AbyssSurfaceContainer,
    surfaceContainerHigh = AbyssSurfaceHigh,
    surfaceContainerHighest = AbyssSurfaceHighest,
    outline = OutlineMuted,
    outlineVariant = OutlineVariantSlate,
    error = ErrorCoral,
    onError = OnErrorDeep,
    errorContainer = ErrorContainerDeep,
    onErrorContainer = OnErrorContainerSoft,
  )

val HydroShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp),
  )

@Composable
fun HydroFluidityTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = HydroDarkColorScheme,
    typography = HydroTypography,
    shapes = HydroShapes,
    content = content,
  )
}
