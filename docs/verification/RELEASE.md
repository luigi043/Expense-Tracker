# Expense Tracker Android evaluation build

Install the attached debug APK on Android 8.0 (API 26) or later. This is a debug-signed evaluation build, not a Play Store release. It contains no exchange API key or financial-account integration.

The update fixes finite-amount validation, currency-scoped totals, stale converter results, private CSV/PDF sharing, recurring worker scheduling, save-error feedback, deletion behavior and English formatting. The existing Kotlin/Compose/Room architecture is retained.

[Interactive browser companion](https://expense-tracker-demo.hummingmole.chatgpt.site) · [Source and screenshots](https://github.com/luigi043/Expense-Tracker/tree/expense-tracker-verification/docs/verification)

The browser companion uses synthetic records in tab memory. It does not execute Android storage or background jobs. Its exchange rates are fixed examples, not current quotes.

Build verification uses JDK 17 and Android SDK 35. The six JVM regression tests cover invalid monetary inputs, separate currency totals, CSV formula safety and English formatting under a Portuguese locale. All four native instrumentation tests passed, including the full UI walkthrough, report integration, recurring processing and English resource contexts. The walkthrough captured 38 real native screen states.

Known limits: live different-currency conversion needs separately configured API access; WorkManager timing is controlled by Android; recurring insertion and schedule advancement are not crash-atomic; existing budget limits are numeric values reinterpreted when base currency changes; amounts use Double; Room retains destructive migration fallback. No bank connection, real private data, physical phone, production signing or release/minification verification was used.

APK SHA-256: `52DBF907F8BFD175723402E027D2D79CBDDF46DC80D76F9EA64F1CBE8855A08E`.
