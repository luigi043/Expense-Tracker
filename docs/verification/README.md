# Expense Tracker verification

## Purpose and scope

This is a local personal expense tracker for Android. Its code supports transactions, category budgets, recurring templates, reports and an optional exchange-rate converter. There is no bank connection, team account, invoice system or evidence of production deployment. The existing Kotlin/Compose/Room/Hilt stack has been preserved.

The active Android code is in `app/src/main`. The root-level Kotlin copies are historical reference files and are not compiled. The browser companion in `docs/demo` is a separate JavaScript simulation, clearly labeled on the public page.

## Baseline and toolchain

- Baseline source: `0a1c8d85580a49b5149eda03d209c227715c5b52`.
- Clean working tree at the beginning of this audit; existing user work was preserved.
- Gradle 8.13, Android Gradle Plugin 8.7.0, Kotlin 2.0.0, compile/target SDK 35, minimum SDK 26.
- Android Studio's current bundled JDK 25 failed before compilation with `25.0.3`. The existing JDK 17 built the project successfully. No SDK or JDK installation was added.
- Native testing targets only the existing emulator. The test script requires an `emulator-*` serial and verifies `ro.kernel.qemu=1` before installing or clearing this app. The connected Samsung was not used.
- Tracked Gradle locks, IDE state and `local.properties` were removed from Git while preserving local files.

## Fixes

| Problem | Result |
| --- | --- |
| Infinite or overflowed amounts could reach persistence | Shared finite-positive validation across transactions, budgets and recurring templates |
| Edit validation differed from create validation | Consistent note limits; original timestamps and recurring links retained |
| Old converter responses could replace current input results | Response-version guard and removal of stale results on invalid input |
| Conversion to the cached base currency failed | Base currency uses a rate of 1; invalid rates are rejected |
| Missing converter configuration produced a vague failure | Explicit missing-key message; same-currency arithmetic remains offline |
| Different currencies were added and relabeled | Currency-scoped dashboard and budget spending; original row currencies; separate history/PDF totals |
| Report opening used blocked file URIs | Private export directory and scoped FileProvider content URIs; missing viewer handling |
| CSV formatting depended on the device locale | Explicit decimal formatting and formula-prefix neutralization for notes |
| Delete controls confirmed actions with empty callbacks | Deletion remains in the working edit screen; empty row actions removed |
| Save failures were discarded | Budget/recurring errors surface without closing the entry dialog |
| Recurring worker was never scheduled | Existing unique daily WorkManager request is enqueued at startup |
| Recurring deletion was immediate | Confirmation explains that existing transactions remain |
| Fixed-day recurrence labels implied calendar months | Explicit 30/90/365-day labels |
| Long currency lists could be clipped | Scrollable dialogs |
| Some dates and amounts inherited non-English locale formatting | Explicit English formatting with a Portuguese-locale regression case |
| Date-picker resources could follow another device language | English activity resource context verified from Portuguese and Turkish contexts |
| Dark theme retained dark system-bar icons | System-bar appearance follows the app theme, with a native assertion and refreshed screenshot |
| Different currencies shared a visible symbol | ISO codes are shown alongside transaction and recurring amounts |

## Screens and dependencies

| Route | Screen | Entry |
| --- | --- | --- |
| `dashboard` | Monthly overview and charts | App launch / Home |
| `add_expense` | Add transaction | Home floating action |
| `edit_expense/{expenseId}` | Edit/delete transaction | Tap a transaction |
| `history` | Search, filters and exports | History navigation |
| `budget` | Budgets and category-limit dialog | Budget navigation |
| `recurring` | Recurring schedules and entry dialog | Settings tool |
| `converter` | Currency calculator and currency dialogs | Settings tool |
| `settings` | Appearance, currency and tools | Settings navigation |

Only different-currency conversion needs an external service: exchangeratesapi.io, a configured key and network access. This repository retains a placeholder key. No real key, financial account or private app records were used. Report opening depends on a compatible external viewer; report creation and content URI readability are tested independently. No camera, RFID, Bluetooth, location or other hardware is required by the app.

## Reproduce

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --no-daemon --max-workers=2
./scripts/verify-native.ps1 -Serial emulator-5554 -ResetSyntheticData
```

`-SkipBuild` is appropriate only after the exact current source has passed its build. Native captures live in `native/`; their actions are mapped in [SCREEN-INDEX.md](SCREEN-INDEX.md). Raw native test output and JVM XML results are under `evidence/`.

The public browser test is `node scripts/check-demo.cjs`. It accepts `PLAYWRIGHT_MODULE`, `CHROMIUM_PATH`, `DEMO_URL` and `SCREENSHOT_DIR` to reuse installed browser tooling and select the local or public page. [Public publication details](PUBLIC-DEMO.md) distinguish browser evidence from native evidence.

`node scripts/package-demo.cjs` packages the browser companion, native gallery and previously verified APK into the separate hosting checkout. `node scripts/check-artifacts.cjs` checks all public native images, the phone-width gallery and the streamed public APK checksum against the GitHub release digest. The repository stays private; the separate public artifacts need no repository access.

## Known limits

- No live exchange API, bank/account integration or production release has been verified.
- Recurring processing currently creates one entry for a due template, then advances from processing day. It does not replay every missed interval. Separate insertion/schedule writes do not establish crash-safe exactly-once processing. WorkManager timing is controlled by Android.
- Budget limits are stored as numeric category/month settings. Changing the base currency reinterprets the existing limits rather than converting them. Spending is filtered by the selected currency.
- Amounts remain `Double`; decimal accounting precision has not been migrated.
- Room retains destructive migration fallback. Release signing, backup/privacy policy, migration coverage and release/minification checks remain outside this debug evaluation.
- The app resource locale and its date-picker controls are English. External report viewer apps retain their own language settings.

Actual final test counts, APK checksum and Git commits are recorded in [RESULTS.md](RESULTS.md). An incomplete test is never counted as passing.
