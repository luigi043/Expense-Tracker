# Verification results

Verified on 5 November 2026. The native evaluation used the existing API 35 `sdk_gphone64_x86_64` emulator. No physical phone, real financial records or live exchange key was used.

## Final checks

| Check | Actual result |
| --- | --- |
| `:app:assembleDebug` | Passed with existing JDK 17 and SDK 35 |
| `:app:testDebugUnitTest` | 6 tests passed, 0 failures |
| `:app:assembleDebugAndroidTest` | Passed |
| Emulator installation | Debug and test APKs installed successfully |
| Native instrumentation | 4 tests passed, 0 failures; final run 146.523 seconds |
| Native UI walkthrough | Passed; 38 real captures across all 8 destinations and meaningful dialogs/states |
| Native report integration | Passed: readable content URI, stable CSV decimals under German locale, formula-safe notes, separate USD/EUR PDF totals |
| Native recurring processor | Passed against isolated Room: due template processed once, original currency/link retained, next date advanced, immediate second run creates nothing |
| Native English resource context | Passed from Portuguese and Turkish resource contexts |
| Dark-mode system bars | Native assertion passed; refreshed screenshot shows readable light icons |
| Browser companion | Local and deployed Chromium workflows passed, including all mobile navigation screens and zero page errors |
| Renamed public source links | Published HTML/JS return HTTP 200 and contain the current GitHub branch links |
| Public APK | HTTP 200; all 24,237,392 bytes match the GitHub release SHA-256 |
| Public native gallery | All 38 PNGs accessible; phone layout passed with zero browser errors |
| `git diff --check` | Passed |
| Live exchange service | Not run; placeholder key retained |
| External report viewer UI | Not verified; depends on installed applications |
| Android lint and release/minification | Not run; this is a debug evaluation |

The UI walkthrough creates and edits a synthetic expense, verifies USD 45 budget spending, searches/reopens saved history, confirms persistence across activity recreation, deletes the transaction, and creates a USD 1,000 income. It also covers invalid budget/transaction/recurring/converter inputs, date and currency selection, export options, offline same-currency calculation, missing-key feedback, recurring pause/delete confirmation and dark mode.

Raw results are under `evidence/`. [SCREEN-INDEX.md](SCREEN-INDEX.md) maps the actual captures. Browser evidence is kept separate from Android evidence. Earlier harness failures were corrected before this final passing run and are not counted as successful tests.

## APK

- Package: `com.expensetracker`, version `1.0.0` (code 1).
- Minimum Android API: 26; target/compile API: 35.
- App-code source commit: `18d2a73797327b38585ec97430833849118d6bfe`.
- Local artifact: `app/build/outputs/apk/debug/app-debug.apk`.
- Publication copy: `build/verification-artifacts/expense-tracker-debug.apk`.
- SHA-256: `52DBF907F8BFD175723402E027D2D79CBDDF46DC80D76F9EA64F1CBE8855A08E`.

The APK is debug signed and intended for evaluation. Signing keys are not committed.

- [Public APK download](https://expense-tracker-demo.hummingmole.chatgpt.site/expense-tracker-debug.apk)
- [Public native gallery](https://expense-tracker-demo.hummingmole.chatgpt.site/native.html)
- [GitHub prerelease](https://github.com/luigi043/Expense-Tracker/releases/tag/v1.0.0-evaluation-20261105), requires access to the private repository.
- Release target: `0271beb6f99534687d5cbca67e199031a7300762`.
- Asset size: 24,237,392 bytes. The GitHub asset digest and the hash streamed from the public download both match the checksum above.

Publication metadata is in `evidence/release.json` and `evidence/public-artifacts.json`. Repository visibility was preserved. Public publication does not change the tested native app source.

## Logical Git sequence

| Commit | Purpose |
| --- | --- |
| `130eaec` | Remove generated Gradle/IDE state and machine SDK configuration from tracking |
| `3a46f03` | Validate financial input, preserve edit metadata, isolate currency totals and guard converter responses |
| `9f1decd` | Repair report sharing, CSV safety and per-currency PDF/history totals |
| `9920dca` | Repair save feedback, schedule recurring work, confirm deletion and improve screen controls |
| `10180c0` | Add the tested public browser companion and browser evidence |
| `01f0d49` | Keep dates and decimal formatting in English under a Portuguese locale |
| `18d2a73` | Enforce English app resources, disambiguate row currencies and repair dark system bars |
| `0271beb` | Commit the native workflow/integration tests, 38 captures, screen index, reproduction script and evidence |

The final publication commit records the APK/gallery links, artifact checksums and reproducible packaging/check scripts. The branch is `expense-tracker-verification`; commits use `Luiz Fehlberg <cluiz4003@outlook.pt>`. The original main branch has not been merged or overwritten. The GitHub repository is private; the separate public demo and artifacts can be accessed without GitHub repository access.

## Remaining limits

See [verification scope and known limits](README.md). In particular, this result does not establish live-rate service integration, crash-atomic recurring processing, decimal accounting precision, safe schema migration or production-release readiness.
