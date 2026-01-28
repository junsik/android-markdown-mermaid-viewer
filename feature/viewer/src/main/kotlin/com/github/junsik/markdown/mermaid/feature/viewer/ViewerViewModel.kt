package com.github.junsik.markdown.mermaid.feature.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository
import com.github.junsik.markdown.renderer.MarkdownRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val markdownRenderer = MarkdownRenderer()
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadDocument(uriString: String) {
        viewModelScope.launch {
            _uiState.value = ViewerUiState.Loading
            try {
                val markdown = documentRepository.readDocument(uriString)
                val html = withContext(Dispatchers.Default) {
                    markdownRenderer.render(markdown)
                }
                _uiState.value = ViewerUiState.Success(html)

                // 열람 이력 기록
                val uri = android.net.Uri.parse(uriString)
                val contentTitle = extractMarkdownTitle(markdown)
                val displayName = documentRepository.getDisplayName(uriString) ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Untitled.md"
                val fileName = displayName
                documentRepository.addRecentDocument(
                    RecentDocument(
                        id = uriString.hashCode().toString(),
                        uriString = uriString,
                        title = fileName,
                        lastOpenedAt = System.currentTimeMillis(),
                        contentTitle = contentTitle,
                        path = null
                    )
                )
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    companion object {
        /** 마크다운 첫 번째 # 제목 추출 */
        fun extractMarkdownTitle(markdown: String): String? {
            for (line in markdown.lineSequence()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("# ")) {
                    return trimmed.removePrefix("# ").trim().takeIf { it.isNotEmpty() }
                }
                // 빈 줄이나 front-matter(---) 등은 건너뛰기
                if (trimmed.isNotEmpty() && !trimmed.startsWith("---")) break
            }
            return null
        }
    }
}

sealed interface ViewerUiState {
    object Loading : ViewerUiState
    data class Success(val htmlContent: String) : ViewerUiState
    data class Error(val message: String) : ViewerUiState
}
