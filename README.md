# HealthAid (WIP)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg)
![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg)
![IDE](https://img.shields.io/badge/IDE-Android%20Studio-purple.svg)

A comprehensive Android application designed to help users track symptoms, manage daily medication reminders, and maintain personal health profiles. This project serves as a practical implementation of modern Android development concepts, focusing on clean UI/UX architecture, cloud authentication, and modular fragment-based navigation.

## Key Features
* **Smart Onboarding:** Features a dynamic splash screen with intelligent routing based on Firebase Auth state, including personalized "Welcome back" greetings and smooth fade transitions.
* **Secure Authentication:** Full login and registration system powered by Firebase Email/Password Authentication.
* **Symptom Tracker & Remedies:** A responsive grid interface providing tailored over-the-counter pill recommendations and home remedies for common ailments (e.g., Headache, Cold & Flu, Fever).
* **Daily Pill Reminders:** A dedicated tab for managing daily medications, featuring a dynamic Floating Action Button (FAB) that contextually appears only when needed.
* **Persistent User Profiles:** Local storage of user health data (age, weight, known allergies) using `SharedPreferences`, seamlessly integrated with the cloud account lifecycle and logout logic.

## Core Android Concepts Demonstrated
This project was built to showcase a strong understanding of Android lifecycle management and modern mobile UI principles:

* **Fragment-Based Architecture:** Utilizing `BottomNavigationView` and `FragmentManager` for smooth, single-activity (`MainActivity`) navigation between the Home, Reminders, and Profile screens.
* **Modern UI/UX Design:** Implementation of "Glassmorphism" aesthetics, custom vector assets, dynamic shadows, adaptive launcher icons, and immersive fluid backgrounds for a premium feel.
* **Cloud Integration (Firebase):** Handling asynchronous cloud tasks, persistent user sessions, and secure sign-outs using `FirebaseAuth`.
* **Dynamic UI Rendering:** Utilizing the `RecyclerView` component alongside custom Adapters (`SymptomAdapter`, `ReminderAdapter`) and LayoutManagers (`GridLayoutManager`, `LinearLayoutManager`) to display highly scalable data lists.
* **Local Data Persistence:** Efficient read/write operations for lightweight user preferences and UI state data via `SharedPreferences`.
* **Intent & Navigation Management:** Handling explicit Intents for Activity switching (`SplashActivity` -> `LoginActivity` -> `MainActivity`), passing extras between screens, and managing the back-stack flags (`FLAG_ACTIVITY_CLEAR_TASK`).

## Project Architecture
The project strictly separates UI logic from data models and adapters for clean, maintainable code:
* **Activities:** `SplashActivity`, `LoginActivity`, `RegisterActivity`, `MainActivity`, `SymptomDetailActivity`
* **Fragments:** `HomeFragment`, `ReminderFragment`, `ProfileFragment`
* **Adapters:** `SymptomAdapter`, `ReminderAdapter`
* **Data Models:** `Symptom`, `PillReminder`
* **Backend:** Firebase Authentication integration
