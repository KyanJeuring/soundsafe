# SoundSafe

SoundSafe is an Android application designed to protect your hearing by monitoring ambient sound levels and automatically adjusting your device's volume. It ensures a safe and comfortable listening environment by adapting to your surroundings in real-time.

## Features

- **Background Sound Monitoring**: Continuously measures ambient decibel levels using a foreground service.
- **Automatic Volume Control**: Intelligently adjusts Media and Ringtone volumes based on environmental noise.
- **Environment Classification**: Categorizes your surroundings (e.g., Quiet, Moderate, Loud) to provide context-aware adjustments.
- **Historical Analytics**: Tracks sound levels over time, providing insights into your auditory environment.
- **User-Friendly Dashboard**: Built with Jetpack Compose for a modern, responsive user interface.
- **Dark Mode Support**: Seamlessly transitions between light and dark themes.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Dependency Management**: Gradle (Kotlin DSL)
- **Architecture**: MVVM-inspired architecture with a Repository-like `SoundMeasurementStore` and a Foreground Service for long-running monitoring.

## Architecture Overview

- **`SoundMonitoringService`**: A foreground service that manages the `DecibelMeter` and `VolumeController`. It ensures monitoring persists even when the app is in the background.
- **`DecibelMeter`**: Handles raw audio sampling and decibel calculation.
- **`SoundMeasurementStore`**: An in-memory store (using `StateFlow`) for recent measurements, enabling real-time UI updates.
- **`AppDatabase`**: Persists historical sound data for long-term analytics.
- **`SoundEnvironmentClassifier`**: Uses smoothed decibel data to determine the current noise environment.
- **`VolumeController`**: Interfaces with the system's `AudioManager` to perform volume adjustments.

## Getting Started

### Prerequisites

- Android Studio Panda (or newer)
- Android SDK 33+ (Target SDK)
- A physical Android device or emulator with microphone support.

### Permissions

SoundSafe requires the following permissions:
- `RECORD_AUDIO`: To measure ambient noise levels.
- `POST_NOTIFICATIONS`: To display the foreground service notification (Android 13+).

### Building

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the `app` module on your device.

## Usage

1. **Grant Permissions**: Upon first launch, grant the requested audio and notification permissions.
2. **Start Monitoring**: The service starts automatically. You can toggle recording on/off from the dashboard.
3. **Configure Settings**: Enable "Auto Media" or "Auto Ringtone" in the settings to allow SoundSafe to manage your volume.
4. **View History**: Check the dashboard and analytics screens to see your sound level trends.
