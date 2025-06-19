🚀 Palm Pay – The Future of Payment is in Your Palm
✨ Project Overview

Palm Pay is a next-generation Android application that simulates a biometric palm-scan-based payment system. Designed with a sleek, minimalistic UI following Material Design 3 principles and a fintech-inspired aesthetic, the project serves as a high-quality portfolio piece, especially suited for interviews at innovation-focused startups like Five.id.
🎯 Project Objective

To demonstrate system-level Android development skills by building a smart, secure, and intuitive palm-payment simulation app. It covers all critical areas required for advanced Android interviews and showcases real-world, scalable architecture.
💡 Features

    🔐 Biometric Login
    Secure app access using device-native biometrics (fingerprint/face).

    ✋ Palm Registration
    Users can simulate palm scan enrollment using the device’s camera.

    🖐️ Real-Time Palm Scan for Payment
    Instant palm match simulation for secure payment verification.

    💼 Dynamic Wallet Management
    Real-time updates on:

        Wallet balance

        Scans made

        Last payment received

    💸 Payment Flow
    Intuitive UI for entering payment amounts with immediate success/failure feedback.

    📜 Transaction History
    Scrollable list showing all past transactions with date, amount, and status.

    🔒 Secure Local Storage
    EncryptedSharedPreferences for storing authentication data securely.

    🔄 Background Processing
    WorkManager handles tasks like offline payment sync simulation.

🔧 Tech Stack
🛠 Languages & Frameworks:

    Kotlin

    Jetpack Compose (UI)

    Dagger Hilt (Dependency Injection)

🧰 Jetpack Libraries:

    ViewModel & StateFlow

    Navigation Compose

    Room (Local Database)

    DataStore Preferences

    CameraX

    WorkManager

    BiometricPrompt

💄 UI & Design:

    Material 3 Components

    Compose Animations

    Feather/Material Icons

🧠 Android Concepts Demonstrated

    ✅ MVVM + Repository Pattern

    ✅ Jetpack Compose UI Architecture

    ✅ Navigation in Compose

    ✅ Biometric Authentication

    ✅ Real-Time CameraX Integration

    ✅ EncryptedSharedPreferences

    ✅ DataStore & Room

    ✅ Kotlin Coroutines & Flow

    ✅ Background Tasks with WorkManager

    ✅ BroadcastReceivers (conceptual)

    🕹 RecyclerView Pattern with LazyColumn

    🔔 Planned: Local Notifications & Deep Linking

    🌐 Optional Future Integration: Firebase (Analytics, Auth)

📱 How to Run This Project
Step-by-step:

    Clone the repository:

    git clone https://github.com/codewith-pk/Palm-Pay.git

    Open in Android Studio:

        Choose “Open an existing project”

        Navigate to Palm-Pay/ folder

    Sync & Build:

        Allow Gradle sync to complete

        Run: Build > Clean Project, then Build > Rebuild Project

    Run the App:

        Use Emulator or physical Android device (API 23+ recommended)

        Press the green Run ▶️ button

⚠️ Testing Notes

    First Launch: Wallet balance starts at ₹0.00 and scans at 0.

    Palm Registration Prompt: Shown after successful biometric login if no palm is enrolled.

    Payment Simulation:

        Tap Get Payment

        Enter amount

        Proceed to scan screen and simulate palm verification

    Reset Wallet: Available in the Profile screen

⏭️ Planned Enhancements

    🧠 ML Kit Integration: Real hand detection using Google’s ML Kit

    🔬 Advanced Biometrics: Explore palm vein structure scanning (OpenCV/external devices)

    🔗 Backend Integration: Simulated or real payment gateway via Retrofit

    👥 User Profiles & Auth: Support for multiple users

    📊 Financial Insights: Charts and analytics (MPAndroidChart)

    🛡 Advanced Security: Code obfuscation, tamper checks, etc.

    🌍 Cross-platform Expansion: Using Compose Multiplatform (iOS/Desktop)

    🔔 Push Notifications: For payment status and background updates

🤝 Sponsor This Project

Palm Pay is open-source and actively seeking sponsors to support development of real biometric payment simulation with palm vein detection.
Ways to Support:

    🎥 Hardware Contribution (Palm Vein Camera):
    Email us at: praveen@codewithpk.com

    💖 Sponsor on GitHub:
    github.com/sponsors/codewith-pk


Your sponsorship fuels innovation in cardless, secure, biometric payments.
🌐 Stay Updated

    🔗 Website: https://codewithpk.com

    📝 Project Blog Post: codewithpk.com/palm-based-payments

📄 License

Licensed under the MIT License. See the LICENSE file for details.
👨‍💻 Built With ❤️ by @codewithpk
