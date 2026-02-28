package com.github.junsik.markdown.mermaid.feature.viewer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junsik.markdown.renderer.MarkdownMermaidClient

@Composable
fun ViewerRoute(
    viewModel: ViewerViewModel = hiltViewModel(),
    uriString: String,
    isDark: Boolean,
    isTableFormat: Boolean = false,
    onBackClick: () -> Unit,
    onNavigateToDocument: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uriString, isTableFormat) {
        viewModel.loadDocument(uriString, isTableFormat)
    }

    when (val state = uiState) {
        is ViewerUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ViewerUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is ViewerUiState.Success -> {
            ViewerScreen(
                htmlContent = state.htmlContent,
                isDark = isDark,
                currentDocumentUri = uriString,
                onBackClick = onBackClick,
                onNavigateToDocument = onNavigateToDocument
            )
        }
    }
}

@Composable
fun ViewerScreen(
    htmlContent: String,
    isDark: Boolean,
    currentDocumentUri: String,
    onBackClick: () -> Unit,
    onNavigateToDocument: ((String) -> Unit)? = null
) {
    var diagramData by remember { mutableStateOf<Pair<String, String>?>(null) } // svgHtml, mermaidSource

    BackHandler { onBackClick() }

    Box(Modifier.fillMaxSize()) {
        WebViewComponent(
            htmlContent = htmlContent,
            isDark = isDark,
            currentDocumentUri = currentDocumentUri,
            onDiagramClick = { svgHtml, source -> diagramData = Pair(svgHtml, source) },
            onNavigateToDocument = onNavigateToDocument
        )

        // 뒤로가기 버튼 (좌상단, 반투명)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(40.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = diagramData != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DiagramFullscreenViewer(
                svgHtml = diagramData?.first ?: "",
                isDark = isDark,
                onClose = { diagramData = null }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DiagramFullscreenViewer(
    svgHtml: String,
    isDark: Boolean,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val bgColor = if (isDark) {
        androidx.compose.ui.graphics.Color(0xFF1a1a2e)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF8FAFC)
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.setSupportZoom(true)
                    webViewClient = WebViewClient()
                    val bgHex = if (isDark) 0xFF1a1a2e.toInt() else 0xFFF8FAFC.toInt()
                    setBackgroundColor(bgHex)
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = true
                    val html = buildSvgViewerHtml(svgHtml, isDark)
                    val b64 = Base64.encodeToString(html.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
                    loadData(b64, "text/html; charset=utf-8", "base64")
                    webViewRef = this
                }
            }
        )

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(44.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "닫기",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        // Zoom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webViewRef?.zoomIn() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "확대",
                    tint = androidx.compose.ui.graphics.Color.White)
            }
            IconButton(
                onClick = { webViewRef?.zoomOut() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "축소",
                    tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

// SVG viewer - displays pre-rendered SVG, fit to screen width, centered
private fun buildSvgViewerHtml(svgHtml: String, isDark: Boolean): String {
    val bg = if (isDark) "#1a1a2e" else "#F8FAFC"
    return StringBuilder().apply {
        append("<!DOCTYPE html><html><head>")
        append("<meta charset=\"utf-8\">")
        append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=10.0, user-scalable=yes\">")
        append("<style>")
        append("* { margin: 0; padding: 0; box-sizing: border-box; }")
        append("html, body { width: 100%; min-height: 100vh; background: ").append(bg).append("; }")
        append("body { display: flex; justify-content: center; align-items: center; padding: 12px; }")
        append("body > svg, body > div { max-width: 100%; height: auto; transition: transform 0.3s ease; }")
        append("svg { height: auto !important; }")
        append("</style>")
        append("</head><body>")
        append("<div id=\"container\">").append(svgHtml).append("</div>")
        append("<script>")
        append("var container = document.getElementById('container');")
        append("var lastTap = 0;")
        append("document.addEventListener('touchstart', function(e) {")
        append("  var now = new Date().getTime();")
        append("  var timesince = now - lastTap;")
        append("  if ((timesince < 300) && (timesince > 0)) {")
        append("    // Double tap detected")
        append("    e.preventDefault();")
        append("    container.style.transition = 'transform 0.4s cubic-bezier(0.4, 0, 0.2, 1)';")
        append("    container.style.transform = 'scale(1)';")
        append("    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });")
        append("  }")
        append("  lastTap = now;")
        append("}, { passive: false });")
        append("</script>")
        append("</body></html>")
    }.toString()
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(
    htmlContent: String,
    isDark: Boolean,
    currentDocumentUri: String,
    onDiagramClick: (String, String) -> Unit,
    onNavigateToDocument: ((String) -> Unit)? = null
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                settings.allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
                setBackgroundColor(if (isDark) 0xFF0F172A.toInt() else Color.WHITE)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url ?: return false
                        val urlString = url.toString()

                        // HTTP/HTTPS → 외부 브라우저
                        if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, url))
                            return true
                        }

                        // 상대 경로 .md 링크 → 앱 내 뷰어로 열기
                        // file:///android_asset/ 기반 상대 경로가 resolve된 URL이 올 수 있음
                        if (urlString.endsWith(".md") || urlString.endsWith(".markdown")) {
                            val resolved = resolveRelativeMdLink(currentDocumentUri, urlString)
                            if (resolved != null) {
                                onNavigateToDocument?.invoke(resolved)
                                return true
                            }
                        }

                        return false
                    }
                }

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun openDiagram(svgHtml: String, mermaidSource: String) {
                        post { onDiagramClick(svgHtml, mermaidSource) }
                    }
                }, "AndroidBridge")
            }
        },
        update = { webView ->
            val theme = if (isDark) "dark" else "default"
            webView.loadDataWithBaseURL(
                "file:///android_asset/",
                MarkdownMermaidClient.wrapHtml(htmlContent, theme),
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

/**
 * 현재 문서 URI와 상대 경로 링크를 기반으로 실제 content:// URI를 구성한다.
 * content://.../.../dir/current.md + ./other.md → content://.../.../dir/other.md
 */
private fun resolveRelativeMdLink(currentDocumentUri: String, linkUrl: String): String? {
    // file:///android_asset/ 경로는 무시 (앱 내부 asset)
    if (linkUrl.startsWith("file:///android_asset/")) return null

    val currentUri = Uri.parse(currentDocumentUri)
    val currentPath = currentUri.path ?: return null

    // 링크에서 file:// prefix 제거
    val relativePath = linkUrl
        .removePrefix("file:///android_asset/")
        .removePrefix("file://")
        .removePrefix("/")

    // 현재 문서의 디렉토리 경로
    val parentDir = currentPath.substringBeforeLast('/', "")

    // 상대 경로 해석
    val resolvedPath = if (relativePath.startsWith("/")) {
        relativePath
    } else {
        "$parentDir/$relativePath"
    }

    // content:// URI인 경우 경로를 재구성
    return currentUri.buildUpon()
        .path(resolvedPath)
        .build()
        .toString()
}
