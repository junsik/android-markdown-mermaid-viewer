# Markdown Mermaid Viewer

Markdown 문서와 Mermaid 다이어그램을 렌더링하는 Android 뷰어 앱입니다.

## 주요 기능

- **Markdown 렌더링**: Flexmark-java 기반 HTML 변환 + WebView 렌더링
- **Mermaid 다이어그램**: `mermaid.min.js`를 활용한 다이어그램 렌더링 (graph, sequence, flowchart 등)
- **다이어그램 전체화면**: 다이어그램 탭하여 확대/축소 가능
- **파일 열기**: SAF(Storage Access Framework) 파일 선택기 또는 외부 앱에서 `.md` 파일 열기
- **최근 문서 관리**: 열람 이력 자동 저장, 스와이프 삭제 지원
- **다크 모드**: 앱 전체 및 WebView 다크 테마 지원 (설정 영속 저장)
- **링크 처리**: HTTP 링크는 외부 브라우저, 상대 경로 `.md` 링크는 앱 내 뷰어로 열기

## 프로젝트 구조

멀티 모듈 아키텍처로 구성되어 있습니다:

```
:app                  - 메인 앱 모듈 (Navigation, DI, Activity)
:renderer             - Markdown → HTML 변환 (Flexmark-java)
:core                 - 공통 유틸리티 (Dispatchers, Theme)
:domain               - 도메인 레이어 (Model, Repository Interface)
:data                 - 데이터 레이어 (Repository 구현, SharedPreferences)
:feature:home         - 홈 화면 (최근 문서 목록)
:feature:viewer       - 뷰어 화면 (WebView + Mermaid)
:feature:settings     - 설정 화면 (다크 모드, 앱 정보)
```

## 기술 스택

- **UI**: Jetpack Compose + Material3
- **DI**: Hilt
- **Rendering**: WebView + Flexmark-java + mermaid.min.js
- **Storage**: SharedPreferences (JSON)
- **Min SDK**: 26 (Android 8.0)

## 빌드 및 실행

1. Android Studio에서 프로젝트를 엽니다
2. Gradle Sync를 수행합니다
3. `app` 모듈을 선택하고 에뮬레이터 또는 실제 디바이스에서 실행합니다

## 라이선스

Copyright 2026 junsik. All rights reserved.
