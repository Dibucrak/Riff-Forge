<div align="center">

<img src="Logo.png" width="160" height="160" alt="RiffForge Logo" />

# RiffForge

**Your all-in-one music companion** — tuner, metronome, chord dictionary, music theory, ear training, and community repertoire.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-brightgreen.svg)](https://kotlinlang.org)
[![KMP](https://img.shields.io/badge/KMP-Multiplatform-blue.svg)](https://kotlinlang.org/lp/multiplatform/)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-orange.svg)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%20%26%20iOS-lightgrey.svg)]()

</div>

---

## 🎸 Features

- **Chromatic Tuner** — Real-time pitch detection (440 Hz) with string selector and multiple tunings (`Standard`, `Drop D`, `DADGAD`, etc.)
- **Smart Metronome** — BPM control, time signature accents, tap tempo, and selectable sound profiles (`Classic`, `Mechanical`, `Studio`)
- **Chord Dictionary** — Interactive chord library with Canvas fretboard diagrams and synthesized audio playback
- **Music Theory & Training** — Circle of Fifths, Fretboard Scale Builder (frets 0–12), Harmonic Progression Generator, Daily Learning, and Ear Training
- **Repertoire & Practice Mode** — Songs and setlists with live chord transposition, active wake-lock practice mode, and American (`C, D, E`) or Latin (`Do, Re, Mi`) notation
- **Moderated Community & Admin Panel (Firebase Firestore)** — Share songs to the Community after admin review, manage users/roles with timed confirmation, and display synced user profile avatars everywhere
- **Authentication & Cloud Profile** — Email/password + native Google Sign-In, account linking, synced Google/custom gallery profile photos, and cloud backup/restore

## 🏗️ Architecture

RiffForge is a **Kotlin Multiplatform (KMP)** app sharing logic and UI across Android and iOS:

| Module | Platform | Description |
|--------|----------|-------------|
| `app/` | Native Android | Android app built with Clean Architecture + MVVM, Hilt, Room, Credential Manager, and Firebase |
| `composeApp/` | KMP (Android + iOS) | Shared UI built with Compose Multiplatform, Voyager, Koin, and `multiplatform-settings` |
| `shared/` | KMP (common) | Shared domain logic (chord library, scales, and transposition) |
| `iosApp/` | Native iOS | SwiftUI container + native Swift bridge (`FirebaseAuth`, `Firestore`, `PHPicker`, CocoaPods) |

### Design

The project combines Material 3 and **Liquid Glass** aesthetics with light/dark theme presets, dynamic palettes, and bilingual music notation support.

## 🚀 Getting Started

### Prerequisites

- **JDK 17–21**
- **Android Studio Ladybug+** or IntelliJ IDEA
- **Xcode 15+** and **CocoaPods** (for building iOS)
- **Kotlin 2.1+**

### Build Android

```bash
./gradlew :app:assembleRelease
```

### Build & Open iOS

1. Build the KMP frameworks and install CocoaPods dependencies:
   ```bash
   ./gradlew :composeApp:linkReleaseFrameworkIosArm64 :shared:linkSharedReleaseFrameworkIosArm64
   cd iosApp && pod install
   ```
2. Open `iosApp/RiffForge.xcworkspace` in Xcode (not `.xcodeproj`) and run on a simulator or physical device.

### Build composeApp

```bash
./gradlew :composeApp:assembleDebug
```

## 📦 Downloads (Releases)

Compiled installers are published under [**GitHub Releases**](https://github.com/Dibucrak/Riff-Forge/releases):

- **Android (`RiffForge-v1.1.0-android.apk`)**: Signed release APK ready for direct installation on Android 8.0+.
- **iOS (`RiffForge-v1.1.0-ios.ipa`)**: `arm64` device package built with Xcode and ad-hoc signed, ready for AltStore / Sideloadly or re-signing in Xcode with your Apple ID.

## 📄 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.

## 👥 Contributors

- **Dibu** — Creator and lead developer

---

<div align="center">

**RiffForge** — Forge your sound 🎵

</div>
