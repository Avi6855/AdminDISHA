# 🛠️ AdminDISHA — Institute Admin Console (Kotlin + Jetpack Compose)

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/Dagger_Hilt-009688?logo=dagger&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-54_endpoints-3DDC84?logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Room-Cache-FF6F00?logo=sqlite&logoColor=white)
![CameraX](https://img.shields.io/badge/CameraX-1.3.2-3DDC84?logo=android&logoColor=white)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Tests](https://img.shields.io/badge/Unit_Tests-25_passing-brightgreen?logo=junit5&logoColor=white)

The **admin side of the DISHA Computer Education platform**. This console runs the institute's day-to-day
operations — students, courses, topics, batches, attendance, fees/payments, exams, certificates and
institute IDs — against the same REST backend that serves the published student app.

📲 **Companion student app (live on Google Play):** **[DISHA Computer](https://play.google.com/store/apps/details?id=com.avinashpatil.app.dishacomputer)**
— students sign in with the same institute-issued DISHA ID this console manages.

<img src="app/src/main/ic_launcher-playstore.png" width="110" alt="AdminDISHA app icon" />

## 👀 Quick scan (30 seconds)

| What you want | Proof in this repo |
|---|---|
| Kotlin + Jetpack Compose, Material 3 | 26 Compose screen files in `screens/` (~10k lines) |
| Coroutines & Flow, MVVM | 11 `@HiltViewModel` classes (~2.2k lines) exposing `StateFlow` UI state |
| Hilt + Retrofit + Room | `di/NetworkModule.kt`, `di/DatabaseModule.kt`, `di/DataStoreModule.kt`, **54 REST endpoints** |
| Admin auth | Email + **DISHA ID** + registered **device name** (`Build.MODEL`), JWT access token + `RefreshTokenAuthenticator` |
| Full back-office CRUD | Students (+ enrollment & payments), courses, topics, batches — create / edit / delete |
| Attendance & certificates | Date-wise attendance marking per batch; one-tap **"Send Certificates"** to a whole course |
| Real product | Version 1.0.0 · minSdk 29 · compileSdk/targetSdk 36 · runs the live institute |
| Testing mindset (TDD) | 25 JVM unit tests in `app/src/test/` — `./gradlew :app:testDebugUnitTest` |

**Scale:** 73 Kotlin files · ~15.6k lines · 26 Compose screen files · 54 API endpoints · 11 Hilt ViewModels · 2 repositories.

## ✨ Features

- **🔐 Admin login + device registration** — email + DISHA ID (masked field) + the device name is captured
  automatically from `Build.MODEL`, so an admin account can be bound to known devices (`LoginViewModel`).
- **📊 Home dashboard** — per-course **student-count statistics cards**, enrolled course list, and a
  quick-action FAB: *Add New Student*, *Send Certificates*, *See DISHA ID*, *About*
  (`HomeViewModel.refreshData()` loads counts + courses in parallel with `joinAll`).
- **🧑‍🎓 Student onboarding wizard** — a validated **3-step form** (personal details → course + batch →
  payment) with per-step validation, email/amount checks and error text helpers (`StepUpForm.kt`,
  hosted at the `AddNewStudent` / `edit_student` routes).
- **👥 Student management** — full student list with search and filtering, student detail screen with
  **editable profile and payment fields** (full name, email, total / paid / pending amount, payment
  status) pushed to the backend with `PUT api/users/{userId}`; delete-student actions included.
- **📚 Courses & topics** — create, edit and delete **courses** (`CourseViewModel`: `POST/PUT/DELETE api/courses`)
  and **topics** per course (`TopicViewModel`: `POST/PUT/DELETE api/topics`), with course pricing shown
  inside the student flow.
- **🗂️ Batch management** — all batches per course plus add/edit/delete dialogs (batch number,
  timing with a time picker, start/end dates) and batch detail with enrolled users.
- **🗓️ Attendance** — pick a **date** (date picker) and mark each student **Present / Absent** for the
  selected batch; `POST api/attendance` is called per student and the local state switches immediately,
  with search and `All / Present / Absent` filters over the batch roster.
- **💳 Enrollments & payments** — enroll a student into course + batch with a `PaymentDto`, update
  payments (`PUT api/enrollments/{id}/update-payment`), and read payment history/summary per user
  (`api/payments/user/{userId}`, `api/payments/summary/{userId}`, `api/payments/calculate`).
  `utils/PaymentDataHandler.kt` resolves the paid amount through a chain of sources — payment history
  (`api/payments/user/{id}`) → enrollment lookup → course pricing → payment summary → default — with
  retry and DTO validation, so the bill screen still shows sane numbers when one source is missing.
- **🧾 All-in-one bill console** — one screen that loads student + enrollment data (user, course, batch,
  paid amount, pending balance) and writes the corrected payment record back to the backend.
- **📝 Exams & quizzes** — per-course quiz list showing duration and **scheduled date/time**, delete-quiz
  confirmation, quiz submission and result retrieval endpoints.
- **🎓 Certificate broadcast** — `POST api/certificates/courses/{courseId}`: send certificates to every
  student of a course in a single action, with success/failure state surfaced to the UI.
- **🆔 DISHA ID directory** — searchable student list showing each student's institute DISHA ID
  (name / DISHA ID / course / batch search), for issuing credentials at the counter.
- **🤖 DISHA AI** — in-app chat assistant (OpenRouter chat completions) using the same prompt/response
  pattern as the student app, for quick admin queries.
- **🏆 Ranking** — a ranked table screen (rank, avatar, name, score) with name search.
- **🔒 Account lock endpoints** — `POST api/auth/lock-user`, `PUT api/users/{userId}/lock|unlock`: the
  institute can suspend a student's access; the student app enforces it and shows the "Account Locked"
  restriction screen.

## 🏗️ How the app is wired

```
Compose screen (screens/)
   → Hilt ViewModel (viewmodels/) — StateFlow UI state
      → Repository (data/repository/) — Room cache first, API refresh
         → Retrofit (api/)  ⟷  Room (data/local/)  ⟷  DataStore (tokens, admin prefs)
```

**Start reading here**

1. `navigation/AppNavigation.kt` — the full admin route graph (students, batches, attendance, bills, exams)
2. `api/ApiService.kt` — the backend contract, grouped and commented per module (auth, users, courses, topics, batches, attendance, enrollments, payments, quizzes, certificates)
3. `screens/student/StepUpForm.kt` — validated multi-step student onboarding
4. `screens/bill/AllInOneBillScreenNew.kt` + `viewmodels/BillViewModel.kt` — payment editing with cached + fresh data
5. `api/interceptor/AuthInterceptor.kt` + `api/interceptor/RefreshTokenAuthenticator.kt` — bearer token injection and refresh on 401

## 🗂️ Project structure

```
app/src/main/java/com/avinashpatil/app/admindisha/
├── api/            # Retrofit services, DTOs, interceptors (54 endpoints)
├── data/           # Room (AppDatabase v3, DAOs, entities), repositories, DataStore
├── di/             # NetworkModule, DatabaseModule, DataStoreModule (Hilt)
├── navigation/     # AppNavigation routes + bottom navigation
├── screens/        # 26 Compose screens: home, students, courses, topics, batches,
│                   # attendance, exams, DISHA ID, bills, AI, ranking, login
├── utils/          # PaymentDataHandler, payment flow diagnostics, helpers
├── viewmodel/ + viewmodels/   # 11 Hilt ViewModels (a few live beside their screens)
└── ui/theme/       # Material 3 theme
```

## 🌐 Backend API surface (highlights)

| Area | Endpoints |
|---|---|
| Auth | `POST api/auth/login`, `POST api/auth/refresh`, `POST api/auth/lock-user` |
| Users | `GET api/users/all-users`, `GET api/users/{id}`, `POST api/users/register`, `PUT api/users/{id}`, `DELETE api/users/{id}`, `GET api/users/batch/{batchId}`, `GET api/users/course/{courseId}` |
| Courses / topics | `GET/POST api/courses`, `PUT/DELETE api/courses/{id}`, `GET api/courses/students-count`, `GET/POST/PUT/DELETE api/topics` |
| Batches | `GET api/batches`, `GET api/batches/course/{courseId}`, `GET api/batches/{id}`, `POST api/batches`, `PUT/DELETE api/batches/{id}` |
| Attendance | `POST api/attendance`, `GET api/batches/{batchId}` (attendance for a batch) |
| Enrollments / payments | `POST api/enrollments/enroll`, `POST api/enrollments/enroll-user`, `PUT api/enrollments/{id}/update-payment`, `GET api/payments/user/{userId}`, `GET api/payments/summary/{userId}`, `POST api/payments/calculate` |
| Exams & results | `GET api/quizzes/courses/{courseId}`, `DELETE api/quizzes/{quizId}`, `POST api/quiz/submit`, `GET api/exam/result/{examId}`, `GET api/users/{userId}/statistics` |
| Certificates & ranking | `POST api/certificates/courses/{courseId}`, `GET api/leaderboard` |

## 🧪 Tests

25 JVM unit tests cover the console's pure logic, so no emulator is needed:

| Suite | What it pins down |
|---|---|
| `PaymentDataHandlerTest` (10) | The paid-amount resolution chain (payment history → enrollment → course price → summary → default) and its fallbacks |
| `PaymentModelsTest` (6) | Payment DTO formatting and validity, and the payment-summary totals including the empty case |
| `StepUpFormValidationTest` (4) | Step-1 institute email validation, including the documented double-`@` limitation |
| `ConvertersTest` (5) | Batch, user and string list round trips through the Room converters, plus ISO date/time handling |

```bash
./gradlew :app:testDebugUnitTest
```

## 🚀 Build & run

Requirements: **Android Studio (latest)**, **JDK 11**, Android SDK with **compileSdk 36**, a device/emulator on **API 29+**.

```bash
git clone https://github.com/Avi6855/AdminDISHA.git
cd AdminDISHA
./gradlew :app:assembleDebug   # build the debug APK
./gradlew :app:testDebugUnitTest   # run the 25 unit tests
```

Notes:

- Admin sessions require the institute backend to be reachable; without it the app opens but login and
  data screens cannot load.
- Credentials are never committed: `local.properties`, `google-services.json` and keystores are gitignored.
- The release build enables R8 (`isMinifyEnabled = true`) with `proguard-rules.pro`.

## 🧭 Roadmap / known gaps (honest list)

- **Product selling screens are placeholders** — `screens/selling/SellingProductsScreen.kt` and
  `AddNewProductScreen.kt` are empty stubs and are not wired into the navigation graph; the
  "selling products" module is planned, not shipped.
- **Lock/unlock is API-ready but not wired to a button yet** — `ApiService` exposes both lock endpoints;
  surfacing them on the student-detail screen would complete the loop with the student app's restriction screen.
- **Attendance history is not re-hydrated yet** — `AttendanceViewModel.loadAttendanceForDate()` currently
  clears `_attendanceMap` with a "not available in batch response yet" note, so a previously saved day's
  Present/Absent state is not repainted when you reopen the screen. The `POST api/attendance` write path works.
- **Ranking screen renders sample rows** — `screens/ranking/RankingScreen.kt` holds hardcoded `RankingItem`s
  (with real student names) while `GET api/leaderboard` is already available; bind the screen to the API next.
- **Legacy duplicates to consolidate** — `screens/AllInOneBillScreen.kt` (superseded by
  `screens/bill/AllInOneBillScreenNew.kt`) and `viewmodel/BatchViewModel.kt` vs `viewmodels/BatchViewModel.kt`.
- **Debug logging is heavy** — `AllInOneBillScreen` and the bill view models log every state transition
  with `println`; move to a `BuildConfig.DEBUG`-guarded logger before release builds.
- **Repository/agent folders are tracked** — `.vscode/`, `.starFactory/`, `.zhanlu/` and the stray
  `APK FILE/admindisha` artifact are committed and could be removed/gitignored.
- **Instrumented tests are still the generated template** — `ExampleInstrumentedTest` is a placeholder; the
  attendance and certificate flows have no UI test yet.

## 👤 Author

**Avinash Rajendra Patil** — Android developer (Kotlin, Jetpack Compose)
📧 avinashpatil6855@gmail.com · GitHub [@Avi6855](https://github.com/Avi6855)

Student app: [DISHA_Computer](https://github.com/Avi6855/DISHA_Computer) ·
[DISHA Computer on Google Play](https://play.google.com/store/apps/details?id=com.avinashpatil.app.dishacomputer)
