# Blood Network Bangladesh — Android App

Native Android client (Kotlin + Jetpack Compose) for the Blood Network Bangladesh platform.
It consumes the existing .NET backend at `https://blood-network-bangladesh.onrender.com/api` — **no backend
changes are required**. The app reuses the same API, including the `/api/chat` AI chatbot (which proxies Groq
server-side, so no API key is ever shipped in the APK).

## Requirements

- **Android Studio** (Ladybug or newer recommended) — bundles the JDK, Android SDK and Gradle. This machine does
  not have Java/Gradle/Android SDK installed, so builds run from Android Studio (or your own CLI tooling).
- Android SDK `compileSdk = 35`, `minSdk = 26` (Android 8.0+), JVM target 17.

## Build & Run

1. Open Android Studio → **Open** → select this folder (`Blood Network Bangladesh Android App`).
2. Let Gradle sync complete (downloads dependencies on first run).
3. Press **Run** ▶ on an emulator or connected device.

To build a release APK from the terminal:
```
./gradlew assembleRelease
```
The APK is at `app/build/outputs/apk/release/app-release.apk`. The release build is R8-optimized (minified + obfuscated).

> **Android Studio will detect no existing `gradle-wrapper.jar`.** Run the wrapper task once: you can also execute
> `gradle wrapper` from your installed Gradle, or just sync/build from Android Studio which handles the wrapper.

## Release build & signing

Release builds need a signing key before `assembleRelease`/`bundleRelease` will produce an installable
(and Play-Store-uploadable) artifact. One-time setup, from Android Studio's **Terminal** tab (it bundles a JDK):

```
keytool -genkeypair -v -keystore release.jks -alias bloodnetwork -keyalg RSA -keysize 2048 -validity 10000
```

Answer its prompts (name/org/etc. — anything reasonable) and **pick strong store/key passwords you will not
lose** — losing this file or its passwords means you can never publish an update to the same Play Store
listing again. Then:

1. Copy `keystore.properties.example` → `keystore.properties` (repo root, gitignored) and fill in the
   `storeFile` path plus the passwords/alias you just chose.
2. Build the release artifacts:
   ```
   ./gradlew bundleRelease   # Play Store .aab
   ./gradlew assembleRelease # sideloadable .apk, e.g. for internal testing
   ```
3. Back up `release.jks` and its passwords somewhere durable and private (password manager + offline copy) —
   they're gitignored on purpose and this repo has no other copy.

For every release after the first, bump both `versionCode` (integer, always increases) and `versionName`
(the human-readable string) in `app/build.gradle.kts` before building.

## Architecture

Layered, with manual dependency injection (no Hilt/KSP to keep the build lean):

```
app/src/main/java/com/bloodnetwork/bangladesh/
├── BloodNetworkApp.kt          Application; owns the AppContainer
├── MainActivity.kt             Compose host
├── data/
│   ├── AppContainer.kt         Manual DI container
│   ├── BloodNetworkRepository.kt  Singleton facade over the API + token store
│   ├── model/                  @Serializable DTOs (camelCase + string enums)
│   ├── network/                Retrofit interface, OkHttp client, auth interceptor
│   └── prefs/TokenStore.kt     Session persistence (DataStore)
└── ui/
    ├── AppRoot.kt              Navigation host
    ├── theme/                  Material 3 theme (blood-red palette)
    ├── components/             Reusable Compose components
    ├── viewmodel/              StateFlow-based ViewModels
    └── screens/                Landing, Login, Register, FindBlood, RequestBlood,
                                DonorDashboard, DonorProfile, Eligibility, Notifications, Chatbot
```

### Data/API contract
- JSON is **camelCase** with **enums serialized as PascalCase strings** (the .NET API uses `JsonStringEnumConverter`).
  Every DTO uses explicit `@SerialName` to match; never rename fields without changing both sides.
- All calls go through `BloodNetworkRepository`; screens never touch Retrofit directly.
- JWT access token is attached via `AuthInterceptor`. Tokens are persisted in DataStore and cleared on logout.

### Screens (v1)
Landing · Login · Register · Find Blood (donor search) · Donate (eligibility) · Request Blood ·
Donor Profile (create/update + availability) · Notifications (realtime via SignalR, type filter, deep-link
tap) · AI Chatbot · About (developer contact info, admin-editable) · Admin Dashboard, Analytics
(charts: blood-type distribution, 30-day trends, district breakdowns), User Management, Reports, Audit Logs.

### Realtime notifications

The app connects to the backend's `/hubs/notifications` SignalR hub (`data/network/NotificationSocket.kt`)
while logged in, so new notifications and unread-count changes show up live instead of only on manual
refresh/poll. Push-when-backgrounded (FCM) is a deferred follow-up — see `plan.md`/the roadmap doc for why.

## Security

- **HTTPS only** — cleartext is disabled at the OS level (`usesCleartextTraffic=false`) and again via
  `network_security_config.xml`, which also restricts trust to **system CAs only** (blocks MITM via a
  user-installed CA).
- **No secrets in the app** — the Groq key and DB credentials live only on the backend; the app calls `/api/chat`.
- **R8 obfuscation on release** with kotlinx.serialization / Retrofit / OkHttp keep rules.
- **Backups disabled** (`allowBackup=false` + `data_extraction_rules`) so tokens never restore from cloud backup.
- **Session**: access token sent as `Authorization: Bearer`. Logout clears local session.

### Follow-ups (see plan.md)
- Migrate tokens to **Keystore-encrypted storage** and add an optional **Biometric** unlock.
- Add **certificate pinning** once the production certificate SHA-256 pins are known (add to `ApiClient`).
- Add refresh-token rotation on 401.

## AI Chatbot

The chatbot screen calls the existing `POST /api/chat` endpoint. The request body:
```jsonc
{ "message": "...", "history": [ { "role": "assistant|user", "content": "..." } ] }
// response: { "reply": "..." }
```
The server handles the Groq call, system prompt and rate limiting — the app just sends the conversation.

## Config

The API base URL is defined in `app/src/main/res/values/strings.xml` (`base_api_url`).

## Repository root

The Android app lives at `E:\Temp\Project\Blood Network Bangladesh Android App`. The related web/API project is in
`E:\Temp\Project\Blood Network Bangladesh` (`plan.md` / `memory.md` there track the Android phase too).
