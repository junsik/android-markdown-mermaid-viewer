# Security & Privacy — v1.1 (Review Applied)
Date: 2026-01-27

## 1. HTML Sanitization (MANDATORY)
- OWASP Java HTML Sanitizer 적용
- 허용 태그: p, h1-h6, ul, ol, li, pre, code, blockquote, table, thead, tbody, tr, td, th, img, a, span, div, svg
- 금지: script, iframe, object, embed, on* 이벤트, javascript: URL

### Pipeline
Markdown → HTML → **Sanitizer** → Template Inject → WebView

## 2. Markdown Parser Hardening
- escapeHtml = true
- sanitizeUrls = true
- 링크 스킴 허용: http, https, mailto

## 3. Content Security Policy
```html
<meta http-equiv="Content-Security-Policy"
content="default-src 'none';
script-src 'self' 'unsafe-inline';
style-src 'self' 'unsafe-inline';
img-src 'self' data:;
font-src 'self';
connect-src 'none';">
```

## 4. WebView Hardening
- domStorageEnabled = false
- databaseEnabled = false
- geolocationEnabled = false
- savePassword = false
- WebView.setDataDirectorySuffix("md_mermaid")

## 5. JS Interface Guard
- index 범위 검증
- 메시지 길이 제한(1000)
- UI thread marshal
- 로그 인젝션 방지

## 6. Privacy
- 외부 네트워크 요청 0 (MVP)
- 문서 원문 로그 기록 금지
