# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Run tests
./gradlew test                    # unit tests
./gradlew connectedAndroidTest    # instrumented tests (requires device/emulator)

# Run a single test class
./gradlew test --tests "com.expensetracker.SomeTest"

# Lint
./gradlew lint

# Clean
./gradlew clean
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Architecture

MVVM Clean Architecture in a single `:app` module. The dependency flow is:

```
UI (Screens + ViewModels)
    ↓
Domain (UseCases → Repository interfaces → Models)
    ↓
Data (Repository impls → Room DAOs / Retrofit / DataStore)
```

**Domain layer** (`domain/`) is pure Kotlin — no Android imports.

- `domain/model/Models.kt` — all domain models in one file (`Expense`, `Budget`, `RecurringExpense`, `DashboardSummary`, `ExchangeRate`, `TransactionType`, `ExpenseCategory`)
- `domain/repository/Repositories.kt` — repository interfaces
- `domain/usecase/UseCases.kt` — all use cases in one file; each use case is a class with `invoke` as the entry point

**Data layer** (`data/`) provides two sub-trees:

- `data/local/database/` — Room: `AppDatabase` (version 1, `fallbackToDestructiveMigration`), three entities (`ExpenseEntity`, `BudgetEntity`, `RecurringExpenseEntity`), all DAOs in `Daos.kt`, all entity→domain mappers in `Mappers.kt`
- `data/local/datastore/AppPreferences.kt` — DataStore keys: `dark_mode`, `base_currency`, `onboarding_done`
- `data/local/repository/RepositoryImpls.kt` — local repository implementations
- `data/remote/api/ExchangeRateApi.kt` — Retrofit interface for exchangeratesapi.io
- `data/remote/repository/ExchangeRateRepositoryImpl.kt` — remote repository

**DI** (`di/Modules.kt`) — three Hilt modules all in one file: `DatabaseModule`, `NetworkModule`, `RepositoryModule`. All scoped to `SingletonComponent`.

**UI layer** (`ui/`):

- Each screen lives in its own package under `ui/screens/<feature>/` with a `*Screen.kt` (Composable) and `*ViewModel.kt`
- Screens: `dashboard`, `addexpense`, `history`, `budget`, `recurring`, `converter`, `settings`
- `ui/navigation/Navigation.kt` — `Screen` sealed class + `AppNavigation()` composable; `NavHost` is the single entry point with slide+fade transitions
- `ui/components/Components.kt` — shared composables
- `ui/theme/Theme.kt` — Material 3 dynamic theming

**Background work** (`worker/RecurringExpenseWorker.kt`) — `@HiltWorker` that runs daily via `WorkManager` to auto-create expenses from active recurring templates. Enqueued at app startup via `RecurringExpenseWorker.enqueue(workManager)`.

**Utilities** (`utils/`):

- `Formatters.kt` — date/currency formatting extension functions; also defines `SUPPORTED_CURRENCIES` and `RECURRING_INTERVALS` constants
- `ExportManager.kt` — PDF (iTextPDF) and CSV (OpenCSV) export

## Key Configuration

**Exchange rate API key** is set in `app/build.gradle.kts` as a `buildConfigField`:
```kotlin
buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"YOUR_API_KEY_HERE\"")
```
Replace with a real key from exchangeratesapi.io for the converter feature to work.

**Database** uses `fallbackToDestructiveMigration()` — incrementing the version in `AppDatabase` drops all data. Write proper migrations before shipping.

**Min SDK 26** — `java.time.*` APIs are used directly (no desugaring needed at API 26+).

## Tech Stack Summary

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.51 |
| Database | Room 2.6 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Background | WorkManager 2.9 |
| Preferences | DataStore 1.1 |
| Charts | MPAndroidChart 3.1 (via JitPack) |
| Export | iTextPDF 5.5 + OpenCSV 5.9 |
| Serialization | kotlinx.serialization |
