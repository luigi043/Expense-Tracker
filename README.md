# Expense Tracker - Production-Ready Android App

A comprehensive, production-ready Expense Tracker application built with modern Android development practices, Kotlin, and Jetpack Compose.

## 🚀 Features

- **Full Expense Management**: Add, edit, and delete expenses with fields for amount, category, date, note, and type (income/expense).
- **Dashboard & Analytics**:
  - Total monthly spending, income vs expenses, and remaining balance.
  - Visual spending breakdown using Pie Charts.
  - Daily/Weekly spending trends using Bar Charts.
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
3. Replace `YOUR_API_KEY_HERE` in `app/build.gradle.kts` with your key from [ExchangeRate-API](https://exchangeratesapi.io) (optional for full functionality).
4. Sync Gradle and run the app.

## 📝 License

This project is for educational and portfolio purposes.
