# App Lock 🔒

A simple Android app locker that lets you protect selected apps with a PIN.

I built this project to learn more about Android services, accessibility APIs, secure local storage, and device administration. The app runs in the background and checks which application is currently in the foreground. If the application is locked, it displays a PIN screen before allowing access.

## Features

* 🔐 PIN-based app protection
* 📱 Select which apps you want to lock
* ♿ Uses Android Accessibility Service to detect the foreground app
* 🛡️ Device Admin support to make casual uninstallation more difficult
* 🔑 PIN is stored as a SHA-256 hash rather than plain text
* 💾 Uses `EncryptedSharedPreferences` for local storage
* 🚫 No ads
* ⚡ Lightweight and runs locally on the device

## How it works

The main flow is fairly simple:

1. The user sets a PIN.
2. The user enables the Accessibility Service.
3. The app monitors foreground application changes.
4. When a locked application is opened, the lock screen is shown.
5. The user enters the PIN to continue.
6. The selected apps can be managed from the main screen.

Device Admin is also available to make uninstalling the app less straightforward. Android requires the administrator permission to be disabled before the application can normally be removed.

## Getting Started

### Requirements

* Android Studio
* Android device or emulator
* Android SDK
* USB debugging enabled if running on a physical device

### Build

Clone the repository and open the project in Android Studio.

```bash
git clone <your-repository-url>
cd AppLock
```

Let Android Studio finish the Gradle sync and then run the application on your device.

You can also generate an APK from:

**Build → Generate App Bundles or APKs → Generate APKs**

The debug APK will be available under:

```text
app/build/outputs/apk/debug/
```

## Setup

After installing the app:

1. Open **App Lock**.
2. Set your PIN.
3. Enable the Accessibility Service when Android opens the relevant settings page.
4. Enable Device Admin if you want uninstall protection.
5. Select the applications you want to protect.
6. Open one of the locked apps and enter your PIN when prompted.

## Project Structure

The project is built as a native Android application using Kotlin.

The main components are:

* **Accessibility Service** — detects foreground application changes.
* **Lock Screen** — handles PIN authentication.
* **App Selection** — manages the list of protected applications.
* **Secure Storage** — stores authentication-related data locally.
* **Device Admin Receiver** — handles the device administrator functionality.

## Limitations

This is an application-level app locker, so it is not intended to provide complete device security.

Someone with sufficient access to the device can potentially bypass the lock using methods such as disabling permissions, booting into recovery, using ADB with appropriate access, or performing a factory reset.

The goal of this project is to provide a practical layer of protection against casual access to selected applications.

## Possible Improvements

Some things I may add in future versions:

* Fingerprint / biometric unlock
* Lock-screen customization
* Temporary unlock
* Lock delay options
* Better handling of Android background-service restrictions
* Device Owner support for stronger device management

## Why I Built This

I wanted to understand how Android applications can interact with system-level features such as Accessibility Services and Device Administration while keeping sensitive information stored locally.

It was also a useful project for getting more familiar with Android permissions, services, lifecycle handling, and secure storage.
