# Agent Council 종합 리뷰 — Android Markdown Viewer + Mermaid
- 리뷰 일자: 2026-01-27
- 리뷰 관점: 아키텍처 / UX / 보안·QA (3인 Council)

---

## 1. 종합 평가

| 항목 | 점수 (5점 만점) | 요약 |
|------|:-:|------|
| 문서 완성도 | 4.0 | 10개 문서가 체계적이며 MVP 범위가 명확. 일부 상세 누락 |
| 문서 간 일관성 | 4.5 | PRD↔Architecture↔Spec 간 정합성 우수 |
| 구현 가능성 | 4.0 | 기술 선택이 현실적이며 MVP 범위가 적절. 난이도 3.5/5 |
| 보안 설계 | 2.5 | 원칙은 좋으나 HTML 삽입 방식에 XSS 취약점 존재 |
| 테스트 계획 | 2.5 | 기본 기능 커버. 보안/성능/엣지케이스 테스트 부재 |
| UX 설계 | 3.5 | 핵심 플로우 명확. Focus View, 접근성, TOC 상세화 필요 |

### 구현 가능성 결론: **구현 가능 (조건부)**
MVP 범위의 기술 선택과 아키텍처가 현실적이며 구현 가능합니다. 다만 보안(HTML Sanitization)과 테스트 보강이 선행 조건입니다.

---

## 2. 핵심 강점

1. **WebView + mermaid.js 전략이 MVP에 최적**: Mermaid 네이티브 구현 부담을 완전히 회피
2. **모바일 최적화가 차별화 포인트**: 자동 방향 전환 + Focus View는 경쟁 앱 대비 강점
3. **명확한 범위 제한**: graph/flowchart만 방향전환 대상으로 제한하여 리스크 관리
4. **오프라인 우선 + 프라이버시 중심**: 외부 전송 없음, SAF 기반 권한 최소화
5. **캐시 키 설계가 정교**: 4가지 요소(docHash, theme, viewport, settings) 조합

---

## 3. 치명적 이슈 (즉시 해결 필요)

### 3.1 XSS 취약점 — `__MARKDOWN_HTML__` 삽입 방식
- **문제**: Markdown→HTML 변환 결과를 문자열 치환으로 템플릿에 삽입하면, 악성 Markdown 파일의 `<script>` 태그나 `onerror` 핸들러가 그대로 실행됨
- **영향**: WebView 내에서 JS Bridge를 통한 앱 기능 악용 가능
- **해결**:
  1. OWASP Java HTML Sanitizer 도입
  2. CommonMark 파서에서 `escapeHtml(true)` + `sanitizeUrls(true)` 설정
  3. HTML 템플릿에 Content Security Policy 메타 태그 추가

### 3.2 보안 테스트 전무
- XSS 공격 벡터, 네트워크 차단 검증, JS Interface 입력 검증 테스트가 전혀 없음
- **해결**: OWASP XSS 치트시트 기반 최소 50개 테스트 벡터 구축

---

## 4. 주요 개선 권고

### 아키텍처 (상세: REVIEW_ARCHITECTURE.md)
1. `domain/` 모듈 추가 — UseCase 분리로 테스트 용이성 확보
2. 캐시 저장소 명시 — 2-tier (메모리 LruCache + 디스크 Room) 권장
3. WebView 생명주기 관리 강화 — `onDestroy`에서 `webView.destroy()` 필수
4. 테마 변경 시 CSS만 교체하는 방식으로 v1.1 개선 예정 필요

### UX (상세: REVIEW_UX.md)
1. 자동 방향 전환 시 **시각적 인디케이터** 필수 (사용자 혼란 방지)
2. Focus View에 **pull-to-dismiss**, **다이어그램 간 스와이프** 추가
3. **TOC UI 구체화** (바텀시트 + 현재 위치 하이라이트 권장)
4. **TalkBack 접근성** 계획 추가 필요
5. **읽기 위치 복원** 기능 MVP에 포함 검토

### 보안·QA (상세: REVIEW_SECURITY_QA.md)
1. HTML Sanitization 라이브러리 즉시 도입
2. JS Interface 입력 검증 (인덱스 범위, 메시지 길이 제한, 로그 인젝션 방지)
3. 회귀 테스트에 **xss_attempt.md**, **large_document.md**, **unicode_heavy.md** 추가
4. 성능 테스트 추가 (대용량 파일 렌더링 시간, 메모리 사용량)
5. Mermaid 버전 고정 + 보안 패치 모니터링 프로세스

---

## 5. 모듈별 구현 난이도

| 모듈 | 난이도 | 비고 |
|------|:-:|------|
| core, data | 1/5 | Room, DataStore boilerplate |
| renderer | 4/5 | Mermaid 전처리 휴리스틱, 오류 처리 복잡 |
| feature/viewer | 4/5 | Compose+WebView 통합, JS Bridge, Focus View |
| feature/home | 2/5 | 단순 리스트 |
| feature/settings | 2/5 | DataStore 읽기/쓰기 |
| **종합** | **3.5/5** | |

---

## 6. 리스크 매트릭스

| 리스크 | 영향 | 확률 | 대응 |
|--------|:----:|:----:|------|
| XSS via 악성 Markdown | 높음 | 중간 | HTML Sanitizer + CSP |
| 대형 문서 성능 저하 | 중간 | 높음 | 캐싱 + 파일 크기 제한 + 로딩 인디케이터 |
| 방향 전환 부작용 | 중간 | 중간 | 대상 제한 + lock + fallback + 시각적 표시 |
| WebView 메모리 누수 | 높음 | 낮음 | `onDestroy` 정리 |
| SAF URI 권한 만료 | 중간 | 높음 | `takePersistableUriPermission` + 재승인 |
| 테마 전환 깜빡임 | 낮음 | 높음 | MVP 수용, v1.1에서 CSS 동적 교체 |

---

## 7. 문서별 품질 평가

| 문서 | 완성도 | 주요 피드백 |
|------|:-:|------|
| PRD | ★★★★☆ | 목표/비목표/시나리오 명확. 성공 지표 측정 방법 추가 필요 |
| ARCHITECTURE | ★★★★☆ | 기술 선택 근거 명확. domain 모듈, 캐시 저장소 구체화 필요 |
| MERMAID_ADAPTIVE_SPEC | ★★★★★ | 대상/조건/fallback 모두 잘 정의됨 |
| HTML_TEMPLATE_AND_BRIDGE | ★★★☆☆ | 보안(Sanitization, CSP) 내용 보강 필수 |
| UX_SPEC | ★★★☆☆ | 핵심 플로우만 기술. TOC, Focus View 상세 부족 |
| IMPLEMENTATION_NOTES | ★★★★☆ | 개념 코드 포함으로 명확. WebView 생명주기 추가 필요 |
| SECURITY_PRIVACY | ★★★☆☆ | 원칙은 좋으나 구체적 구현(Sanitizer, CSP) 누락 |
| TEST_PLAN | ★★☆☆☆ | 기본 기능만 커버. 보안/성능/엣지케이스 전무 |
| ROADMAP | ★★★★☆ | 단계별 범위 명확 |
| README | ★★★☆☆ | 문서 목록만 나열. 빌드/실행 방법 추가 필요 |

---

## 8. 최종 권고 액션 아이템

### MVP 착수 전 필수 (Blocking)
- [ ] HTML Sanitization 전략 확정 및 SECURITY_PRIVACY.md 업데이트
- [ ] TEST_PLAN.md에 보안 테스트 추가
- [ ] HTML_TEMPLATE_AND_BRIDGE.md에 CSP 메타 태그 추가

### MVP 구현 중 (High Priority)
- [ ] `domain/` 모듈 추가
- [ ] WebView 생명주기 관리 코드 표준화
- [ ] 자동 방향 전환 시각적 인디케이터 UX 설계
- [ ] JS Interface 입력 검증 구현

### v1.1 준비 (Medium Priority)
- [ ] 테마 변경 시 CSS만 교체 방식 설계
- [ ] TOC UI/UX 상세 설계
- [ ] 읽기 위치 복원 기능
- [ ] 접근성(TalkBack) 지원 계획
