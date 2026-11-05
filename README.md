# Expense Tracker

A native Android personal expense tracker built with Kotlin and Jetpack Compose. It stores transactions, category budgets and recurring templates locally with Room. This is a portfolio project, not a bank-connected accounting service.

[Try the interactive browser demo](https://expense-tracker-demo.hummingmole.chatgpt.site) · [Verification](docs/verification/README.md) · [Screen index](docs/verification/SCREEN-INDEX.md)

[Download the Android debug APK](https://expense-tracker-demo.hummingmole.chatgpt.site/expense-tracker-debug.apk) · [View 38 native screenshots](https://expense-tracker-demo.hummingmole.chatgpt.site/native.html)

The demo, APK and gallery are public. This GitHub repository retains its private visibility; source and repository evidence links require access.

The browser companion uses synthetic records and JavaScript. It does not execute Room, Android background work or live exchange-rate requests. Its data resets on reload.

## 🚀 Features

- **Full Expense Management**: Add, edit, and delete expenses with fields for amount, category, date, note, and type (income/expense).
- **Dashboard & Analytics**:
  - Total monthly spending, income vs expenses, and remaining balance.
  - Visual spending breakdown using Pie Charts.
  - Daily spending trends using Bar Charts.
- **Budgeting System**: Set monthly budgets per category and track remaining limits in real-time.
- **Recurring Expenses**: Automatic logging of regular expenses (rent, subscriptions) using WorkManager.
- **Transaction History**: Advanced filtering by date, category, and transaction type.
- **Currency Converter**: Real-time exchange rates integration via external API.
- **Data Export**: Export your financial data as CSV or PDF reports.
- **Local Storage**: Fully offline-first approach using Room Database.
- **Modern UI**: Clean, responsive Jetpack Compose interface with Dark Mode support.

## 🏗 Architecture & Tech Stack

The app follows **Clean Architecture** principles and the **MVVM** (Model-View-ViewModel) pattern to ensure modularity, testability, and scalability.

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Asynchronous Flow**: Coroutines & StateFlow
- **Dependency Injection**: Hilt (Dagger)
- **Database**: Room
- **Networking**: Retrofit & OkHttp
- **Background Tasks**: WorkManager
- **Preferences**: DataStore
- **Charts**: MPAndroidChart
- **PDF/CSV Generation**: iText & OpenCSV

## 📂 Project Structure

- `data/`: Room entities, DAOs, Repositories (Impl), and API definitions.
- `domain/`: Pure Kotlin models, Repository interfaces, and Use Cases for business logic.
- `ui/`: Compose screens, ViewModels, and reusable components.
- `di/`: Hilt modules for Dependency Injection.
- `utils/`: Helper functions, formatters, and export managers.
- `worker/`: Background jobs for recurring transactions.

## 🛠 Setup

1. Clone the repository.
2. Open the project in **Android Studio (Ladybug or later)**.
3. Select JDK 17 for Gradle and install Android SDK platform 35. JDK 25 is incompatible with the current Gradle/Kotlin configuration. Minimum Android version is API 26.
4. The converter uses exchangeratesapi.io. The build contains a placeholder key; different-currency conversion requires your own configuration and network access. Never commit a real key. Same-currency calculations work offline.
5. Sync Gradle and run the app. The active source is under `app/src/main/java/com/expensetracker`; root-level Kotlin files are historical reference copies and are not compiled.

## Verification and limitations

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --no-daemon --max-workers=2
./scripts/verify-native.ps1 -Serial emulator-5554 -ResetSyntheticData
```

The script refuses physical-device serials and clears only this app on the selected synthetic-data emulator. The installable debug APK is `app/build/outputs/apk/debug/app-debug.apk`; debug signing is for evaluation.

Dashboard totals and charts include only the selected currency, with no implicit conversion. History and PDF totals keep currencies separate. Budget spending also uses the selected currency. Existing numeric budget limits follow the selected base currency and are not converted when it changes.

Recurring intervals are fixed days, not calendar months. WorkManager controls execution time. The current processor creates one transaction for a due template and advances from processing day; it does not replay every missed interval. Transaction insertion and schedule advancement remain separate writes, so crash-safe exactly-once processing is not established.

Room retains the existing destructive migration fallback. Review migrations, monetary precision, backup/privacy policy and release signing before real financial use. No bank/account integration or production readiness has been verified. Native report opening requires a compatible viewer app.

The browser checks in `scripts/check-demo.cjs` require Playwright and Chromium. `PLAYWRIGHT_MODULE` and `CHROMIUM_PATH` can select an existing local installation. Set `DEMO_URL` to test the deployed URL; otherwise the test opens `docs/demo/index.html`. See [public demo provenance](docs/verification/PUBLIC-DEMO.md).

## 📝 License

This project is for educational and portfolio purposes.
