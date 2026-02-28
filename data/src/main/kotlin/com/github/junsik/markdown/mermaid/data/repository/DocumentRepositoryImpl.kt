package com.github.junsik.markdown.mermaid.data.repository

import android.content.Context
import android.provider.OpenableColumns
import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class DocumentRepositoryImpl(
    private val context: Context
) : DocumentRepository {

    private val prefs = context.getSharedPreferences("recent_documents", Context.MODE_PRIVATE)
    private val _documents = MutableStateFlow(loadFromPrefs())

    override fun getRecentDocuments(): Flow<List<RecentDocument>> = _documents.asStateFlow()

    override suspend fun addRecentDocument(document: RecentDocument) {
        val current = _documents.value.toMutableList()
        current.removeAll { it.uriString == document.uriString }
        current.add(0, document.copy(lastOpenedAt = System.currentTimeMillis()))
        _documents.value = current
        saveToPrefs(current)
    }

    override suspend fun removeRecentDocument(id: String) {
        val current = _documents.value.toMutableList()
        current.removeAll { it.id == id }
        _documents.value = current
        saveToPrefs(current)
    }

    override suspend fun readDocument(uriString: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val uri = android.net.Uri.parse(uriString)
                
                // 1. 이미 앱 내부로 캐시된 파일인 경우
                if (uri.scheme == "file") {
                    val path = uri.path ?: return@withContext "# Error\n\nInvalid file path"
                    return@withContext java.io.File(path).readText()
                }

                // 2. 외부 Content URI인 경우
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: "# Error\n\nCould not open document: $uriString"
            } catch (e: Exception) {
                "# Error\n\nCould not read document.\n${e.message}"
            }
        }
    }

    override fun getDisplayName(uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            if (uri.scheme == "file") {
                return uri.lastPathSegment
            }
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: uri.lastPathSegment
        } catch (_: Exception) {
            val uri = android.net.Uri.parse(uriString)
            uri.lastPathSegment ?: "Untitled.md"
        }
    }

    override suspend fun cacheExternalFile(uriString: String): String = withContext(Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(uriString)
            
            // 이미 로컬 파일이면 그대로 반환
            if (uri.scheme == "file") return@withContext uriString

            // 파일 이름 결정
            val displayName = getDisplayName(uriString) ?: "temp_${System.currentTimeMillis()}.md"
            val safeFileName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            
            // 앱 전용 내부 저장소 폴더 생성 (files/external)
            val externalDir = java.io.File(context.filesDir, "external").apply { mkdirs() }
            val targetFile = java.io.File(externalDir, safeFileName)

            // 복사 수행 (ActivityContext에서 실행되어야 권한 문제가 없음)
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            android.net.Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            uriString // 실패하면 어쩔 수 없이 원본 반환
        }
    }

    private fun loadFromPrefs(): List<RecentDocument> {
        val json = prefs.getString("list", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                RecentDocument(
                    id = obj.getString("id"),
                    uriString = obj.getString("uriString"),
                    title = obj.getString("title"),
                    lastOpenedAt = obj.getLong("lastOpenedAt"),
                    contentTitle = if (obj.has("contentTitle")) obj.getString("contentTitle") else null,
                    path = if (obj.has("path")) obj.getString("path") else null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToPrefs(documents: List<RecentDocument>) {
        val array = JSONArray()
        documents.forEach { doc ->
            array.put(JSONObject().apply {
                put("id", doc.id)
                put("uriString", doc.uriString)
                put("title", doc.title)
                put("lastOpenedAt", doc.lastOpenedAt)
                doc.contentTitle?.let { put("contentTitle", it) }
                doc.path?.let { put("path", it) }
            })
        }
        prefs.edit().putString("list", array.toString()).apply()
    }
}
