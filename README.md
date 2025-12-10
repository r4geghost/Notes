## Notes App (Jetpack Compose)

Minimalist notes app built with Jetpack Compose. Create, pin, search, edit, and delete notes that can mix text and images. Data is stored locally with Room and images are saved to internal storage to keep entries available offline.

### Features
- Compose UI with Material 3 styling and edge-to-edge layout.
- Note lifecycle: create, edit, delete, and pin/unpin.
- Text + image content per note, stored locally; removed images are cleaned up.
- Instant search across titles and text content with pinned/other grouping.
- Splash screen on launch and navigation between list, create, and edit flows.

### Tech Stack
- Kotlin, Coroutines, Flow
- Jetpack Compose (Material 3, icons, previews)
- Navigation Compose
- Room
- Hilt
- Coil
- Kotlinx Serialization
- SplashScreen

### Architecture
- Uses Clean Architecture (domain, data, presentation layers with DI and Room).

### Project Info
- Android app targeting modern SDKs; works on devices with SDK 24+.

### Run It
1) Open in Android Studio Koala or newer.  
2) Sync Gradle; required plugins: Android, Kotlin, Kotlin Serialization, KSP, Hilt, Compose.  
3) Run on device/emulator with SDK ≥ 24: `./gradlew installDebug` or press Run in Studio.

