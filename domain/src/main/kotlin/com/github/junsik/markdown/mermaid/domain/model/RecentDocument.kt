package com.github.junsik.markdown.mermaid.domain.model

data class RecentDocument(
    val id: String,
    val uriString: String,
    val title: String,
    val lastOpenedAt: Long,
    val contentTitle: String? = null,
    val path: String? = null
)
