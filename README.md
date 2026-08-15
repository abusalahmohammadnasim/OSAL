# App Lock

A simple, no-ads app locker for Android. PIN-protects the apps you choose, and
uses Device Admin so it can't be uninstalled with two casual taps like most
free lockers.

## How it works
- **Accessibility Service** watches which app comes to the foreground.
- If that app is on your locked list, it immediately launches a full-screen
  PIN prompt on top of it.
- The PIN is stored only as a SHA-256 hash inside **EncryptedSharedPreferences**
  (AES-256), never in plain text.
- **Device Admin** mode means someone has to go deactivate it in
  Settings → Security → Device Admin apps *before* Android will even let them
  uninstall the app — no ad-supported bypass, no accidental swipe-delete.

## Build it (you'll need Android Studio)
1. Install [Android Studio](https://developer.android.com/studio) (free).
2. `File → Open` → select the `AppLock` folder.
3. Let Gradle sync (first time may take a few minutes to download dependencies).
4. Connect your phone via USB with **USB debugging** enabled (Settings →
   About phone → tap "Build number" 7 times → Developer options → USB
   debugging), or use `Build → Generate Signed Bundle/APK` to produce an APK
   you can transfer and install directly (enable "Install unknown apps" for
   your file manager first).
5. Click Run ▶️, or install the generated APK from
   `app/build/outputs/apk/debug/app-debug.apk`.

## First-time setup on your phone
Open the app and, in order:
1. **Set a PIN.**
2. **Enable Accessibility Service** — Android will take you to Settings; find
   "App Lock Guard" under Downloaded/Installed apps and turn it on.
3. **Enable Device Admin** — confirm the prompt. This is what makes it hard
   to remove.
4. Toggle on the apps you want locked in the list below.

That's it — opening any locked app now shows the PIN screen first.

## Notes / limitations
- This gives real, private protection against someone picking up your phone
  and opening an app — but it is not unbreakable against a technically
  determined attacker with full device access (e.g., via ADB or a factory
  reset). No app-level locker fully is.
- For maximum uninstall resistance you could look into Android's
  **Device Owner** mode (provisioned via ADB during setup), which is stronger
  than Device Admin but more involved to set up — ask if you'd like that
  version.
- Biometric unlock (fingerprint) can be added with `androidx.biometric` if
  you'd like that instead of/alongside the PIN — happy to add it.
