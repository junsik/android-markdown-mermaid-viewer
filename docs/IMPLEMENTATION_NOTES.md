# Implementation Notes (Kotlin/Compose/WebView)
- Version: 1.0
- Date: 2026-01-27

---

## 1. RenderPipeline (MVP)
1) readMarkdown(uri)  
2) parseFrontMatter(optional)  
3) extractMermaidBlocks  
4) applyDirectionPolicy(blocks, viewport, settings, lock)  
5) markdownToHtml  
6) injectHtmlIntoTemplate(theme, fontScale)  
7) webView.loadDataWithBaseURL(assetsBaseUrl, html, ...)

---

## 2. Direction Policy (개념 코드)
```kotlin
data class Viewport(val widthDp: Int, val isPortrait: Boolean)

fun autoAdjustMermaidDirection(
  code: String,
  viewport: Viewport,
  locked: Boolean
): String {
  if (locked) return code
  if (!viewport.isPortrait || viewport.widthDp >= 420) return code

  val headerRegex = Regex("^(graph|flowchart)\\s+(LR|RL)\\b", RegexOption.MULTILINE)
  val m = headerRegex.find(code) ?: return code

  val edgeCount = Regex("-->").findAll(code).count()
  val labels = Regex("\\[(.*?)\\]").findAll(code).map { it.groupValues[1].length }.toList()
  val avgLabel = if (labels.isEmpty()) 0.0 else labels.average()

  if (edgeCount < 6 && avgLabel < 12.0 && code.length < 300) return code

  return code.replaceFirst(headerRegex, "${m.groupValues[1]} TB")
}
```

---

## 3. Focus View
- 별도 화면으로 분리(추천)
- 단일 diagram HTML만 포함(템플릿 재사용)
- zoom/pan: WebView 확대 또는 JS/CSS transform 기반
