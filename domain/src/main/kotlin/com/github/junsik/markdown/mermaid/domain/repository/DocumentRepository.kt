package com.github.junsik.markdown.mermaid.domain.repository

import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getRecentDocuments(): Flow<List<RecentDocument>>
    suspend fun addRecentDocument(document: RecentDocument)
    suspend fun removeRecentDocument(id: String)
    
    suspend fun readDocument(uriString: String): String
    fun getDisplayName(uriString: String): String?
}
