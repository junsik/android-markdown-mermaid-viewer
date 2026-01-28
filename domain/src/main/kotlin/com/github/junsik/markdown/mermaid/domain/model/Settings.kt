package com.github.junsik.markdown.mermaid.domain.model

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val mermaidAutoDirection: Boolean = true
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
