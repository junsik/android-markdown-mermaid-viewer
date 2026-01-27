# PRD — Android Markdown Viewer (Mermaid Mobile Adaptive)
- Version: 1.0 (MVP 설계 완료)
- Date: 2026-01-27
- Platform: Android
- Primary Goal: Markdown 문서 뷰잉 + Mermaid 렌더링 + 모바일 친화(자동 방향/확대/테마 동기화)

---

## 1. 제품 개요
Android에서 로컬/공유된 Markdown(.md) 문서를 열어 읽을 수 있는 뷰어 앱을 만든다. 문서 내 Mermaid 코드블록(````mermaid`)을 렌더링하며, **모바일 화면 폭/회전/다크모드**에 맞춰 Mermaid 다이어그램을 **자동 최적화(방향 전환 + 확대 뷰 + 테마 동기화)** 한다.

---

## 2. 목표 (Goals)
- G1. Markdown 파일을 안정적으로 렌더링한다 (GFM 일부 포함)
- G2. Mermaid `graph/flowchart`를 SVG로 렌더링한다
- G3. 좁은 화면에서 다이어그램이 잘리지 않도록 **자동 방향 전환(조건부) + 확대/줌** 제공
- G4. 시스템 테마(라이트/다크) 변경 시 Markdown + Mermaid가 함께 자연스럽게 동기화
- G5. 오프라인 기본 동작, 개인정보/문서 외부 전송 없음(기본 정책)

---

## 3. 비목표 (Non-goals)
- Markdown 편집 기능(작성/수정)은 MVP 범위에서 제외
- Git 동기화/Repository 브라우징 기능 제외
- Mermaid 외 PlantUML 등 타 다이어그램 제외

---

## 4. 사용자 & 시나리오
### 4.1 타겟 사용자
- 기술 문서를 모바일에서 자주 읽는 개발자/PM/기획자
- Mermaid로 설계 문서를 작성/공유하는 사용자

### 4.2 핵심 시나리오
1) 파일 열기: SAF(Storage Access Framework)로 .md 선택 → 뷰어 표시  
2) Mermaid 렌더: ` ```mermaid` 블록 자동 SVG 렌더  
3) 좁은 화면 최적화: LR 그래프면 TB로 자동 변환(조건부) + 클릭 시 확대 뷰(핀치 줌)  
4) 테마 동기화: 시스템 다크모드 변경 → Markdown/코드/mermaid 테마 동기화  
5) 검색/TOC: 문서 내 검색, 목차 점프(옵션)

---

## 5. 기능 요구사항 (Functional Requirements)
### 5.1 문서 로딩
- FR-LOAD-01: SAF로 로컬 파일(.md) 열기
- FR-LOAD-02: 최근 문서(Recent) 목록 저장/표시 (URI + title + lastOpenedAt)
- FR-LOAD-03: 권한 만료/URI 접근 실패 시 재승인 유도
- FR-LOAD-04(후순위): URL로 markdown 열기(캐시)

### 5.2 Markdown 렌더링
- FR-MD-01: CommonMark 기반 렌더 + GFM 일부(테이블/체크리스트)
- FR-MD-02: 코드블록 렌더 + 하이라이팅(옵션)
- FR-MD-03: 링크 클릭 시 외부 브라우저 열기(기본) / 인앱 WebView(옵션)
- FR-MD-04: 이미지 렌더(가능한 범위 내), 실패 시 placeholder + 경로 표시
- FR-MD-05: TOC 생성 및 섹션 점프(옵션)
- FR-MD-06: 검색(Find in page)

### 5.3 Mermaid 렌더링
- FR-MER-01: ` ```mermaid` 코드블록 탐지 후 렌더
- FR-MER-02: 렌더 실패 시 원본 코드 + 오류 메시지 표시
- FR-MER-03: 렌더 결과는 SVG(텍스트 선명도/확대 친화)
- FR-MER-04: 다이어그램 탭 시 확대(Focus View)
- FR-MER-05: 테마 변경 시 Mermaid 재렌더(또는 reload)

### 5.4 모바일 최적화 (핵심)
- FR-MOB-01: 자동 방향 전환(휴리스틱)  
  - portrait & 좁은 폭에서 `graph/flowchart LR|RL` 을 TB로 치환(조건부)
- FR-MOB-02: 확대 뷰에서 pinch zoom + pan
- FR-MOB-03: 본문에서 기본은 fit-to-width, 필요 시 수평 스크롤 허용

### 5.5 설정
- FR-SET-01: Theme mode (System/Light/Dark)
- FR-SET-02: Font scale
- FR-SET-03: Mermaid auto direction (ON/OFF)
- FR-SET-04: Link open mode (External/In-app)

---

## 6. 비기능 요구사항 (NFR)
- NFR-01: 오프라인 동작(로컬 문서)
- NFR-02: 문서 내용 외부 전송 없음(기본)
- NFR-03: 안정성 — 렌더 실패는 graceful fallback
- NFR-04: 성능 — p50 TTI 1.5s, p95 3.5s 목표(중간 문서 기준)
- NFR-05: 최소 Android API: 28(권장) 이상

---

## 7. 성공 지표 (Metrics)
- M1: TTI(p50/p95)
- M2: Mermaid 렌더 성공률
- M3: Crash-free sessions
- M4: 최근 문서 재오픈율

---

## 8. 릴리즈 범위
### MVP(v1.0)
- SAF 파일 열기 + 최근 문서
- Markdown 렌더(HTML/WebView 기반)
- Mermaid 렌더(SVG) + 테마 동기화
- 자동 방향 전환(대상 제한/휴리스틱)
- 확대(Focus View) + pinch zoom

### v1.1
- TOC + 검색 UX 개선
- 문서별 설정 기억
- URL 열기(캐시)

---

## 9. 리스크 & 대응
- R1: Mermaid 렌더 네이티브 구현 부담 → WebView+local assets로 최소화
- R2: 방향 변환 부작용 → 대상 제한(graph/flowchart), 잠금 옵션, 실패 시 원본 fallback
- R3: 대형 문서 성능 → 캐시/점진 처리, reload 최소화
