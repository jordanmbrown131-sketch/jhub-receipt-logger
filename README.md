# Receipt Logger

A simple Android app for logging expense receipts. Take a photo of a receipt, enter how much to claim, and see a running total of everything you're owed.

Built for jHub Coding Scheme Module 2 in **Kotlin** with **Jetpack Compose** (Android's native, declarative UI framework).

> **Unclassified.** The app holds no classified or sensitive information. All data (photos and amounts) stays on the device in the app's private storage. Nothing is uploaded, synced or shared, and the app requests no network or camera permissions. Screenshots in this repository use dummy receipts only.

## Features

| Requirement | How the app meets it |
|---|---|
| Take photos of receipts | "Add receipt" opens the device camera straight away. You can retake the photo before saving. |
| Save with a timestamp of when the photo was taken | The time is read from the photo's EXIF *DateTimeOriginal* metadata, falling back to the moment the camera returned the photo. It's stored with the receipt and shown as e.g. "17 Nov 2026, 10:14". |
| Manually enter the amount to claim | A £ field with a number keypad and validation: no blanks, negatives, zero, more than 2 decimal places, or amounts over £100,000. |
| Running total | Pinned at the top of the home screen and updated as soon as a receipt is added, edited or deleted. |

Also included:
- An optional description for each receipt (e.g. "Taxi to station")
- A detail screen to view the full photo, correct the amount or delete the receipt (with confirmation)
- Receipts and photos are kept after the app closes (Room / SQLite database)
- Rotation-safe: nothing entered is lost when the screen rotates
- Light and dark mode, screen-reader labels, and the total is announced to TalkBack when it changes

## Screens

| Home (running total) | Add receipt | Receipt details |
|---|---|---|
| ![Home](docs/screenshots/home.png) | ![Add](docs/screenshots/add.png) | ![Detail](docs/screenshots/detail.png) |

## Tech stack

- Kotlin 2.0, Jetpack Compose, Material 3
- Room (on-device SQLite) for receipts
- `ActivityResultContracts.TakePicture` + `FileProvider` to capture photos into private app storage
- AndroidX ExifInterface for the photo's timestamp metadata
- Coil for image loading off the main thread
- MVVM: a `ViewModel` exposes `StateFlow`s that the Compose screens observe
- Money is stored as whole **pence** (`Long`) to avoid floating-point rounding errors

```
app/src/main/java/com/jordanbrown/receiptlogger/
├── MainActivity.kt
├── data/            Receipt entity, DAO (incl. SUM query for the total), Room database
├── ui/
│   ├── ReceiptViewModel.kt
│   ├── AppNavigation.kt
│   ├── screens/     List, Add, Detail screens + shared input fields
│   └── theme/
└── util/            Money parsing/formatting, date formatting, photo storage
```

## Build and run

1. Install [Android Studio](https://developer.android.com/studio) (free).
2. **File → Open** and select this folder. Let Gradle sync finish.
3. Create an emulator (**Device Manager → +**, e.g. Pixel 8, API 35) or plug in an Android phone with USB debugging on.
4. Press **Run ▶**.

The emulator's camera shows a virtual scene, which is fine for testing. On a real phone you can photograph real (non-sensitive) receipts.

Unit tests (money parsing and date handling): `./gradlew test`, or right-click `app/src/test` → **Run tests**.

Requirements: minSdk 26 (Android 8.0), targetSdk 35.

## Usability review

A self-review against the Usability Review headings that apply to a mobile app:

- **Content:** short, plain labels ("Total to claim", "Amount to claim"), with hint text showing an example amount. No jargon.
- **Design:** one primary action per screen. The total is visually separated in a coloured card. Consistent Material 3 styling with a single colour palette.
- **Navigation:** three screens. Every screen except home has a back arrow, and the system back button works. Leaving the add screen with an unsaved photo asks before discarding it.
- **Experience:** the camera opens immediately when you add a receipt. Errors say what to fix (e.g. "Use no more than 2 decimal places, e.g. 12.50"). Deleting asks for confirmation.
- **Accessibility:** large touch targets, system font scaling respected, content descriptions on icons and photos, dark mode, and TalkBack announces changes to the total.

Limitations / possible next steps: export to CSV/PDF for submitting a claim, marking receipts as "claimed", categories, and multi-currency support.
