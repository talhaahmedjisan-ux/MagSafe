# MsgSafe

একটা Android অ্যাপ যেটা Messenger/WhatsApp থেকে আসা মেসেজ নোটিফিকেশন লোকাল ডাটাবেসে সেভ রাখে,
এবং কেউ মেসেজ ডিলিট করলে সেটা দেখিয়ে দেয়।

## Build করার নিয়ম
1. Android Studio (Hedgehog বা নতুন ভার্সন) দিয়ে এই ফোল্ডার Open Project করো।
2. Gradle Sync হতে দাও (প্রথমবার একটু সময় লাগবে)।
3. একটা ফোনে/ইমুলেটরে Run করো।

## অ্যাপ চালু করার পর যা করতে হবে
1. অ্যাপ খুললে "Grant Notification Access" বাটনে চাপ দিয়ে Settings-এ MsgSafe-কে Notification Access দাও।
2. "Disable Battery Optimization" বাটনে চাপ দাও (এটা background service বেঁচে থাকার সম্ভাবনা বাড়ায়)।
3. Samsung/অন্য ফোনে Game Space ব্যবহার করলে, Game Space-এর Settings-এ গিয়ে Messenger-এর
   notification আলাদাভাবে Allow করে দিতে হবে — এটা কোড দিয়ে অটোমেটিক করা সম্ভব না, প্রতিটা
   ফোন/OEM-এ আলাদা জায়গায় এই সেটিং থাকে।

## এখন যা কাজ করে (v1)
- Messenger, Messenger Lite, WhatsApp-এর নোটিফিকেশন ধরে Room ডাটাবেসে সেভ করে
- "This message was deleted" জাতীয় নোটিফিকেশন এলে আগের মেসেজকে "deleted" হিসেবে মার্ক করে
- সার্চ বার দিয়ে চ্যাট/মেসেজ খোঁজা যায়
- Foreground service দিয়ে background-এ চালু রাখার চেষ্টা করে

## এখনো বাকি (Stage 3 থেকে)
- Google Drive weekly backup (WorkManager + Drive API লাগবে, Google Cloud প্রজেক্ট ও OAuth সেটআপ দরকার)
- Fingerprint lock (BiometricPrompt) — কাঠামো তৈরি, UI-এর সাথে যুক্ত করা বাকি

## সীমাবদ্ধতা যা জেনে রাখা দরকার
- এটা শুধু নোটিফিকেশনে যতটুকু টেক্সট দেখায় ততটুকুই সেভ করতে পারে — মেসেঞ্জারের পুরো chat history
  বা ছবি/ভিডিও এভাবে ধরা যায় না।
- "কোন মেসেজ ডিলিট হলো" এই ম্যাচিং heuristic-ভিত্তিক (same chat + closest time) — কখনো কখনো ভুল
  মেসেজকে deleted মার্ক করতে পারে, বিশেষ করে একই চ্যাটে একসাথে অনেক মেসেজ এলে।
- Play Store-এ পাবলিশ করতে চাইলে Notification Access ব্যবহারের কারণ ব্যাখ্যা করে একটা Privacy
  Policy জমা দিতে হবে, এবং রিভিউ কড়া হতে পারে।
- এটা অন্যদের পাঠানো মেসেজও সেভ করে রাখে, তাদের অনুমতি ছাড়া — শুধু ব্যক্তিগত ব্যবহারের জন্য রাখাই ভালো।
