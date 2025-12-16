# 🧘‍♀️ WellnessBuddy - Holistic Health Companion

![WellnessBuddy Banner](app/src/main/res/drawable/ic_logo_wellness.xml) *<-- (Vector Drawable of the Logo)*

**WellnessBuddy** is a comprehensive, native Android application designed to help users track their habits, monitor their mood, and stay hydrated—all in one elegant, modern interface. Built with Kotlin and adhering to Clean Architecture principles, it offers a seamless and engaging user experience.

## ✨ Key Features

### 🏃‍♂️ Advanced Step Tracking
- **Real-time Pedometer**: Utilizes the device's **Accelerometer sensor** to accurate count steps.
- **Battery Efficient**: Smart persistence logic optimizes sensor usage.
- **Auto-Reset**: "Midnight Reset" functionality ensures your daily goal starts fresh every morning.

### 📅 Smart Habit Tracker
- **Customizable Habits**: Create, edit, and delete personal habits with unique icons and goals.
- **Progress Visualization**: Track your daily completion rates with intuitive progress bars.
- **Persistence**: Data is securely stored locally using **SharedPreferences & Gson** serialization.

### 💧 Hydration Monitor
- **Interactive Start**: Tap to log your water intake effortlessly.
- **Visual Feedback**: Dynamic circular progress indicators show how close you are to your daily hydration goal.

### 🎭 Mood Journal
- **Emotional Tracking**: Log your daily mood with emoji indicators and intensity sliders (1-10).
- **History & Trends**: Review your emotional wellbeing over time.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Toolkit**: XML Layouts with **Material Components (Material 3)**.
- **Architecture pattern**: MVVM (Model-View-ViewModel) with structured Fragment/Manager separation.
- **Sensors**: Android SensorManager API (Accelerometer).
- **Data Persistence**: SharedPreferences & Gson for lightweight, efficient local storage.
- **Navigation**: Jetpack Navigation Component.
- **Design**: Custom Vector Drawables, Gradients, and Animations.

## 🚀 Getting Started

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/WellnessBuddy.git
    ```
2.  **Open in Android Studio**:
    Open the project folder in the latest version of Android Studio.
3.  **Build & Run**:
    Connect an Android device or start an emulator and run the `app` configuration.

## 📸 Screenshots

*(Add screenshots of the Dashboard here)*

---

This project was built to demonstrate proficiency in **Android Sensor APIs**, **Custom View logic**, and **Clean Code practices**.
