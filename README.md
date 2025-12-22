# Notes

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/meekieD/Notes?logo=github)](https://github.com/meekieD/Notes/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API_24%2B-blue.svg?logo=android)](https://developer.android.com/studio/releases/platforms)


A modern Android notes application built with Kotlin and Jetpack Compose.
This is a personal learning project to explore modern Android development practices, including Clean Architecture, MVVM, and Room database.

## Features

- Create, edit, and delete notes with text and images
- Search through existing notes by content
- Pin important notes for quick access
- Organize notes with automatic timestamps
- Clean and intuitive Material Design UI
- Offline-first with local Room database storage

## Tech Stack

- **Programming Language**: Kotlin
- **Architecture Pattern**: Clean Architecture with MVVM
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Async Programming**: Kotlin Coroutines and Flow
- **Build Tool**: Gradle with Kotlin DSL
- **Minimum Android SDK**: API 24 (Android 7.0)

## Architecture

The app follows Clean Architecture principles with three main layers:

- **Presentation Layer**: Jetpack Compose UI components with ViewModels implementing MVVM pattern
- **Domain Layer**: Business logic with Use Cases and Repository interfaces
- **Data Layer**: Room database implementation with Repository concrete classes

```
app/
├── data/          # Data layer (Room, DAO, Repository impl)
├── domain/        # Domain layer (Use Cases, Repository interfaces)
└── presentation/  # Presentation layer (Compose UI, ViewModels)
```

## Screenshots

<p float="left">
  <img src="/screenshots/photo_1.jpg" width="250" />
  <img src="/screenshots/photo_2.jpg" width="250" />
  <img src="/screenshots/photo_3.jpg" width="250" />
</p>

<p float="left">
  <img src="/screenshots/photo_4.jpg" width="250" />
  <img src="/screenshots/photo_5.jpg" width="250" />
  <img src="/screenshots/photo_6.jpg" width="250" />
</p>

## Setup & Installation

### Prerequisites

- Android Studio (Giraffe or later)
- JDK 11 or higher
- Android SDK API 24+

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/meekieD/Notes.git
    cd Notes
    ```

2. Open the project in Android Studio and sync Gradle files

3. Run the app on an emulator or connected device

### Build

```bash
./gradlew assembleDebug
```

## Release and Deployment

The project uses GitHub Actions for automated release builds. Releases are triggered by:

- Pushing version tags (e.g., `v1.0.0`)
- Manual workflow dispatch with a specified release tag

The release process includes:

- Building the release APK using Gradle
- Signing the APK with the configured keystore
- Creating a GitHub release with the signed APK and changelog from `CHANGE.md`

## Learning Goals

This project serves as a learning platform for:

- Jetpack Compose for declarative UI development
- Room database for local data persistence
- Clean Architecture for scalable Android apps
- Dependency injection with Hilt
- Kotlin Coroutines for asynchronous programming
- MVVM pattern implementation
- Material Design 3 principles

## Future Improvements

- [ ] Implement note categories/tags
- [ ] Dark mode theme toggle
- [ ] Widget for quick note access
- [ ] Export notes to PDF or other formats
- [ ] Improve test coverage for all layers
