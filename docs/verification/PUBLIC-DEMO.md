# Public interactive companion

Try it: https://expense-tracker-demo.hummingmole.chatgpt.site

The public page uses synthetic data held in tab memory. It demonstrates transaction creation, editing, deletion, search, CSV export, category budgets, recurring schedule pause/resume, currency isolation and display settings. Data resets on reload or Reset sample data. Fixed example exchange rates are explicitly labeled and are not market quotes.

The native Kotlin application is separate. The public page does not execute Android code, Room persistence, WorkManager, live exchange-rate calls or financial-account integration.

Automated Chromium checks passed against both the local files and the deployed URL, including every navigation screen at a 390px viewport and zero browser page errors. Browser screenshots are under `browser/` for local verification and `public/` for the deployed version. They are not native screenshots.

## Publication provenance

- Hosting project: `appgprj_6aebac7edfe48191bfeeca8d66272236`
- Initial source commit: `801f1d86ce63b1f4d74cb51a5207eba640fe3f59`
- Initial saved version: `appgprj_6aebac7edfe48191bfeeca8d66272236~appgver_ed542126c14c8191b95aae34b9333fc4`
- Initial deployment: `appgdep_6aebaff3d6f88191803c04a34d0087ed`, succeeded
- Audience: public, anyone with the URL

Version 2 updates source links to the renamed `expense-tracker-verification` GitHub branch. Its source commit is `685610a67d4ed29133f5ece3110b9d8f56c9cf8b`; saved version is `appgprj_6aebac7edfe48191bfeeca8d66272236~appgver_8444b92c1d7c81919fa119ad656b05b5`. Interactive behavior is unchanged.

The hosting checkout contains only static demo files and its hosting manifest. The Android repository tracks the same demo source under `docs/demo`. No credentials, private app records or signing keys are included.

Version 2 deployment: `appgdep_6aebd89b6ae08191ae3c514e77e9a22b`, succeeded. Published HTML and JavaScript were checked after the rename: HTTP 200, current branch links present, old branch links absent.

## Public APK and native gallery

Version 3 adds the verified debug APK and 38 real native screenshot assets. The source repository remains private; source and detailed GitHub evidence links are labeled accordingly.

- Public APK: https://expense-tracker-demo.hummingmole.chatgpt.site/expense-tracker-debug.apk
- Native gallery: https://expense-tracker-demo.hummingmole.chatgpt.site/native.html
- Hosting source: `a1c2309b8d057a64db5a7c0d953896c279f2f1d0`
- Saved version: `appgprj_6aebac7edfe48191bfeeca8d66272236~appgver_fa244d9d721081919df223a392d8d3c4`
- Deployment: `appgdep_6aebdfd1c49c81919c8bce88f92a3d33`, succeeded
- Public APK size: 24,237,392 bytes
- SHA-256: `52DBF907F8BFD175723402E027D2D79CBDDF46DC80D76F9EA64F1CBE8855A08E`

The final deployed companion passed its browser workflow checks again. The gallery passed phone-width layout and browser-error checks; every native PNG returned HTTP 200 with the expected image type. Streaming the public APK produced the exact GitHub release digest. The checks are reproducible through `scripts/package-demo.cjs`, `scripts/check-demo.cjs` and `scripts/check-artifacts.cjs`.
