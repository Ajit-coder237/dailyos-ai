# DailyOS AI ⚡

DailyOS AI is an industry-grade, offline-first personal life command center built natively with Kotlin, Jetpack Compose, and Material 3. Designed for innovators, students, professionals, and privacy minimalists, DailyOS AI consolidates tasks, note archiving, routine habits, spaced-recall quiz index memory, budgets, sunset gratitude diaries, and localized secure AI chat support under a single sovereign hardware workspace.

Operating 100% locally on-device, DailyOS AI establishes a bulletproof privacy layer, leaving zero network telemetry or tracking footprint.

---

## 🎨 Visual Identity & Aesthetic Spec

DailyOS AI blends a cinematic Dark Mode (Cosmic Slate Theme) and a clean, spacious Soft-Slate Light Mode, utilizing Material 3 dynamic color styling. Consistent 8dp layout margins, tactile Material ripples, large accessibility touch targets (48dp+), custom progress indicators, and filled Material Symbols render the workspace beautiful, inviting, and highly navigable.

---

## 🏗️ Clean MVVM Architecture

The application adheres directly to core MVVM Clean Architecture guidelines, dividing tasks into decoupled, robust packages:

```
com.example
├── DailyOsApplication.kt              # Custom Application class (DI root initializer)
├── MainActivity.kt                    # Central MainActivity navigation host
│
├── core
│   ├── database
│   │   ├── Entities.kt                # 9 Room Persistent Entity Models
│   │   ├── Daos.kt                    # 8 Reactive DB Data Access Objects 
│   │   └── DailyOsDatabase.kt         # Abstract Room database instance
│   │
│   ├── datastore
│   │   └── PreferencesManager.kt      # User preferences datastore key-values
│   │
│   ├── di
│   │   ├── AppContainer.kt            # DI interface definition
│   │   └── AppContainerImpl.kt        # AppContainer manual constructor provider
│   │
│   └── notifications
│       └── NotificationHelper.kt      # Local reminders, timers, cues channels
│
├── data
│   ├── ai
│   │   └── AiAssistantService.kt      # Deterministic local AI rule intelligence
│   │
│   └── repository
│       └── AppRepositories.kt         # Repositories abstracting secure DAO operations
│
└── presentation
    ├── MainViewModel.kt               # CentralViewModel (StateFlow orchestrator)
    │
    ├── components
    │   └── DailyOsComponents.kt       # Common Material 3 Cards, Buttons, and dialogs
    │
    ├── onboarding
    │   └── OnboardingScreen.kt        # Swiper tutorial slide desk
    │
    ├── home
    │   └── HomeScreen.kt              # Comprehensive Dashboard Metrics Ledger
    │
    ├── tasks
    │   └── TaskScreens.kt             # Task filters and picker forms
    │
    ├── notes
    │   └── NoteScreens.kt             # Smart Markdown note detail conversion
    │
    ├── focus
    │   └── FocusScreens.kt            # Pomodoro countdown and full-screen min bounds
    │
    ├── habits
    │   └── HabitScreens.kt            # Routine calendar checkers & streaks tracker
    │
    ├── study
    │   └── StudyScreens.kt            # Active Recall Spaced Memory study modes
    │
    ├── expenses
    │   └── ExpenseScreens.kt          # Spending ledgers and warning progress meters
    │
    ├── review
    │   └── ReviewScreens.kt           # AM intention planning & PM reflection journals
    │
    ├── assistant
    │   └── AssistantScreen.kt         # Secure local rule-chat helper terminal
    │
    └── settings
        └── SettingsScreen.kt          # Profile variables, budget setup, and system wipe
```

---

## ⚡ Features Matrix

1. **Daily Commands Dashboard:** Aggregates active task counts, habit streak fires, cash aggregates, pomodoro minutes, and displays a "Today Brief" local executive brief.
2. **Infinite task managers:** Sort items via dynamic priorities (Low, Medium, High, Critical) and focus categories.
3. **Smart note binders:** Pinned highlights, search vaults, quick Note-to-Task creation, and AI text summaries.
4. **Deep Focus Pomodoro:** Pre-configured blocks (15, 25, 45, 60m) with ambient minimal timer clocks.
5. **Routine streakers:** Streak counter algorithms that track habits sequentially over continuous calendar days.
6. **Double-spaced study card decks:** Flashcards using custom Spaced Repetition (Again/Hard/Good/Easy) and Memory Palace cues.
7. **Expense ledger:** Price logs with spending category chips, currency selectors, and warning budget status bars.
8. **Gratitude Journals (AM/PM review):** Log morning plans and evening grateful prompts with mood sliders.
9. **Private AI chatbot:** Offline conversational prompts responding directly of tasks and plans safely.

---

## 🚀 Execution & Setup

DailyOS AI builds natively out of Gradle. It requires **Java 17+** and **Android SDK 26+**.

1. Clone or unzip this workspace in your build area.
2. Ensure Android Gradle Plugin tools compile successfully.
3. Call Gradle build from your IDE or execute compilation on command line:
   ```bash
   gradle assembleDebug
   ```
4. Verify the built APK aligns cleanly on standard streaming Android emulators.
