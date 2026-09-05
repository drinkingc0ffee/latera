# Latera

Android app (`com.c0ffeebreak.latera`) for bilateral stimulation: a ball travels left and right across a landscape screen and a metronome click plays in the matching speaker at each bounce.

## Use

- Landscape only. The ball stays vertically centered.
- Tap the ball area to pause or resume.
- **Beats per second** at the bottom sets how many side-hits happen each second. The value is stored and restored after restart.
- **White bg / Black bg** swaps white-on-black and black-on-white.

## Build

Requires JDK 17+ and the Android SDK.

```bash
cp local.properties.example local.properties
# set sdk.dir to your Android SDK
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
