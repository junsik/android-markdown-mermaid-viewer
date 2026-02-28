package com.github.junsik.markdown.mermaid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    val recentDocuments = documentRepository.getRecentDocuments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeDocument(id: String) {
        viewModelScope.launch {
            documentRepository.removeRecentDocument(id)
        }
    }

    fun addDocument(uriString: String, title: String) {
        viewModelScope.launch {
            documentRepository.addRecentDocument(
                RecentDocument(
                    id = uriString.hashCode().toString(),
                    uriString = uriString,
                    title = title,
                    lastOpenedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun cacheDocument(uriString: String): String {
        return documentRepository.cacheExternalFile(uriString)
    }

    fun getDisplayName(uriString: String): String? {
        return documentRepository.getDisplayName(uriString)
    }
}
