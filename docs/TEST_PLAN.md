# Test Plan — v1.1 (Security/UX/Perf)
Date: 2026-01-27

## 1. Security Tests (Blocking)
- xss_script_tag.md
- xss_img_onerror.md
- xss_javascript_url.md
- mermaid_html_injection.md
- js_interface_bounds.md

## 2. Edge Cases
- empty.md
- 10mb_large.md
- unicode_heavy.md
- broken_mermaid.md

## 3. Performance
- 100 diagrams render time
- memory peak tracking
- scroll FPS

## 4. Accessibility
- TalkBack reading order
- focus traversal
- touch target >=48dp
