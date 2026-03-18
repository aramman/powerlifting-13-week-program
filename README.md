# powerlifting-13-week-program

Android app for a full 13-week powerlifting-focused bench progression block with per-set tracking.

## What it does
- Accepts maxes for bench, squat, deadlift, and overhead press
- Accepts exact accessory weights for self-selected movements
- Generates exact kg prescriptions for the full 13-week source program
- Tracks every set individually and only advances when the workout is complete
- Supports pause/resume and restart of the training block
- Stores settings and progress locally with DataStore

## Release setup
1. Copy `keystore.properties.example` to `keystore.properties`
2. Point `storeFile` at your upload keystore
3. Fill in the store password, key alias, and key password
4. Build release with:
   - `./gradlew assembleRelease`
   - `./gradlew bundleRelease`

## Product next steps
- Add onboarding and first-run sample values
- Add privacy policy and support links
- Add analytics and crash reporting
- Add subscription/paywall if you want to monetize directly
