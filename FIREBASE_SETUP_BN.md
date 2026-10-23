# Firebase Online Setup — BibahoBondhu (বাংলা)

এই সংস্করণে Firebase Authentication (Email/Password) এবং Cloud Firestore live profile directory ব্যবহার করা হয়েছে। Firebase প্রকল্পের মালিকের হাতে থাকা কনফিগারেশন ছাড়া অনলাইন অংশ চালু হবে না।

## 1. Firebase project তৈরি
1. https://console.firebase.google.com/ খুলে একটি project তৈরি করুন।
2. Project settings → General → Your apps → Add app → Android নির্বাচন করুন।
3. Android package name হুবহু দিন: `com.hasantheabu.bibahobondhu`
4. App registration থেকে Firebase App ID এবং Project ID নোট করুন। Web API key-ও Project settings-এ পাওয়া যাবে।
5. Authentication → Sign-in method → Email/Password চালু করুন।
6. Firestore Database তৈরি করুন (production mode বেছে নিন)।
7. Firestore Rules-এ এই repository-র `firestore.rules` ফাইলের নিয়মগুলো কপি করে Publish করুন।

## 2. GitHub Actions secrets
Repository → Settings → Secrets and variables → Actions → New repository secret-এ নিচের চারটি secret যোগ করুন:

- `FIREBASE_API_KEY` — Firebase project-এর Web API key
- `FIREBASE_APP_ID` — Android app-এর Firebase App ID (সাধারণত `1:...` দিয়ে শুরু)
- `FIREBASE_PROJECT_ID` — Firebase Project ID
- `FIREBASE_DATABASE_URL` — ঐচ্ছিক; বর্তমান Firestore-ভিত্তিক প্রোটোটাইপে ব্যবহৃত হয় না

কোনো service-account private key, admin credential বা password GitHub repository-তে commit করবেন না। Firebase API key client app-এ ব্যবহারের জন্যই তৈরি, তবে Firebase API restrictions ও quota configure করুন।

## 3. Build
Secrets যোগ করার পর Actions → Build BibahoBondhu APK → Run workflow নির্বাচন করুন। সফল run-এর Artifact থেকে `BibahoBondhu-debug-apk` ডাউনলোড করুন।

## 4. Implemented features (এগুলো build-এ আছে; live test-এর বিকল্প নয়)
- Email/password দিয়ে অনলাইন নিবন্ধন ও লগইন
- Firestore-এ profile save/update এবং live profile updates
- নাম, জেলা, শিক্ষা, পেশা ও পাত্র/পাত্রী filter
- profile visibility toggle
- profile screen-এ online/offline indicator
- profile document-এ email/password সংরক্ষণ না করা\n- বর্তমান Email/Password account-এর জন্য re-authentication চাওয়া স্থায়ী account deletion flow যোগ করা হয়েছে; বাস্তব Firebase project-এ সফলতা যাচাই করা বাকি

## 5. End-to-end test checklist (দুইটি test account/ফোন ব্যবহার করুন)

প্রথমে GitHub Actions-এ Firebase secrets সঠিকভাবে যোগ করুন, Firebase Console-এ Email/Password Authentication চালু করুন, Firestore database তৈরি করুন এবং repository-র `firestore.rules` publish করুন। এরপর নতুন APK install করে নিচের প্রতিটি পরীক্ষা করুন:

1. **Registration:** বৈধ ইমেইল, ৮+ অক্ষরের পাসওয়ার্ড, ১৮+ বয়স, নাম ও জেলা দিয়ে account তৈরি করুন। Firebase Authentication-এ user এবং Firestore `profiles/{uid}`-এ profile তৈরি হয়েছে কি না দেখুন।
2. **Validation:** ফাঁকা নাম/জেলা, ভুল বয়স, ১৮-এর কম বয়স, অসম্মতি এবং ভুল/ছোট পাসওয়ার্ড দিলে account create হওয়া আটকায় কি না দেখুন।
3. **Login/logout:** সঠিক তথ্য দিয়ে login, ভুল পাসওয়ার্ডে error, logout এবং পুনরায় login পরীক্ষা করুন।
4. **Profile update:** নাম/জেলা/শিক্ষা/পেশা/about পরিবর্তন করে save করুন; অ্যাপ restart-এর পরও মান থাকে কি না দেখুন।
5. **Visibility/security:** profile hide করলে অন্য account-এর directory থেকে অদৃশ্য হয় কি না এবং আবার visible করলে ফিরে আসে কি না পরীক্ষা করুন। অন্য account দিয়ে hidden profile direct read করা যাবে না।
6. **Search/filter:** নাম, জেলা, শিক্ষা, পেশা এবং পাত্র/পাত্রী filter আলাদাভাবে পরীক্ষা করুন।
7. **Live updates:** দুই account-এ app খুলে একটি profile update করুন; অন্যটিতে live update আসে কি না দেখুন।
8. **Online status:** foreground/background এবং network বন্ধ/চালু করে indicator-এর আচরণ দেখুন; offline status তাৎক্ষণিক না-ও হতে পারে।
9. **Account deletion:** test account-এ সঠিক বর্তমান পাসওয়ার্ড দিয়ে delete করুন। Firestore profile ও Firebase Auth user দুটোই মুছে গেছে কি না যাচাই করুন। ভুল পাসওয়ার্ডে delete হওয়া চলবে না।
10. **Rules:** Firebase Console → Firestore → Rules-এ published rules repository-র `firestore.rules`-এর সঙ্গে মেলে কি না নিশ্চিত করুন।

প্রতিটি ধাপের ফল লিখে রাখুন (PASS/FAIL) এবং FAIL হলে error message/log সংগ্রহ করুন। Production user-এর data দিয়ে পরীক্ষা করবেন না।

## 6. গুরুত্বপূর্ণ সীমা ও নিরাপত্তা
- Firebase Auth বর্তমানে Email/Password ব্যবহার করে; SMS/phone OTP এখনো যুক্ত নয়।
- Online indicator অ্যাপ foreground/background lifecycle দিয়ে আপডেট হয়। Force-stop, crash বা network বিচ্ছিন্ন হলে status কিছু সময় stale থাকতে পারে। নির্ভরযোগ্য production presence-এর জন্য Realtime Database presence + onDisconnect/server-side logic যোগ করতে হবে।
- বর্তমান Firestore rules authenticated members-কে visible profile পড়তে দেয় এবং user-কে নিজের profile-ই edit/delete করতে দেয়। Hidden profile অন্য সদস্য পড়তে পারবেন না।
- "প্রোফাইল মুছুন" শুধু Firestore profile document মুছে; Firebase login account মুছে না। Account deletion flow ও re-authentication production launch-এর আগে যোগ করতে হবে।
- Report/block, moderator console, profile/photo verification, consent/terms, privacy policy, abuse reporting, age assurance, rate limits, data retention/deletion workflow এবং আইনগত পর্যালোচনা এখনো production launch-এর আগে প্রয়োজন।
- এই কোড build এবং বাস্তব Firebase project-এ end-to-end test না করা পর্যন্ত production-ready বলা যাবে না। দুইটি আলাদা ফোনে registration, profile visibility, online/offline, logout এবং account deletion পরীক্ষা করা হয়নি। বাস্তব সদস্যদের সংবেদনশীল তথ্য সংগ্রহ শুরু করবেন না।
