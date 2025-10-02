# Wellness Buddy 📱

A comprehensive Android wellness tracking app built with Kotlin and Material 3 design. Help users manage their daily wellness routines including habits, mood tracking, and hydration.

## Features ✨

### 🏠 Home Dashboard
- Overview of daily habits progress
- Today's mood display
- Hydration progress tracking
- Quick action buttons for easy navigation

### 📝 Daily Habit Tracker
- Add, edit, and delete habits
- Track completion with progress bars
- Visual progress indicators
- Habit completion tracking

### 😊 Mood Journal
- Log mood with emoji selector
- Save mood entries with date/time
- View mood history
- Mood trend analysis with interactive charts
- Intensity scale (1-10)

### 💧 Hydration Reminder
- Set daily hydration goals
- Track water consumption
- Configurable reminder intervals
- Progress visualization

### 📊 Advanced Features
- **Mood Trend Chart**: Interactive line chart using MPAndroidChart showing mood patterns over time
- Data sharing capabilities
- Local data persistence

### ⚙️ Settings
- Manage user preferences
- Notification settings
- Hydration goal configuration
- App information and about section

## Technical Implementation 🛠️

### Architecture
- **Language**: Kotlin
- **UI Framework**: Android Views with Material 3 Design
- **Navigation**: Fragment-based navigation with Bottom Navigation
- **Data Storage**: SharedPreferences with JSON serialization (Gson)
- **Charts**: MPAndroidChart for mood trend visualization

### Key Components

#### Data Models
- `Habit`: Represents daily habits with completion tracking
- `MoodEntry`: Mood logging with emoji, intensity, and notes
- `HydrationSettings`: Hydration goals and tracking

#### Data Managers
- `HabitManager`: CRUD operations for habits with SharedPreferences
- `MoodManager`: Mood entry management and trend analysis
- `HydrationManager`: Hydration tracking and settings

#### Fragments
- `HomeFragment`: Dashboard with overview cards
- `HabitTrackerFragment`: Habit management with CRUD operations
- `MoodJournalFragment`: Mood logging and history
- `HydrationFragment`: Hydration tracking and settings
- `SettingsFragment`: App preferences and configuration

### Dependencies
```kotlin
implementation 'androidx.core:core-ktx:1.17.0'
implementation 'androidx.appcompat:appcompat:1.7.1'
implementation 'com.google.android.material:material:1.12.0'
implementation 'androidx.fragment:fragment-ktx:1.8.5'
implementation 'androidx.navigation:navigation-fragment-ktx:2.8.4'
implementation 'androidx.navigation:navigation-ui-ktx:2.8.4'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.8.7'
implementation 'androidx.work:work-runtime-ktx:2.10.0'
implementation 'com.github.PhilJay:MPAndroidChart:3.1.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

## Project Structure 📁

```
app/src/main/java/com/example/wellnessbuddy/
├── MainActivity.kt                 # Main activity with bottom navigation
├── data/
│   ├── Habit.kt                   # Habit data model
│   ├── MoodEntry.kt               # Mood entry data model
│   ├── HydrationSettings.kt       # Hydration settings model
│   ├── HabitManager.kt            # Habit data management
│   ├── MoodManager.kt              # Mood data management
│   └── HydrationManager.kt        # Hydration data management
└── fragments/
    ├── HomeFragment.kt             # Dashboard fragment
    ├── HabitTrackerFragment.kt    # Habit management fragment
    ├── MoodJournalFragment.kt     # Mood journal fragment
    ├── HydrationFragment.kt       # Hydration tracking fragment
    ├── SettingsFragment.kt        # Settings fragment
    ├── HabitsAdapter.kt           # Habits RecyclerView adapter
    └── MoodHistoryAdapter.kt      # Mood history RecyclerView adapter
```

## Key Features Implementation 🔧

### 1. Fragment Navigation
- Bottom navigation with 5 tabs
- Fragment transactions for smooth navigation
- State preservation across fragment switches

### 2. Data Persistence
- SharedPreferences for local data storage
- JSON serialization with Gson for complex objects
- Daily reset functionality for habits and hydration

### 3. Material 3 Design
- Modern Material 3 components
- Consistent theming and colors
- Responsive layouts for different screen sizes
- Card-based UI design

### 4. Interactive Charts
- MPAndroidChart integration for mood trends
- Line chart with touch interactions
- Customizable chart appearance

### 5. Implicit Intents
- Share wellness data functionality
- Data export capabilities

## Usage Instructions 📖

1. **First Launch**: The app initializes with sample habits
2. **Home Dashboard**: View your daily progress overview
3. **Habits**: Add new habits, track completion, edit or delete existing ones
4. **Mood Journal**: Log your daily mood with emoji and intensity
5. **Hydration**: Track water consumption and set daily goals
6. **Settings**: Configure app preferences and view information

## Sample Data 🎯

The app comes pre-loaded with sample habits:
- Morning Exercise (🏃)
- Drink Water (💧)
- Read Books (📚)
- Meditation (🧘)

## Requirements 📋

- Android API 24+ (Android 7.0)
- Kotlin support
- Material 3 design system
- Minimum SDK: 24
- Target SDK: 36

## Future Enhancements 🚀

- Push notifications for hydration reminders
- Data backup and restore
- Widget support for home screen
- Advanced analytics and insights
- Social sharing features
- Dark theme support

## Development Notes 💡

- All data is stored locally using SharedPreferences
- No external database dependencies
- Responsive design supports both phones and tablets
- Clean architecture with separation of concerns
- Well-documented code with comments

---

**Wellness Buddy** - Your personal wellness companion for building healthy daily routines! 🌟
