# বিবাহবন্ধু — গোপনীয়তা নীতির খসড়া

**অবস্থা:** খসড়া; প্রকাশের আগে প্রকৃত সেবা-পরিচালকের তথ্য, যোগাযোগের ঠিকানা, ডেটা সংরক্ষণনীতি এবং প্রযোজ্য আইন অনুযায়ী পর্যালোচনা প্রয়োজন।

## কী তথ্য নেওয়া হয়
বর্তমান অ্যাপ সংস্করণে নিবন্ধনের সময় ইমেইল/পাসওয়ার্ড Firebase Authentication-এ যায়। বায়োডাটায় নাম, পাত্র/পাত্রী নির্বাচন, বয়স, জেলা, শিক্ষা, পেশা, নিজের পরিচিতি, প্রোফাইল দৃশ্যমানতার পছন্দ এবং অনলাইন স্ট্যাটাস রাখা হয়।

## কী তথ্য অন্য সদস্যরা দেখতে পারেন
লগইন করা সদস্যরা দৃশ্যমান (visible) প্রোফাইলের নাম, বয়স, জেলা, শিক্ষা, পেশা, পরিচিতি ও অনলাইন/অফলাইন সূচক দেখতে পারেন। ইমেইল ও পাসওয়ার্ড প্রোফাইল ডকুমেন্টে রাখা হয় না এবং অন্য সদস্যদের দেখানো হয় না। প্রোফাইল লুকানো থাকলে সাধারণ প্রোফাইল তালিকায় তা দেখানোর কথা নয়।

## ডেটা কোথায় থাকে
বায়োডাটা Firebase Cloud Firestore-এ এবং লগইন-সংক্রান্ত তথ্য Firebase Authentication-এ সংরক্ষিত হয়। Firebase project owner-কে Google/Firebase-এর privacy, security, retention এবং data processing terms পর্যালোচনা করতে হবে।

## ব্যবহারকারীর দায়িত্ব ও নিয়ন্ত্রণ
ব্যবহারকারী নিজের বায়োডাটা সংশোধন করতে এবং দৃশ্যমানতা বন্ধ করতে পারেন। বর্তমান অ্যাপে প্রোফাইল মুছলে শুধু Firestore profile document মুছে; Authentication account নিজে থেকে মুছে না। সম্পূর্ণ account deletion flow এখনো যোগ করা প্রয়োজন।

## নিরাপত্তা ও সীমাবদ্ধতা
এই অ্যাপের online indicator অ্যাপের lifecycle থেকে আপডেট হয়; নেটওয়ার্ক বিচ্ছিন্নতা, crash বা force-stop-এর কারণে status stale হতে পারে। SMS OTP, report/block moderation, photo uploads, identity verification, full account deletion, support/complaint workflow এবং production security testing এখনো সম্পূর্ণ নয়।

## প্রকাশের আগে পূরণ করতে হবে
- প্রকৃত ব্যবসা/পরিচালকের নাম ও যোগাযোগের ঠিকানা
- Privacy contact এবং অভিযোগ জানানোর পদ্ধতি
- account deletion ও data retention period
- রিপোর্ট/ব্লক ও moderation procedure
- Firebase rules/security review, abuse protection এবং end-to-end tests
- বাংলাদেশের প্রযোজ্য আইন ও প্রাসঙ্গিক অন্যান্য দেশের নিয়ম অনুযায়ী আইনি পর্যালোচনা

এই খসড়া আইনগত পরামর্শ নয় এবং সম্পূর্ণ/চূড়ান্ত privacy policy হিসেবে প্রকাশ করা উচিত নয়।
