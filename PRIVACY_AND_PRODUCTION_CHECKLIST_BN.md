# বিবাহবন্ধু — প্রকাশের আগে নিরাপত্তা চেকলিস্ট

## বর্তমান সংস্করণের সীমা
এই অ্যাপটি এখন লোকাল প্রোটোটাইপ। সদস্য, পাসওয়ার্ডের hash এবং বায়োডাটা একই Android ডিভাইসের app storage-এ থাকে। আলাদা ফোনে নিবন্ধিত সদস্যরা একে অপরের প্রোফাইল দেখতে পারবেন না। OTP, সার্ভার, ক্লাউড ব্যাকআপ, chat, photo upload, admin moderation বা online report delivery এখনো নেই। তাই বাস্তব সদস্য সংগ্রহ বা জনসাধারণের জন্য চালু করবেন না।

## অনলাইনে চালুর আগে আবশ্যক
- Firebase Authentication বা সমমানের সার্ভার-ভিত্তিক authentication এবং ফোন যাচাই/OTP কনফিগার করা।
- Cloud database-এর security rules: কেবল authenticated user; প্রতিটি member নিজের profile edit/delete করতে পারবে; private fields অন্য সদস্যের কাছে যাবে না।
- সার্ভার-সাইড profile visibility, consent, age-gate (১৮+), rate limiting এবং abuse prevention।
- Report/block workflow, moderator/admin console, audit trail এবং আপত্তিকর/ভুয়া প্রোফাইল সরানোর প্রক্রিয়া।
- Privacy policy, terms of service, consent notice, retention/deletion policy, support contact এবং প্রযোজ্য স্থানীয় আইন পর্যালোচনা।
- ছবি যোগ করলে নিরাপদ object storage, file type/size validation, access control ও delete handling।
- Production signing key, release build, dependency review, real-device testing, accessibility testing এবং backup/restore test।

## গুরুত্বপূর্ণ
লোকাল পাসওয়ার্ড hash অনলাইন নিরাপত্তার বিকল্প নয়। APK-তে গোপন API key বা server admin credential রাখা যাবে না। Debug APK Play Store production release নয়।
