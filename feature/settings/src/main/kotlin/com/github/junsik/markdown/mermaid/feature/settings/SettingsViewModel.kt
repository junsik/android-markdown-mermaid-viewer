package com.github.junsik.markdown.mermaid.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _isTableFormat = MutableStateFlow(prefs.getBoolean("table_format", false))
    val isTableFormat = _isTableFormat.asStateFlow()

    fun onThemeChanged(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    fun onTableFormatChanged(isTable: Boolean) {
        _isTableFormat.value = isTable
        prefs.edit().putBoolean("table_format", isTable).apply()
    }
}
