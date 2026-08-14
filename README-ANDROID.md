# PFriend Android

Native Android client written in Kotlin + Jetpack Compose. No WebView, HTML, Flutter or React Native.

## Build
Requirements: JDK 17+, Android SDK 36, Build Tools 36.0.0, Gradle 9.5.0.

```bash
gradle :app:assembleDebug
```

The first launch asks for the URL where the PHP backend is installed, for example `https://example.com/pfriend/`.

All user-entered tracker data is visible to other authenticated PFriend users by design. Registration requires explicit acknowledgement of that rule.
