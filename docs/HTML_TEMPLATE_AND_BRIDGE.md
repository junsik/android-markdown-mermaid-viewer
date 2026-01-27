# HTML Template — v1.1 (Secure)
Date: 2026-01-27

## Key Changes
- CSP 메타 태그 추가
- Sanitized HTML만 삽입됨
- 외부 네트워크 차단 전제

```html
<meta http-equiv="Content-Security-Policy"
content="default-src 'none';
script-src 'self' 'unsafe-inline';
style-src 'self' 'unsafe-inline';
img-src 'self' data:;
connect-src 'none';">
```
