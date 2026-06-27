BharatKey IME – Hindi & English Physical Keyboard

Build target:
- Android 12 to Android 15+
- minSdk 31
- targetSdk 35
- Kotlin + Android Gradle Plugin
- JDK 17 required

Latest fix included:
- Fixed Devanagari InScript mapping bugs:
  * g = ु, Shift+g = उ
  * l = त, Shift+l = थ
- Fixed selected-text delete issue:
  * Ctrl+A then Backspace deletes full selected text
  * Ctrl+A then Delete deletes full selected text
  * Normal Backspace/Delete still deletes one character when nothing is selected
- Physical-keyboard-only enforcement included.
- App icon included.
- Notification + keyboard picker included.

Recommended Android Studio setup:
1. Extract ZIP to a short path like C:\AndroidProjects\BharatKeyIME
2. Open folder containing settings.gradle.kts
3. Use Gradle JDK: Embedded JDK 17
4. Install Android API 35 SDK
5. Sync Gradle
6. Build > Make Project
7. Run on a real Android device
8. Enable BharatKey IME in keyboard settings
9. Select BharatKey IME and attach a physical keyboard

Important test:
- Type text
- Press Ctrl+A
- Press Backspace
- Full selected text should be deleted.

Package name:
- com.irashad1707.bharatkeyime
For Play Store or real release, change it to your own domain package.
