<div align="center">

# 🩸 Blood Network Bangladesh

### Connecting donors and recipients across Bangladesh

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-.NET-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)](https://dotnet.microsoft.com)

**A native Android app that connects blood donors with people in need across Bangladesh.**

[Features](#features) • [Tech Stack](#tech-stack) • [Getting Started](#getting-started) • [About](#about-the-developer)

</div>

---

## 🎯 Why I Built This

Every year, thousands of Bangladeshis struggle to find compatible blood donors during emergencies. Existing solutions are fragmented—Facebook groups, scattered WhatsApp chains, or unreliable phone trees. When someone needs blood urgently, they can't afford to spend hours searching.

**Blood Network Bangladesh** solves this by creating a centralized, verified donor network where:

1. **Find donors fast:** Search by blood group, district, and upazila to locate nearby donors
2. **Request blood:** Submit urgent requests with hospital details and required-by dates
3. **Donor availability:** Donors can mark themselves available/unavailable and track their last donation date
4. **Eligibility checks:** Built-in questionnaire ensures donors meet health requirements before donation
5. **Real-time notifications:** Instant alerts for new requests matching donor profiles (via SignalR)
6. **AI assistant:** 24/7 chatbot answers questions about blood donation and eligibility

Built with **Kotlin + Jetpack Compose (Material 3)**, connecting to a .NET backend API at `https://blood-network-bangladesh.onrender.com/api`.

Package: `com.bloodnetwork.bangladesh` • Min SDK 26 (Android 8.0+) • Target SDK 35

---

## ✨ Features

### 1. 🔐 Authentication & User Roles

- **Phone + password login** with Bangladeshi number validation
- **Two user roles:** Donors and Requesters (can be both)
- **Session persistence** with JWT tokens stored in DataStore
- **Secure logout** clears local session completely

### 2. 🩸 Find Blood

- **Multi-filter search:** blood group, district, upazila, availability status
- **Donor cards** show name, location, last donation date, and availability
- **Direct request:** tap "Request Blood" on a donor card to pre-fill their details
- **Pagination:** load more donors as you scroll

### 3. 📝 Request Blood

- **Comprehensive form:** blood group, units needed, hospital name/address, required-by date
- **Location picker:** division → district → upazila cascade with search
- **Urgency levels:** Critical, Urgent, Normal
- **Patient details:** name, relation, contact phone (optional fields)
- **Pre-fill from donor:** auto-populate blood group and location when requesting from a donor card

### 4. 👤 Donor Profile

- **Create/update profile:** blood group, gender, date of birth, location (division/district/upazila)
- **Custom address:** for areas not in the dropdown
- **Availability toggle:** mark yourself available or unavailable for donation
- **Last donation date:** tracks when you last donated (90-day gap enforced)
- **Draft saving:** incomplete profiles are saved locally so you don't lose progress

### 5. ✅ Eligibility Quiz

- **Interactive questionnaire:** weight, age, recent illness, medications, travel history
- **Real-time validation:** answers determine eligibility based on medical guidelines
- **Bilingual:** questions and explanations in English and Bangla
- **Pass/fail feedback:** clear messaging with next steps if ineligible

### 6. 🔔 Notifications

- **Real-time updates** via SignalR WebSocket (no polling)
- **Type filters:** all, donor requests, system alerts, blood requests
- **Mark as read** with visual indicators for unread count
- **Deep linking:** tap notification to navigate to relevant screen

### 7. 🤖 AI Chatbot

- **24/7 assistant** powered by Groq (proxied through backend, no API key in app)
- **Conversation history** maintained during session
- **Bangla support:** ask questions in English or Bangla
- **Topics:** donation eligibility, blood types, health guidelines, general questions

### 8. 📊 Admin Dashboard (Admin role only)

- **Analytics:** blood group distribution, 30-day request trends, district breakdowns
- **User management:** view/search users, activate/deactivate accounts
- **Reports:** view and resolve user-submitted reports
- **Audit logs:** track all actions with timestamps and user attribution
- **System settings:** configure platform-wide parameters

### 9. ℹ️ About Screen

- **Developer info:** name, role, contact details (admin-editable)
- **Version info:** app version and build details
- **Support:** contact developer directly

---

## 🛠 Tech Stack

### Frontend
- **Kotlin 2.0.0** with Jetpack Compose BOM 2024.06.00
- **Material 3** design with custom blood-red palette
- **Navigation Compose** for type-safe routing
- **ViewModel + StateFlow** for reactive state management
- **Manual DI** via `AppContainer` (no Hilt/KSP to keep builds lean)

### Networking
- **Retrofit 2.11.0** + **OkHttp 4.12.0** for API calls
- **kotlinx.serialization** for JSON (camelCase + string enums)
- **SignalR client** for real-time notifications
- **JWT token** persistence in DataStore

### Architecture
- **MVVM pattern:** Screens → ViewModels → Repository → API
- **Repository pattern:** single source of truth for data
- **Interceptor chain:** auto-attach JWT, refresh on 401, error handling
- **Offline-first:** draft profiles saved locally, sync when online

### Security
- **HTTPS only** — cleartext disabled at OS level + `network_security_config.xml`
- **System CAs only** — blocks MITM via user-installed certificates
- **No secrets in APK** — Groq API key lives only on backend
- **R8 obfuscation** on release builds with keep rules for serialization
- **Backups disabled** — tokens never restore from cloud backup

---

## 📁 Project Structure

```
app/src/main/java/com/bloodnetwork/bangladesh/
├── BloodNetworkApp.kt              Application class, owns AppContainer
├── MainActivity.kt                 Compose host with edge-to-edge
├── data/
│   ├── AppContainer.kt             Manual DI container
│   ├── BloodNetworkRepository.kt   Singleton facade over API + token store
│   ├── model/                      @Serializable DTOs (camelCase + string enums)
│   ├── network/
│   │   ├── BloodNetworkApi.kt      Retrofit interface
│   │   ├── ApiClient.kt            OkHttp client with interceptors
│   │   ├── AuthInterceptor.kt      Attaches JWT to requests
│   │   ├── TokenRefreshInterceptor.kt  Handles 401 refresh
│   │   ├── ApiErrorInterceptor.kt  Global error handling
│   │   └── NotificationSocket.kt   SignalR client for real-time
│   └── prefs/
│       ├── TokenStore.kt           JWT persistence (DataStore)
│       ├── DonorProfileStore.kt    Draft profile storage
│       └── RegistrationStore.kt    Incomplete registration data
└── ui/
    ├── AppRoot.kt                  Navigation host with bottom nav
    ├── theme/                      Material 3 theme (blood-red palette)
    ├── components/                 Reusable Compose components
    │   ├── UiComponents.kt         Buttons, text fields, chips, pickers
    │   ├── Avatar.kt               User avatar with initials
    │   ├── Animations.kt           Bounce and fade animations
    │   └── Skeletons.kt            Loading placeholders
    ├── i18n/Strings.kt             Bilingual strings (English/Bangla)
    ├── viewmodel/                  StateFlow-based ViewModels
    │   ├── AuthViewModel.kt        Login/register/logout
    │   ├── DonorViewModel.kt       Donor profile CRUD + availability
    │   ├── FindBloodViewModel.kt   Search and filter donors
    │   ├── RequestBloodViewModel.kt  Submit blood requests
    │   ├── NotificationsViewModel.kt  Fetch and mark notifications
    │   ├── ChatbotViewModel.kt      AI chat conversations
    │   ├── AdminViewModel.kt       Admin dashboard actions
    │   └── ... (12 total)
    └── screens/                    15+ screens
        ├── auth/
        │   ├── LoginScreen.kt
        │   └── RegisterScreen.kt
        ├── LandingScreen.kt        Home + Find Blood + My Requests + Profile
        ├── DonorProfileScreen.kt
        ├── RequestBloodScreen.kt
        ├── DonorMatchesScreen.kt
        ├── EligibilityScreen.kt
        ├── NotificationsScreen.kt
        ├── ChatbotScreen.kt
        ├── AboutScreen.kt
        └── Admin*Screen.kt         (5 admin screens)
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- Android SDK with API level 35 installed
- JDK 17+ (bundled with Android Studio)

### Build & Run

1. **Clone the repo:**
   ```bash
   git clone https://github.com/InfinityAbir/Blood-Network-Bangladesh-Android-App.git
   ```

2. **Open in Android Studio:**
   - File → Open → select the cloned folder
   - Let Gradle sync complete (downloads dependencies on first run)

3. **Run the app:**
   - Select an emulator or connected device (Min SDK 26)
   - Click Run ▶ or press `Shift+F10`

4. **Build release APK (optional):**
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release.apk`

### API Configuration

The app connects to the .NET backend at `https://blood-network-bangladesh.onrender.com/api`.

To change the base URL, edit `app/src/main/res/values/strings.xml`:
```xml
<string name="base_api_url">https://your-backend-url.com/api</string>
```

---

## 🔐 Release Build & Signing

Release builds need a signing key before `assembleRelease`/`bundleRelease` will produce an installable artifact.

### One-time setup

1. **Generate a keystore** (from Android Studio's Terminal):
   ```bash
   keytool -genkeypair -v -keystore release.jks -alias bloodnetwork -keyalg RSA -keysize 2048 -validity 10000
   ```
   Pick strong passwords and **back them up securely** — losing them means you can never update the Play Store listing again.

2. **Configure signing:**
   - Copy `keystore.properties.example` → `keystore.properties` (gitignored)
   - Fill in `storeFile` path, passwords, and alias

3. **Build release artifacts:**
   ```bash
   ./gradlew bundleRelease    # Play Store .aab
   ./gradlew assembleRelease  # Sideloadable .apk
   ```

4. **Version bumps:**
   - Increment `versionCode` (integer, always increases)
   - Update `versionName` (human-readable string)
   - Both in `app/build.gradle.kts`

---

## 📱 Permissions

| Permission | Why |
|------------|-----|
| `INTERNET` | API calls to backend |
| `ACCESS_NETWORK_STATE` | Check connectivity before requests |
| `POST_NOTIFICATIONS` (Android 13+) | Real-time alerts for new requests |

**No location tracking** — users manually select their division/district/upazila.

---

## 🗺 Roadmap

### Phase 1 (Current)
- ✅ Donor search and filtering
- ✅ Blood request submission
- ✅ Donor profiles with availability
- ✅ Eligibility quiz
- ✅ Real-time notifications (SignalR)
- ✅ AI chatbot integration
- ✅ Admin dashboard
- ✅ Bilingual support (English/Bangla)

### Phase 2 (Planned)
- ⏳ **FCM push notifications** for background alerts
- ⏳ **Biometric unlock** with Keystore-encrypted tokens
- ⏳ **Certificate pinning** for enhanced security
- ⏳ **Offline mode** with local cache for donor search
- ⏳ **SMS fallback** for donors without smartphones
- ⏳ **Donation history** tracking and certificates

### Phase 3 (Future)
- ⏳ **Blood bank integration** — connect with hospitals and blood banks
- ⏳ **Donor rewards** — gamification and recognition
- ⏳ **Community features** — donor forums and success stories
- ⏳ **Multi-language** — add more regional languages

---

## 🤝 Contributing

Contributions are welcome! Here's how to get involved:

1. **Fork** the repository
2. **Create a feature branch:** `git checkout -b feature/your-feature`
3. **Commit your changes:** `git commit -m "Add your feature"`
4. **Push and open a Pull Request**

### Guidelines
- Keep code style consistent with existing patterns
- Add unit tests for new ViewModels and Repository methods
- Test on both English and Bangla locales
- Update documentation for new features
- Squash commits before merging

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names (no abbreviations)
- Add KDoc comments for public APIs
- Keep Compose functions small and focused

---

## 👨‍💻 About the Developer

**Abir Hasan**  
Mobile App Developer • Bangladesh

I'm a passionate Android developer focused on building clean, user-friendly apps that solve real problems in Bangladesh. I believe in open-source, continuous learning, and creating software that makes a positive impact.

### Connect with me

- 🌐 **Portfolio:** [https://infinityabir.github.io/abir-hasan-portfolio/](https://infinityabir.github.io/abir-hasan-portfolio/)
- 💼 **LinkedIn:** [https://www.linkedin.com/in/infinityabirhasan/](https://www.linkedin.com/in/infinityabirhasan/)
- 🐙 **GitHub:** [https://github.com/InfinityAbir](https://github.com/InfinityAbir)
- 📧 **Email:** infinityabir@gmail.com

### Other Projects

- **[NoorUp Islamic Lifestyle App](https://github.com/InfinityAbir/NoorUp-Islamic-Lifestyle-App)** — Prayer times, Quran, Duas, and Islamic tools for Bangladesh

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ in Bangladesh**

If this project helped you, please ⭐ the repo!

</div>