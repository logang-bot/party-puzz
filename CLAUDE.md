# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**PartyPuzz** is an Android party game app built with Kotlin and Jetpack Compose. Package: `com.restrusher.partypuzz`.

## Build Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew assembleRelease         # Build release APK
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests (requires device/emulator)
./gradlew lint                    # Run lint checks
./gradlew clean                   # Clean build outputs
```

Run a single test class:
```bash
./gradlew test --tests "com.restrusher.partypuzz.ExampleUnitTest"
```

## Architecture

**Pattern**: MVVM + Repository + Proxy, single-module app.

**Layer breakdown**:
- `ui/views/<feature>/` — Composable screens + ViewModel + State data class per feature
- `data/repositories/` — Repository interfaces and implementations
- `data/local/` — Room database, DAOs, entities, and local data sources
- `data/local/proxies/` — `PlayerLocalProxy` implements `PlayerProxy` for DB access
- `di/` — Hilt modules wiring the layers together
- `navigation/` — Compose Navigation with type-safe serializable route objects

**State flow**: ViewModels expose `StateFlow<XxxState>` consumed by Composables via `collectAsStateWithLifecycle()`.

## Key Technologies

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.51.1 (`@HiltAndroidApp` on `PartyPuzzApplication`) |
| Navigation | Navigation Compose 2.8.1 with serializable routes |
| Database | Room 2.6.1 (KSP for code gen, destructive migration fallback) |
| Image loading | Coil 3.0.4 |
| Serialization | Kotlinx Serialization JSON 1.6.3 |
| Build | AGP 8.5.0, Kotlin 2.0.0, KSP 2.0.0-1.0.24 |

Versions are managed in `gradle/libs.versions.toml`.

## DI Structure

Three Hilt modules in `di/`:
- `DatabaseModule` — provides `PartyPuzzDatabase` and `PlayerDao`
- `ProxyModule` — binds `PlayerLocalProxy` → `PlayerProxy` with `@DatabaseProxy` qualifier
- `RepositoryModule` — provides `PlayerRepositoryImpl` as `PlayerRepository`

A `@DatabaseProxy` qualifier annotation distinguishes local vs future remote proxy bindings (remote module is commented out as a TODO).

## Navigation

Routes are defined as `@Serializable` objects/data classes in `HomeScreenRoutes.kt`:
- `HomeScreen`
- `GameConfigScreen(gameModeName, gameModeImage)`
- `CreatePlayerScreen`

Navigation is wired in `HomeNavigation.kt`. The `AppBar` shows a logo on `HomeScreen` and a title string on other screens.

## Known TODOs in Codebase

- Remote proxy module for API integration (commented in `ProxyModule`)
- Edit and delete player in `PlayerRepository`
- `GamePlayersList` (in-memory list in `appDataSource/`) is a temporary workaround before full persistence

## SDK Targets

- Min SDK: 24 | Target SDK: 34 | Compile SDK: 35
- Java target: 1.8
