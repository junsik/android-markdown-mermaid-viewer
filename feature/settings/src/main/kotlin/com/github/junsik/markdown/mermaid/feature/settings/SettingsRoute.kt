package com.github.junsik.markdown.mermaid.feature.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsRoute(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    SettingsScreen(
        isDarkTheme = isDarkTheme,
        onThemeChanged = onThemeChanged,
        onBackClick = onBackClick
    )
}
