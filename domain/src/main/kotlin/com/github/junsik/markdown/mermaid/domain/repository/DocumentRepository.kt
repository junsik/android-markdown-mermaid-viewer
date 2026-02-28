package com.github.junsik.markdown.mermaid.domain.repository

import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getRecentDocuments(): Flow<List<RecentDocument>>
    suspend fun addRecentDocument(document: RecentDocument)
    suspend fun removeRecentDocument(id: String)
    
    suspend fun readDocument(uriString: String): String
    fun getDisplayName(uriString: String): String?
    
    /** 외부 Uri를 내부 저장소로 복사하여 안정적인 접근을 보장한다. */
    suspend fun cacheExternalFile(uriString: String): String
}
