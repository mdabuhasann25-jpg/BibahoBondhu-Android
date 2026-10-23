# BibahoBondhu Android

A Bengali-language Android app prototype for marriage biodata and matchmaking.

## Current build
- Native Android app using Kotlin and Jetpack Compose
- Profile creation and search UI
- Account screen and local preferences
- GitHub Actions workflow builds a debug APK on each push to main

## Important deployment status
This repository is currently a local-device prototype, not a live multi-user matchmaking service. It does not yet have a hosted backend, real OTP/password authentication, shared cloud profile storage, moderation tools, or server-side privacy enforcement. Do not use it to collect real users' sensitive biodata until those services and legal/privacy controls are configured and tested.

A production release requires:
1. A backend project (Firebase or equivalent), secure authentication, and database rules.
2. Server-side authorization so only authenticated members can view member profiles.
3. Profile visibility and consent settings, report/block, moderation, account deletion, and privacy policy.
4. Secure photo storage, upload validation, and deletion handling.
5. Signed release key and end-to-end testing.

## Build
GitHub Actions runs the Build BibahoBondhu APK workflow. Open the Actions tab, select the latest successful run, and download the BibahoBondhu-debug-apk artifact. Extract the ZIP to get app-debug.apk.

Package: com.hasantheabu.bibahobondhu
