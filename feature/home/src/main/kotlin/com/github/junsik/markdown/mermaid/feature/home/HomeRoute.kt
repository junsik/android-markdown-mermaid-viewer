package com.github.junsik.markdown.mermaid.feature.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junsik.markdown.mermaid.domain.model.RecentDocument

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onDocumentClick: (RecentDocument) -> Unit,
    onSettingsClick: () -> Unit
) {
    val recentDocuments by viewModel.recentDocuments.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        
        // [정석 해결] Compose Scope를 사용하여 비동기 캐싱 작업 수행
        scope.launch {
            val cachedUriString = viewModel.cacheDocument(uri.toString())
            val title = viewModel.getDisplayName(cachedUriString) ?: "Untitled.md"
            
            onDocumentClick(
                RecentDocument(
                    id = cachedUriString.hashCode().toString(),
                    uriString = cachedUriString,
                    title = title,
                    lastOpenedAt = System.currentTimeMillis()
                )
            )
        }
    }

    HomeScreen(
        recentDocuments = recentDocuments,
        onDocumentClick = { doc ->
            viewModel.addDocument(doc.uriString, doc.title)
            onDocumentClick(doc)
        },
        onDeleteDocument = { doc ->
            viewModel.removeDocument(doc.id)
        },
        onOpenFileClick = {
            filePickerLauncher.launch(arrayOf("text/markdown", "text/plain", "text/*"))
        },
        onSettingsClick = onSettingsClick
    )
}
