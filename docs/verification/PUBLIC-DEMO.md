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
