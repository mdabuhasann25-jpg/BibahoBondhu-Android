# BibahoBondhu Android

Bangla-language Android marriage biodata and matchmaking app prototype.

## Online architecture
- Firebase Authentication: Email/password accounts
- Cloud Firestore: member profiles and live updates
- Authenticated users can read visible profiles
- A member can only create, update, or delete their own profile document
- Email and password are not stored in the public profile document
- Profile visibility and basic online/offline display

## Required setup before online use
Follow [FIREBASE_SETUP_BN.md](FIREBASE_SETUP_BN.md). Add the required Firebase GitHub Actions secrets and publish the rules in [firestore.rules](firestore.rules).

## Build
GitHub Actions → Build BibahoBondhu APK. The build artifact is a debug APK, not a signed Play Store release.

## Package
`com.hasantheabu.bibahobondhu`

## Not yet production-ready
SMS OTP, robust Realtime Database presence, complete account deletion, report/block and moderation tools, verified identity, photos, privacy/terms pages, and production end-to-end security testing remain outstanding. Do not collect real users' sensitive information until these are implemented and tested.
