# BenchPress13Week Android app

This is a ready Android Studio project for a custom 13-week bench press planner.

## What it does
- Accepts bench 1RM in kg
- Lets you use a training max percentage (recommended: 95%)
- Lets you choose weight rounding step
- Generates the 13-week bench plan from the provided Excel logic
- Shows a Today screen, full Program screen, and Setup screen
- Stores settings locally with DataStore

## Build APK
1. Open this folder in Android Studio
2. Let Gradle sync
3. Run on device/emulator or use:
   - Build -> Build Bundle(s) / APK(s) -> Build APK(s)

## Notes
- This project is intentionally offline-first and simple
- Good next upgrades:
  - mark workout done/skipped
  - notes per workout
  - history screen
  - recalc after week 4 or week 8
  - export/share current workout
