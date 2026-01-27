# Android Markdown Viewer + Mermaid — FINAL Design Pack
- Date: 2026-01-27
- Status: FINAL (v1.1 review-applied)

## Included (canonical)
- PRD.md
- ARCHITECTURE.md (v1.1 applied)
- MERMAID_ADAPTIVE_SPEC.md
- HTML_TEMPLATE_AND_BRIDGE.md (v1.1 secure template/CSP/sanitization notes applied)
- UX_SPEC.md (v1.1 applied)
- SECURITY_PRIVACY.md (v1.1 applied)
- TEST_PLAN.md (v1.1 applied)
- IMPLEMENTATION_NOTES.md
- ROADMAP.md

## Review traceability
- REVIEW_RESOLUTION_SUMMARY.md
- REVIEW_SUMMARY.md
- REVIEW_ARCHITECTURE.md
- REVIEW_UX.md
- REVIEW_SECURITY_QA.md

## Final decisions (high level)
- Rendering: Markdown → HTML → Sanitizer → WebView (Mermaid via assets mermaid.js)
- Security: OWASP Java HTML Sanitizer + CSP + WebView hardening + JS bridge input validation
- Mobile: auto direction (graph/flowchart only) + Focus View (pinch/pan) + indicator + original view option
