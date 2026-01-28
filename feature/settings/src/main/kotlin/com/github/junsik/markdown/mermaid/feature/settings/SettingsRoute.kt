package com.github.junsik.markdown.mermaid.feature.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsRoute(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    isTableFormat: Boolean,
    onTableFormatChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    SettingsScreen(
        isDarkTheme = isDarkTheme,
        onThemeChanged = onThemeChanged,
        isTableFormat = isTableFormat,
        onTableFormatChanged = onTableFormatChanged,
        onBackClick = onBackClick
    )
}
