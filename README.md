# Bookshelf Mobile

A cross-platform bookshelf management application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**.

## Features

- **Book Management**: Add, edit, and organize your personal library.
- **Barcode Scanning**: Quickly register books by scanning ISBN barcodes (supports single and bulk scanning).
- **Search**: Search for books manually or view results from external APIs.
- **Offline Support**: Local database integration for managing your collection offline.
- **Cross-Platform**: Shared logic and UI between Android and iOS.

## Tech Stack

- **UI**: Compose Multiplatform
- **Database**: Room (KMP)
- **Networking**: Ktor
- **Dependency Injection**: Koin
- **Serialization**: kotlinx.serialization (JSON/XML)
- **Image Loading**: Coil
- **Logging**: Napier
- **Utilities**: kotlinx-datetime, FileKit, EasyQRScan

## Project Structure

- `:shared`: Contains the core logic and Compose Multiplatform UI shared between platforms.
- `:androidapp`: Android-specific entry point and configuration.
- `iosApp`: iOS-specific Xcode project.

## Getting Started

1. Open the project in **Android Studio** (with the KMP plugin) or **IntelliJ IDEA**.
2. To run on Android: Select the `androidapp` configuration and run.
3. To run on iOS: Open `iosApp/iosApp.xcworkspace` in Xcode or use the `iosApp` run configuration in Android Studio.
