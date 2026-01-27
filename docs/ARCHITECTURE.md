# Architecture — v1.1 (Review Applied)
Date: 2026-01-27

## Structural Changes
- Clean Architecture 도입

Modules:
- :app
- :core
- :domain (UseCases, Repository Interfaces)
- :data (Room, DataStore)
- :renderer
- :feature:viewer
- :feature:home
- :feature:settings

## Caching
- LruCache (3 docs) + Room cache (20 docs, 7d TTL)
- viewportBucket:
  - PORTRAIT_NARROW
  - PORTRAIT_WIDE
  - LANDSCAPE

## WebView Lifecycle
- remember(documentUri)
- onDispose → webView.destroy()
- scrollY save/restore
