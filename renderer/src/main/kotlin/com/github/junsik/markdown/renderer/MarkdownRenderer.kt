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
        val rows = tableHtml.split(Regex("""<tr.*?>.*?</tr>""", RegexOption.DOT_MATCHES_ALL))
            .filter { it.contains("<th") || it.contains("<td") }

        if (rows.isEmpty()) return tableHtml

        val sb = StringBuilder()
        sb.append("<div style=\"border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; margin: 1rem 0;\">")

        // 헤더 추출
        val headerRow = rows.firstOrNull { it.contains("<th") }
        val headers = if (headerRow != null) {
            Regex("""<th[^>]*>(.*?)</th>""", RegexOption.DOT_MATCHES_ALL)
                .findAll(headerRow)
                .map { it.groupValues[1].trim() }
                .toList()
        } else {
            emptyList()
        }

        // 데이터 행들
        val dataRows = rows.drop(if (headerRow != null) 1 else 0)

        dataRows.forEach { row ->
            val cells = Regex("""<td[^>]*>(.*?)</td>""", RegexOption.DOT_MATCHES_ALL)
                .findAll(row)
                .map { it.groupValues[1].trim() }
                .toList()

            sb.append("<div style=\"padding: 1rem; border-bottom: 1px solid #e2e8f0;\">")

            cells.forEachIndexed { index, cell ->
                val header = if (index < headers.size) headers[index] else "Column ${index + 1}"
                sb.append("<div style=\"margin-bottom: 0.5rem;\">")
                sb.append("<strong>$header:</strong> $cell")
                sb.append("</div>")
            }

            sb.append("</div>")
        }

        sb.append("</div>")
        return sb.toString()
    }
}
