# WalletCase — Android Developer Case Study

## 1. Overview

**WalletCase** is a Digital Wallet Dashboard for Android, built with **Jetpack Compose**, the **MVVM** pattern, and a **multi-module** architecture. It is a case study implementation that demonstrates clean architecture principles in practice — most notably a data layer that is decoupled from the UI at **compile time**, so the mock data source can be replaced with a real network service without touching the View or the ViewModel.

## 2. Build & Run

> Requires **Android Studio Narwhal (2025.1.1)** or newer (AGP 9.1.1).

1. Clone the repository and open the project root in Android Studio.
2. Let Gradle sync complete.
3. Run with `./gradlew assembleDebug` or press **Run** in Android Studio.

The app launches directly to the wallet dashboard.

## 3. Architecture

The project is split into **6 modules**, each with a single, well-defined responsibility:

- **`:domain`** — Pure Kotlin/JVM, **zero Android dependencies**. Holds the domain models, the `WalletRepository` interface, and the `DemoScenarioController` interface. This is the **compile-time decoupling seam** every other layer agrees on.
- **`:data`** — Implements the `:domain` interfaces. `MockWalletDataSource` reads bundled JSON assets; DTOs are mapped to domain models; a Hilt DI module (`DataModule`) binds the implementations.
- **`:core:designsystem`** — The fintech **Material 3** theme, `WalletIcons`, a hand-rolled `Modifier.shimmer()`, and the `SectionHeader` component.
- **`:core:util`** — `CurrencyFormatter` (`₺1,250.75`, `tr-TR` locale) and `DateFormatter` (`Mar 28`, `Locale.ENGLISH`).
- **`:feature:wallet`** — **MVVM** UI: a stateless `WalletDashboardScreen`, a `WalletDashboardRoute` (state collection), and a `WalletDashboardViewModel`. Depends only on `:domain` and `:core:*` — **never on `:data`**.
- **`:app`** — DI assembly, the `NavHost`, the bottom `NavigationBar`, and `MainActivity`.

### Dependency graph

```
:domain                      ← (no deps)
:data                        → :domain
:core:designsystem           → (no project deps)
:core:util                   → (no project deps)
:feature:wallet              → :domain, :core:designsystem, :core:util
:app                         → all modules
```

### Why multi-module?

The brief asks for a data layer "decoupled so the mock can be replaced with a real network service without touching the View/ViewModel." In this implementation, that guarantee is enforced at **compile time**: `:feature:wallet` **cannot** reference `:data` — the dependency edge simply doesn't exist in the build graph. Swapping `MockWalletDataSource` for a `RetrofitWalletDataSource` means editing one Hilt `@Binds` line in `:data:DataModule` — zero changes to the ViewModel, the Screen, or the domain.

## 4. UI States

The screen renders one of four states, modelled as a `sealed interface WalletDashboardUiState`:

- **Loading** — a shimmer skeleton animation (hand-rolled `Modifier.shimmer()`, no extra dependency). The skeleton layout mirrors the Success layout to prevent a visual jump when data arrives.
- **Success** — a balance card (`BigDecimal`, `₺`-formatted), a horizontal children row, and a recent-transactions list with signed amounts and `Mar 28`-style dates.
- **Empty** — a **distinct** state for a brand-new wallet (`₺0.00`, no children, no transactions). It is not a special-case of Success.
- **Error** — an error message plus a **Retry** button that re-triggers the load.

The demo scenario menu (the `⋮` overflow in the top bar) lets a reviewer switch between states live, without rebuilding.

## 5. Configuration Change & Process Death Survival

StateFlow was chosen over LiveData because it is lifecycle-independent, works seamlessly in a multi-module setup without pulling an Android dependency into the ViewModel, and integrates naturally with coroutines. MutableState was ruled out because it ties state to the Compose runtime, making the ViewModel harder to unit-test.

- **Configuration changes** (rotation): the ViewModel is retained by the Jetpack lifecycle; the `StateFlow` uses `WhileSubscribed(5_000)`, which keeps the last emitted state alive for 5 seconds across recompositions and the brief unsubscribe/resubscribe window of a rotation.
- **Process death** (bonus): the selected demo scenario is stored in `SavedStateHandle` as an **`Int` ordinal** — the only Bundle-safe way to persist an enum. On a cold restart after process death, the ViewModel restores the scenario from `SavedStateHandle` before triggering `load()`, so the screen returns to the same state the user had selected.
- Transaction data is intentionally **re-fetched** on cold start — the correct fintech behaviour (never persist stale balances across process death).

## 6. Swapping Mock for Real Network

To replace the mock with a real API:

1. Add a `RetrofitWalletDataSource : WalletDataSource` in `:data`.
2. Change one `@Binds` line in `DataModule`: `bindWalletDataSource(impl: RetrofitWalletDataSource)`.
3. Delete `MockWalletDataSource`, `DemoScenarioStore`, and `ScenarioMenu` (all marked `// Demo-only`).
4. **No changes** to `:feature:wallet`, `:domain`, or any other module.

## 7. Testing

- **Unit tests**: `CurrencyFormatterTest`, `DateFormatterTest`, `DefaultWalletRepositoryTest`, and `WalletDashboardViewModelTest` (6 state-transition cases with **Turbine**).
- **Integration test**: `DataLayerIntegrationTest` — exercises the full `MockWalletDataSource → DefaultWalletRepository → WalletDashboard` pipeline against the **real bundled assets**, with no fakes.
- **Compose UI tests**: `WalletDashboardScreenTest` — 5 cases asserting that each state renders correctly and that the Retry callback fires.

Run all tests:

```bash
./gradlew testDebugUnitTest                          # unit tests
./gradlew :data:connectedDebugAndroidTest            # integration test (needs emulator)
./gradlew :feature:wallet:connectedDebugAndroidTest  # UI tests (needs emulator)
```

## 8. Tech Stack

Kotlin 2.2.10 · Jetpack Compose · Material 3 · Hilt 2.59.2 · Navigation Compose (type-safe routes) · kotlinx.serialization · StateFlow · SavedStateHandle · core library desugaring (`java.time` on minSdk 24) · Turbine · AGP 9.1.1

## 9. Trade-offs & Given More Time

### Trade-offs made

**Mock data layer over Retrofit.**
The brief asks for a data layer that is *swappable* rather than one backed by a real API. The mock is intentionally the simplest implementation that proves the seam: a single JSON file, a `DefaultWalletRepository`, and a Hilt `@Binds`. Keeping it this thin makes the swap path (Section 6) concrete and easy to verify.

**No offline caching.**
Room was left out intentionally — there is no real network layer yet, so there is nothing to cache. The correct sequence is: replace the mock with Retrofit first, then introduce Room as a local cache on top of it. Adding Room against a mock data source would have been premature and would have obscured the decoupling story the spec is actually testing.

**Demo scenario menu instead of a settings screen.**
The `⋮` menu lets a reviewer switch between Loading / Success / Empty / Error without rebuilding. In a real app this would not exist; it is clearly annotated `// Demo-only` throughout so every piece is easy to delete (Section 6 lists exactly what to remove).

**Single top-level NavGraph, no deep-link support.**
The app has one destination today. Navigation is wired with type-safe routes so it can be extended, but there is no deep-link manifest entry or `Intent` filter because there is no real URL scheme to register.

### Given more time

- **Replace mock with Retrofit + OkHttp, then add a Room offline cache**: I would replace the mock with a `NetworkDataModule` (auth header interceptor, zero ViewModel/Screen changes), and then introduce a Room database as a local cache so the dashboard would remain usable without a network connection.
- **UI/UX design alignment**: I would use Claude Design to analyze the existing screens and design language on your platform, have it generate a design template that fits the product's visual identity, and then implement that design in the app. In this submission I intentionally kept the UI simple and focused my effort on the technical and architectural aspects of the challenge.
