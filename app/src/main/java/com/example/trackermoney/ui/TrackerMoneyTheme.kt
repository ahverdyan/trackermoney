package com.example.trackermoney.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.trackermoney.R

@Composable
fun TrackerMoneyTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.dark_primary),
            onPrimary = colorResource(R.color.dark_on_primary),
            primaryContainer = colorResource(R.color.dark_primary_container),
            onPrimaryContainer = colorResource(R.color.dark_on_primary_container),
            secondary = colorResource(R.color.dark_secondary),
            background = colorResource(R.color.dark_background),
            onBackground = colorResource(R.color.dark_on_background),
            surface = colorResource(R.color.dark_surface),
            onSurface = colorResource(R.color.dark_on_surface),
            surfaceVariant = colorResource(R.color.dark_surface_variant),
            onSurfaceVariant = colorResource(R.color.dark_on_surface_variant),
            outline = colorResource(R.color.dark_outline),
            outlineVariant = colorResource(R.color.dark_outline_variant),
            error = colorResource(R.color.dark_error),
            errorContainer = colorResource(R.color.dark_error_container)
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.light_primary),
            onPrimary = colorResource(R.color.light_on_primary),
            primaryContainer = colorResource(R.color.light_primary_container),
            onPrimaryContainer = colorResource(R.color.light_on_primary_container),
            secondary = colorResource(R.color.light_secondary),
            background = colorResource(R.color.light_background),
            onBackground = colorResource(R.color.light_on_background),
            surface = colorResource(R.color.light_surface),
            onSurface = colorResource(R.color.light_on_surface),
            surfaceVariant = colorResource(R.color.light_surface_variant),
            onSurfaceVariant = colorResource(R.color.light_on_surface_variant),
            outline = colorResource(R.color.light_outline),
            outlineVariant = colorResource(R.color.light_outline_variant),
            error = colorResource(R.color.light_error),
            errorContainer = colorResource(R.color.light_error_container)
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
