package com.github.junsik.markdown.renderer

object MarkdownMermaidClient {
    fun wrapHtml(body: String, theme: String = "default"): String {
        return """
            <!DOCTYPE html>
            <html class="${if (theme == "dark") "dark" else ""}">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        line-height: 1.6;
                        color: #1e293b;
                        padding: 1.5rem;
                        max-width: 100%;
                        background-color: #ffffff;
                    }
                    html.dark body { color: #f8fafc; background-color: #0f172a; }
                    a { color: #2563eb; }
                    html.dark a { color: #60a5fa; }
                    pre {
                        background: #f1f5f9;
                        border-radius: 8px;
                        padding: 1rem;
                        overflow-x: auto;
                    }
                    html.dark pre { background: #1e293b; }
                    code { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }
                    img { max-width: 100%; height: auto; border-radius: 8px; }
                    table { border-collapse: collapse; width: 100%; margin: 1rem 0; }
                    th, td { border: 1px solid #e2e8f0; padding: 0.75rem; text-align: left; }
                    html.dark th, html.dark td { border-color: #334155; }
                    th { background-color: #f8fafc; }
                    html.dark th { background-color: #0f172a; }
                    .mermaid-wrapper {
                        margin: 1.5rem -1.5rem;
                        border-top: 1px solid #e2e8f0;
                        border-bottom: 1px solid #e2e8f0;
                        background: #f8fafc;
                        overflow-x: auto;
                        -webkit-overflow-scrolling: touch;
                        position: relative;
                        cursor: pointer;
                    }
                    html.dark .mermaid-wrapper {
                        border-color: #334155;
                        background: #0f172a;
                    }
                    .mermaid-wrapper .mermaid {
                        display: flex;
                        justify-content: center;
                        padding: 1rem;
                        min-width: fit-content;
                    }
                    .mermaid-wrapper .mermaid svg {
                        display: block;
                    }
                    .mermaid-expand-hint {
                        position: sticky;
                        left: calc(100% - 6rem);
                        bottom: 0.5rem;
                        display: inline-block;
                        margin: 0 0.5rem 0.5rem 0;
                        background: rgba(0,0,0,0.6);
                        color: #fff;
                        font-size: 0.7rem;
                        padding: 0.25rem 0.6rem;
                        border-radius: 4px;
                        pointer-events: none;
                        float: right;
                    }
                </style>
                <script src="mermaid.min.js"></script>
                <script>
                    mermaid.initialize({
                        startOnLoad: true,
                        theme: '$theme',
                        securityLevel: 'loose',
                        themeVariables: {
                            fontFamily: 'inherit'
                        }
                    });
                </script>
            </head>
            <body>
                <div id="content">
                    $body
                </div>
                <script>
                    document.querySelectorAll('code.language-mermaid').forEach(function(codeBlock) {
                        var pre = codeBlock.parentElement;
                        if (pre.tagName === 'PRE') {
                            var wrapper = document.createElement('div');
                            wrapper.className = 'mermaid-wrapper';

                            var source = codeBlock.textContent.replace(/%%\{init:.*?\}%%/g, '').trim();
                            wrapper.setAttribute('data-mermaid-source', source);

                            var div = document.createElement('div');
                            div.className = 'mermaid';
                            div.textContent = source;

                            var hint = document.createElement('span');
                            hint.className = 'mermaid-expand-hint';
                            hint.textContent = '탭하여 확대';

                            wrapper.appendChild(div);
                            wrapper.appendChild(hint);
                            pre.replaceWith(wrapper);
                        }
                    });

                    mermaid.run().then(function() {
                        document.querySelectorAll('.mermaid-wrapper .mermaid svg').forEach(function(svg) {
                            var style = svg.getAttribute('style') || '';
                            style = style.replace(/max-width:[\s]*[\d.]+px;?/gi, '');
                            svg.setAttribute('style', style);
                        });

                        document.querySelectorAll('.mermaid-wrapper').forEach(function(wrapper) {
                            wrapper.addEventListener('click', function() {
                                var svg = wrapper.querySelector('svg');
                                var source = wrapper.getAttribute('data-mermaid-source') || '';
                                if (svg && window.AndroidBridge) {
                                    window.AndroidBridge.openDiagram(svg.outerHTML, source);
                                }
                            });
                        });
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
