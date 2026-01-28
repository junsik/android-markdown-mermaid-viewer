package com.github.junsik.markdown.mermaid.feature.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        // 영구 읽기 권한 획득
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // 일부 provider는 persistable 권한을 지원하지 않음
        }
        val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Untitled.md"
        viewModel.addDocument(uri.toString(), title)
        onDocumentClick(
            RecentDocument(
                id = uri.toString().hashCode().toString(),
                uriString = uri.toString(),
                title = title,
                lastOpenedAt = System.currentTimeMillis()
            )
        )
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
