package com.github.junsik.markdown.renderer

import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

class MarkdownRenderer {
    private val options = MutableDataSet().apply {
        set(Parser.EXTENSIONS, listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create(),
            AutolinkExtension.create()
        ))
        // Basic HTML generation options
        set(HtmlRenderer.GENERATE_HEADER_ID, true)
    }

    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun render(markdown: String, convertTableToList: Boolean = false): String {
        val document = parser.parse(markdown)
        var html = renderer.render(document)

        if (convertTableToList) {
            html = convertTablesToLists(html)
        }

        return html
    }

    /**
     * HTML table을 목록 형식으로 변환
     */
    private fun convertTablesToLists(html: String): String {
        return html.replace(Regex("""<table>.*?</table>""", RegexOption.DOT_MATCHES_ALL)) { match ->
            val tableHtml = match.value
            convertTableToList(tableHtml)
        }
    }

    /**
     * 단일 table HTML을 목록 형식으로 변환
     */
    private fun convertTableToList(tableHtml: String): String {
        // 모든 <tr> 태그 찾기
        val trPattern = Regex("""<tr[^>]*>(.*?)</tr>""", RegexOption.DOT_MATCHES_ALL)
        val trMatches = trPattern.findAll(tableHtml).toList()

        if (trMatches.isEmpty()) return tableHtml

        val sb = StringBuilder()
        sb.append("""<div style="border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; margin: 1rem 0;">""")

        // 첫 번째 행에서 헤더 추출
        val firstRowContent = trMatches.firstOrNull()?.groupValues?.get(1) ?: return tableHtml
        val headers = extractCells(firstRowContent, "th")

        // 헤더가 없으면 첫 번째 행도 데이터로 처리
        val dataStartIndex = if (headers.isNotEmpty()) 1 else 0

        // 데이터 행들 처리
        trMatches.drop(dataStartIndex).forEach { match ->
            val rowContent = match.groupValues[1]
            val cells = extractCells(rowContent, "td")

            if (cells.isNotEmpty()) {
                sb.append("""<div style="padding: 1rem; border-bottom: 1px solid #e2e8f0;">""")

                cells.forEachIndexed { index, cell ->
                    val header = if (index < headers.size) headers[index] else "Column ${index + 1}"
                    sb.append("""<div style="margin-bottom: 0.5rem;"><strong>$header:</strong> $cell</div>""")
                }

                sb.append("</div>")
            }
        }

        sb.append("</div>")
        return sb.toString()
    }

    /**
     * 행(row)에서 셀(cell) 추출
     */
    private fun extractCells(rowContent: String, cellTag: String): List<String> {
        val cellPattern = Regex("""<$cellTag[^>]*>(.*?)</$cellTag>""", RegexOption.DOT_MATCHES_ALL)
        return cellPattern.findAll(rowContent)
            .map { it.groupValues[1].trim() }
            .toList()
    }
}
