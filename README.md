# CleanShare

CleanShare is an Xposed module that removes Direct Share's suggested contact/conversation shortcuts from Android's Share Sheet.

![Android CI](https://github.com/hxreborn/cleanshare/actions/workflows/android.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/API-30%2B-3DDC84?logo=android&logoColor=white)

<div align="center">
  <img src=".github/assets/direct-share-targets.png" alt="Direct Share targets row removed" width="320" />
</div>

## About

Direct Share suggests contacts you emailed once five years ago, colleagues from jobs you no longer have, and people you'd rather not be reminded of. The suggestions are [rarely useful](https://support.google.com/android/thread/153774734). I've yet to hit a case where they helped. Might as well cut the row and skip the hassle.

CleanShare tricks the Share Sheet into thinking it's running on a low-RAM device. Android then skips the Direct Share pipeline to save resources, so the row never loads.

On devices with [Android System Intelligence](https://www.androidpolice.com/what-is-android-system-intelligence/), it also blocks backend shortcut queries to prevent share target profiling.

## Requirements

- Android 11 (API 30) or higher
- [LSPosed](https://github.com/JingMatrix/LSPosed) (JingMatrix fork recommended)
- Pixel or AOSP-based ROM (OEM skins untested)

## Installation

1. Download the APK:

   <a href="https://f-droid.org/packages/eu.hxreborn.cleanshare"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="60" alt="Get it on F-Droid" /></a>
   <a href="https://apt.izzysoft.de/fdroid/index/apk/eu.hxreborn.cleanshare?repo=main"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="60" alt="Get it on IzzyOnDroid" /></a>
   <a href="../../releases"><img src=".github/assets/badge_github.png" height="62" alt="Get it on GitHub" /></a>

2. Install and enable the module in LSPosed.
3. Configure the scope:

   **Android 13+**
   - Intent Resolver (`com.android.intentresolver`)
   - Android System Intelligence (`com.google.android.as`)
   - Ignore System Framework (`android`) even if marked "Recommended"

   **Android 11-12**
   - System Framework (`android`)
   - Android System Intelligence (`com.google.android.as`)

4. Reboot your device.

Don't worry about selecting all scopes. The module checks your Android version and only applies what's needed.

## Build

```bash
git clone --recurse-submodules https://github.com/hxreborn/cleanshare.git
cd cleanshare
./gradlew buildLibxposed
./gradlew assembleRelease
```

Requires JDK 21 and Android SDK. Configure `local.properties`:

```properties
sdk.dir=/path/to/android/sdk

# Optional signing
RELEASE_STORE_FILE=<path/to/keystore.jks>
RELEASE_STORE_PASSWORD=<store_password>
RELEASE_KEY_ALIAS=<key_alias>
RELEASE_KEY_PASSWORD=<key_password>
```

## License

<a href="LICENSE"><img src=".github/assets/gplv3.svg" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0 – see the [LICENSE](LICENSE) file for details.
