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

## 4. কী কাজ করবে
- Email/password দিয়ে অনলাইন নিবন্ধন ও লগইন
- Firestore-এ profile save/update এবং live profile updates
- নাম, জেলা, শিক্ষা, পেশা ও পাত্র/পাত্রী filter
- profile visibility toggle
- profile screen-এ online/offline indicator
- profile document-এ email/password সংরক্ষণ না করা

## 5. গুরুত্বপূর্ণ সীমা ও নিরাপত্তা
- Firebase Auth বর্তমানে Email/Password ব্যবহার করে; SMS/phone OTP এখনো যুক্ত নয়।
- Online indicator অ্যাপ foreground/background lifecycle দিয়ে আপডেট হয়। Force-stop, crash বা network বিচ্ছিন্ন হলে status কিছু সময় stale থাকতে পারে। নির্ভরযোগ্য production presence-এর জন্য Realtime Database presence + onDisconnect/server-side logic যোগ করতে হবে।
- বর্তমান Firestore rules authenticated members-কে visible profile পড়তে দেয় এবং user-কে নিজের profile-ই edit/delete করতে দেয়। Hidden profile অন্য সদস্য পড়তে পারবেন না।
- "প্রোফাইল মুছুন" শুধু Firestore profile document মুছে; Firebase login account মুছে না। Account deletion flow ও re-authentication production launch-এর আগে যোগ করতে হবে।
- Report/block, moderator console, profile/photo verification, consent/terms, privacy policy, abuse reporting, age assurance, rate limits, data retention/deletion workflow এবং আইনগত পর্যালোচনা এখনো production launch-এর আগে প্রয়োজন।
- এই কোড build এবং বাস্তব Firebase project-এ end-to-end test না করা পর্যন্ত production-ready বলা যাবে না। বাস্তব সদস্যদের সংবেদনশীল তথ্য সংগ্রহ শুরু করবেন না।
