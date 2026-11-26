<div align="center">

<img src="Logo.png" width="160" height="160" alt="RiffForge Logo" />

# RiffForge

**Your all-in-one music companion** — tuner, metronome, chord dictionary, music theory, and more.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-brightgreen.svg)](https://kotlinlang.org)
[![KMP](https://img.shields.io/badge/KMP-Multiplatform-blue.svg)](https://kotlinlang.org/lp/multiplatform/)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-orange.svg)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%20%26%20iOS-lightgrey.svg)]()

</div>

---

## 🎸 Features

- **Tuner** — Tune your instrument with precision
- **Metronome** — Keep time with a configurable metronome
- **Chord Dictionary** — Comprehensive chord library with fretboard diagrams
- **Music Theory** — Circle of Fifths, scales, and harmonic progressions
- **Repertoire** — Organize your songs with chord transposition
- **Cloud Backup** — Save and restore your progress from the cloud
- **Spanish Support** — Note nomenclature in Spanish or English

## 🏗️ Architecture

RiffForge is a **Kotlin Multiplatform (KMP)** app sharing code across Android and iOS:

| Module | Platform | Description |
|--------|----------|-------------|
| `app/` | Native Android | Main app with Firebase, Hilt, and Compose UI |
| `composeApp/` | KMP (Android + iOS) | Shared UI via Compose Multiplatform |
| `shared/` | KMP (common) | Shared business logic (chords, transposition) |
| `iosApp/` | Native iOS | SwiftUI wrapper embedding `composeApp` |

### Design

The entire project uses the **Liquid Glass** design language, inspired by Apple Vision Pro, featuring:
- Neutral backgrounds and a single accent color (forge orange `#FF9F0A`)
- Frosted glass components
- Neutral shadows and adaptive typography
- Light and dark mode support

## 🚀 Getting Started

### Prerequisites

- **JDK 17+**
- **Android Studio Ladybug+** (or IntelliJ IDEA with Android/Kotlin plugins)
- **Xcode 15+** (for building iOS)
- **Kotlin 2.1+**

### Build Android

```bash
./gradlew :app:assembleDebug
```

### Build iOS

Open `iosApp/RiffForge.xcodeproj` in Xcode and run on a simulator or physical device.

### Build composeApp

```bash
./gradlew :composeApp:assembleDebug
```

## 📄 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.

## 👥 Contributors

- **Dibu** — Creator and lead developer

---

<div align="center">

**RiffForge** — Forge your sound 🎵

</div>
