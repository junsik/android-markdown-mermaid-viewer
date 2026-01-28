package com.github.junsik.markdown.mermaid.data.repository

import android.content.Context
import android.provider.OpenableColumns
import com.github.junsik.markdown.mermaid.domain.model.RecentDocument
import com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        return try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: "# Error\n\nCould not open: $uriString"
        } catch (e: Exception) {
            "# Error\n\n${e.message}"
        }
    }

    override fun getDisplayName(uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: Exception) { null }
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
