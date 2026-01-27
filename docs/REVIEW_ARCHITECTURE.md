# 아키텍처 리뷰 — Android Markdown Viewer + Mermaid
- 리뷰어: Architecture Agent (시니어 Android 아키텍트 관점)
- 일자: 2026-01-27

---

## 1. 아키텍처 결정 평가

### WebView + HTML 렌더링 선택: 적절함
- Mermaid 네이티브 구현은 비현실적이며, 공식 `mermaid.js` 활용이 합리적
- commonmark-java는 검증된 라이브러리이고 WebView는 HTML/CSS를 완벽 지원
- HTML template에서 CSS 변수로 테마를 제어하는 것이 네이티브보다 유연

### 주의점
- 대형 문서(10,000줄+)나 다수 Mermaid 다이어그램(20개+)에서 초기 로딩 지연 가능
- WebView 별도 프로세스로 인한 메모리 관리 주의 필요

---

## 2. 모듈 구조 개선안

### 현재 (문서)
```
core/ → data/ → renderer/ → feature/viewer/ → feature/home/ → feature/settings/
```

### 권장
```
:app                     → Application, DI, Navigation
:core                    → 공통 유틸
:domain                  → UseCases, Repository Interfaces
:data                    → Repository 구현, Room, DataStore
:renderer                → Markdown/Mermaid 렌더 엔진
:feature:viewer
:feature:home
:feature:settings
```

**근거**: Clean Architecture 원칙에 따라 비즈니스 로직을 프레임워크(WebView, Room)로부터 격리하면 테스트가 용이해짐.

---

## 3. 데이터 흐름 개선

### 전체 reload 문제
테마 변경/회전 시 전체 reload는 MVP에서 수용 가능하나, 대형 문서에서 UX 문제 발생.

**v1.1 개선안**:
- 테마 변경 → CSS 클래스만 교체 + Mermaid 블록만 재렌더
- 회전 → 캐시 히트 시 즉시 표시

### Mermaid Fallback 복잡성
변환 실패 시 해당 블록만 원본으로 교체하는 부분 reload 방식 검토 필요.

---

## 4. 캐시 전략 보강 필요

### 누락 사항
1. **캐시 대상 명확화**: Markdown→HTML 변환 결과만 캐싱 권장 (template 삽입은 매번 수행)
2. **viewportBucket 정의**: `PORTRAIT_NARROW(<420dp)`, `PORTRAIT_WIDE(>=420dp)`, `LANDSCAPE`
3. **저장소**: 2-tier — 메모리 LruCache(최근 3개) + 디스크 Room(최근 20개, 7일 TTL)
4. **만료 정책**: 캐시 조회 전 `DocumentFile.lastModified()` 확인

---

## 5. Compose + WebView 혼합 문제

| 문제 | 해결책 |
|------|--------|
| AndroidView 재구성 시 WebView 재생성 | `remember(documentUri)` 로 URI 변경 시만 재생성 |
| 키보드/WebView 포커스 전환 | `focusRequester`로 명시적 관리 |
| 회전 시 WebView 스크롤 위치 유실 | ViewModel에서 scrollY 보존 후 복원 |
| 메모리 누수 | `DisposableEffect`의 `onDispose`에서 `webView.destroy()` |

---

## 6. 확장성 평가

### 확장 가능
- Mermaid 전처리를 `MermaidPreprocessor` 인터페이스로 추상화 → v1.2 themeVariables 대응
- `DiagramRenderer` 인터페이스로 PlantUML 등 추가 가능

### 추가 작업 필요
- **URL 캐싱 (v1.1)**: OkHttp + 캐시 정책, 네트워크 상태 모니터링 → 별도 DataSource
- **문서별 설정 (v1.1)**: Room에 `DocumentSettings` 테이블 추가
- **TOC (v1.1)**: commonmark-java AST에서 헤더 노드 추출 필요 → RenderPipeline 수정

---

## 7. 구현 난이도: 3.5/5

핵심 난제:
1. Mermaid 렌더 실패 처리 (5/5) — 에러 전달, fallback 체인
2. 방향 전환 휴리스틱 엣지 케이스 (4/5) — subgraph, 주석, 복잡한 정규식
3. WebView 보안 설정 (3/5) — strict 모드와 기능 제한 간 균형
4. 캐싱 무효화 로직 (3/5) — 다중 조건 조합, race condition
