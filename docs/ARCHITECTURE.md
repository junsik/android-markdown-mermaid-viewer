# Architecture: Android Markdown Viewer with Mermaid

## 1. 기술 스택(권장)
- UI: Jetpack Compose
- Markdown 파싱: commonmark-java(+GFM extension) 또는 Markwon(유력)
- Mermaid 렌더링: WebView + mermaid.min.js (로컬 asset)
- 이미지: Coil
- DI: Hilt(선택)
- 상태/비동기: Kotlin Coroutines + Flow
- 저장: DataStore(설정), Room(최근 문서/북마크)

## 2. 핵심 설계 의도
- Markdown은 네이티브로 파싱/렌더(Compose)하되,
- Mermaid는 "브라우저 엔진(WebView)"에서 SVG로 렌더링한 뒤
  - (A) WebView 안에서 Markdown 전체를 렌더하거나
  - (B) 네이티브 Markdown 렌더 + Mermaid 부분만 WebView로 삽입
중 선택해야 한다.

권장안은 **A: 전체를 WebView(HTML)로 렌더**:
- Mermaid/코드 하이라이트/테마 동기화가 쉬움
- 레이아웃 일관성 좋음
- 단점: Compose 네이티브 컴포넌트(TOC/검색/선택) 구현이 다소 불편

대안은 **B: Hybrid**:
- Compose에서 Markdown 컴포넌트로 렌더
- Mermaid 블록만 WebView 컴포넌트로 렌더(SVG)
- 장점: UI/상호작용 네이티브, 단점: 일관성/성능/스크롤 충돌 이슈 가능

MVP에서는 A가 리스크가 가장 낮다.

## 3. 권장 아키텍처(옵션 A: HTML/WebView 중심)

### 3.1 렌더 파이프라인
1) SAF URI로 파일 읽기 → raw markdown text
2) Markdown 전처리
   - Mermaid 코드블록 추출/치환(placeholder id 부여)
3) Markdown → HTML 변환
4) HTML 템플릿에 삽입
   - mermaid.min.js 로드(assets)
   - highlight.js 로드(옵션)
   - theme css 적용
5) WebView loadDataWithBaseURL 로 렌더
6) mermaid.initialize + mermaid.run
7) 다이어그램 클릭 이벤트 → Android로 브릿지 → Focus View로 확대

### 3.2 Mermaid 렌더 HTML 템플릿 예(개념)
- `<pre class="mermaid">...</pre>` 형태로 Mermaid 블록 삽입
- JS에서 mermaid.run() 실행
- 테마 변경 시 mermaid.initialize({ theme }) 후 재렌더

### 3.3 테마 동기화
- Android에서 현재 테마(시스템/라이트/다크) 결정
- WebView에 JS injection:
  - `window.__APP_THEME__ = "dark" | "light"`
  - mermaid config의 theme 및 themeVariables 변경
- 테마 변경 이벤트 감지 시:
  - 전체 reload 또는 mermaid만 재렌더(성능 고려)

## 4. 모바일 화면 최적화(방향 자동 전환)

### 4.1 요구사항 해석
- Mermaid 그래프는 `graph TD`, `graph LR`, `flowchart TB` 등 방향 지시자를 가진다.
- 모바일에서는 LR이 가로로 길어져 잘리는 경우가 많다.
- 따라서 특정 조건(화면 폭, 예상 노드 수/텍스트 길이 등)에서
  - LR → TB 로 변환하는 "안전한 전처리"가 필요하다.

### 4.2 방향 전환 알고리즘(현실적인 접근)
- Mermaid 블록별로 다음을 계산:
  - viewportWidthDp, isLandscape
  - 다이어그램 내용 길이(문자수), 줄 수, 노드 수의 근사치(정규식 기반)
- 휴리스틱:
  - portrait & viewportWidthDp < 420dp 이고
  - (노드 수가 많거나) 텍스트가 길거나 (LR 선언일 때)
  - => 방향을 TB로 강제
- 변환 방법:
  - `graph LR` / `flowchart LR` / `direction LR` 등을 탐지하여 `TB`로 치환
  - 단, 사용자가 명시적으로 방향을 고정하고 싶을 수 있으므로:
    - 문서 메타 옵션 지원(예: front-matter `mermaidDirectionLock: true`)
    - 앱 설정에서 "자동 방향 전환" 토글 제공

### 4.3 안전장치
- 일부 Mermaid는 direction 치환 시 의미가 깨질 수 있음(예: subgraph 내 direction 등)
- 최소 침습 전략:
  - 최상단 선언만 바꾸고 subgraph 내부는 건드리지 않음(1차)
  - 변환 실패/렌더 오류 발생 시 원본 방향으로 fallback

### 4.4 UX 대체/보완책(필수로 같이 제공 권장)
- 방향 변환만으로 해결 안 되는 경우가 많으므로
  - "다이어그램 확대 보기"에서 pinch-zoom & pan 제공
  - 기본 본문에서는 fit-to-width + 수평 스크롤(최후 수단)

## 5. 구성 요소 설계

### 5.1 Modules (Gradle)
- app
- core: 파일 IO, 설정, 공통 유틸
- renderer: markdown->html, mermaid preprocess, theme
- feature-viewer: Viewer 화면
- feature-home: 최근 문서/파일 열기

(초기에는 단일 모듈로 시작해도 됨)

### 5.2 주요 클래스(예시)
- DocumentRepository
  - openDocument(uri): String
  - persistRecent(uri, title, lastOpenedAt)
- MarkdownPreprocessor
  - extractMermaidBlocks(md): List<MermaidBlock>
  - applyDirectionPolicy(block, viewport): MermaidBlock
- HtmlRenderer
  - render(md, theme, blocks): String (최종 HTML)
- ViewerViewModel
  - state: Loading/Ready/Error
  - intents: OpenUri, ToggleTheme, Search, OpenDiagramFocus
- WebViewBridge
  - onDiagramClicked(id)
  - onMermaidError(msg)

## 6. 데이터 모델
- RecentDocument
  - id, uriString, title, lastOpenedAt
- Settings
  - themeMode (system/light/dark)
  - fontScale
  - mermaidAutoDirection (bool)
  - linkOpenMode (external/inapp)

## 7. 보안/프라이버시
- 기본 정책: 문서 내용 외부 전송 없음
- WebView:
  - JS는 local asset만 로드(가능하면 네트워크 차단)
  - file 접근/권한 최소화
  - 필요 시 `setAllowFileAccess(false)` 등 하드닝 검토

## 8. 성능 전략
- 문서 hash 기반 캐시
  - (mdHash + theme + viewportBucket) => renderedHtml
- Mermaid 렌더 결과 캐시
  - 블록별 SVG 캐시(가능하면) 또는 전체 HTML 캐시로 단순화
- 대형 문서:
  - 초기 로딩 시 스켈레톤 표시
  - 렌더 완료까지 Progress UI

## 9. 테스트 전략
- Unit
  - Mermaid 블록 추출/치환
  - 방향 변환 휴리스틱
  - theme config 생성
- Instrumentation
  - SAF 파일 오픈 플로우
  - WebView 렌더 및 테마 전환 안정성
- 샘플 문서 세트(회귀)
  - LR/TB 혼합, subgraph 포함, 시퀀스 다이어그램, gantt 등

## 10. Mermaid 다크테마 예시 정책
- light: theme="default" 또는 "neutral"
- dark: theme="dark"
- 커스텀 themeVariables는 v1.2 이후(색/폰트/배경 일괄)

## 11. Mermaid 다이어그램 확대 보기(Focus View)
- 본문: 클릭 시 특정 다이어그램의 SVG/HTML만 분리해 전체 화면 표시
- 기능:
  - pinch zoom
  - pan
  - 저장/공유(후순위)
