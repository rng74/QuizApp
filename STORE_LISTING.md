# Google Play Store Listing — kz.yers.quiz v5.0

Released as **versionCode 5 / versionName "5.0"**. App is Russian-only by design,
so default locale = `ru-RU`. Optional English fallback included below for users
whose device locale doesn't match `ru` (US users see this on the web Play Store).

Map this document 1-to-1 to:
**Play Console → kz.yers.quiz → Grow → Store presence → Main store listing**.

---

## 1 · App name  (Play Console: "App name", max 30 chars displayed)

Pick one — ranked by my preference. Russian for the default locale.

1. **Аниме Квиз: угадай OST**  ·  22 chars  ·  *recommended* — brand + clear
   anime-music keyword for search.
2. **Угадай аниме по саундтреку**  ·  26 chars — what `strings.xml` carries
   today; descriptive but no brand.
3. **Аниме Квиз — угадай трек**  ·  24 chars — brand + slightly softer "трек".
4. **Аниме Квиз**  ·  10 chars — punchy, brand-only; loses search keywords.

If you change `app_name` in `strings.xml`, make sure it stays ≤ 26 chars so it
also fits under the launcher icon on most Android devices without truncation.

---

## 2 · Short description  (Play Console: "Short description", max 80 chars)

Russian, ordered by my preference. The Play Store renders this on the search
result tile and at the top of the page, so it has to read in one breath.

1. **Угадай аниме по саундтреку. Дуэли с друзьями, дневной челлендж, рекорды.**  ·  72 chars
2. **Слушай OST и угадывай аниме! Дуэли по коду, дневной вызов, лидерборд.**  ·  70 chars
3. **Угадай аниме за 10 секунд. Каждый день новый трек, дуэль с другом.**  ·  65 chars

---

## 3 · Full description  (Play Console: "Full description", max 4000 chars)

Copy-paste the block below verbatim into Play Console. Play Console will accept
plain text + simple line breaks; no markdown.

```
🎵 АНИМЕ КВИЗ — УГАДАЙ ПО САУНДТРЕКУ

Узнаёшь любимое аниме по первым секундам OP, ED или вставки? Тогда тебе сюда.
Игра построена на ассоциации «трек ↔ аниме»: слушаешь — выбираешь — зарабатываешь очки.

🎮 РЕЖИМЫ ИГРЫ
• Лёгкий — только хиты, рейтинг 8.3+
• Нормальный — крепкая база, рейтинг 7.5+
• Случайный — что выпадет, то и слушаешь
• Хардкор — забытые треки и нишевые сезоны

🔥 ДНЕВНОЙ ВЫЗОВ
Один трек на всех — для каждого игрока в мире одинаковый. Двойные очки. Серия из подряд сыгранных дней — копи, не теряй.

⚔️ ДУЭЛЬ С ДРУГОМ
Создаёшь дуэль — получаешь 6-символьный код. Делишься с другом — он подключается. Оба слушают один и тот же набор из 10 треков (без подсматривания), потом сравниваете счёт. Никаких аккаунтов и регистраций.

👥 ДРУЗЬЯ И ЛИДЕРБОРД
Глобальный топ-30 и личный рейтинг. Добавляешь друзей по коду — видишь их рекорды рядом со своим.

💡 ПОДСКАЗКИ
• 50/50 — убирает два неверных варианта
• Буква — открывает первую букву названия
• Пропуск — двигаешься дальше без штрафа
Покупаешь за монеты или смотришь короткую рекламу.

🏆 МОНЕТЫ
Зарабатываешь за каждый ответ. Бонус за каждые 7 дней серии. Тратишь на подсказки в магазине.

🎌 СТИЛЬ
Манга-дизайн: страницы в стиле комикса, рукописные шрифты, акценты «дон!». Не очередной плоский Material.

♿ ДОСТУПНОСТЬ
• Режим уменьшенного движения — отключает анимации
• Дальтоник-режим — синий вместо зелёного, оранжевый вместо красного
• Крупный шрифт +20%
• Шрифт Lexend для читаемости при дислексии

🔒 ПРИВАТНОСТЬ
Никаких аккаунтов. Анонимный вход в Firebase, никаких email/пароля. Личные данные не собираем. Аналитика обезличена — только статистика прохождений.

📡 ЧТО НУЖНО
• Интернет — для загрузки треков и работы дуэлей/лидерборда
• Android 7.0 (API 24) и выше
• Лучше с наушниками

———
Нашёл баг или есть идея? Напиши в Play-консоли в «Связь с разработчиком».
```

**Hits these limits, with breathing room:** ~1900 chars used out of 4000 max.
Room to add e.g. a "Что нового в 5.0" block or a partner-aniclub credit later.

---

## 4 · Release notes / "What's new" for v5.0  (max 500 chars)

Play Console: **Releases → Production → Edit release → "Release notes"** (per
locale). Russian:

```
v5.0 — большой полировочный апдейт
• Реальные награды AdMob вместо тестовых
• Восстановление данных при обновлении (раньше серии и друзья обнулялись)
• Исправлен размытый постер на Android 11 / Galaxy A03
• Стабильность на устройствах с 2 ГБ ОЗУ
• Дуэль теперь корректно показывает причину сбоя
• Доступность: подтверждения сброса, крупные тапа, плюрализация времени
• Аналитика для будущих улучшений
```

≈ 360 chars — fits.

---

## 5 · Optional English fallback  (Play Console: "Add language → en-US")

The app UI is Russian-only, but adding an English-locale listing means
non-Russian-locale Android users see meaningful text on the web Play Store
instead of falling back to your developer-account default. The app launches
in Russian regardless.

### Short description (en-US, 80 chars)
```
Guess the anime by its OST. Daily challenge, code-based duels, leaderboard.
```

### Full description (en-US, abridged)
```
🎵 ANIME QUIZ — GUESS BY THE SOUNDTRACK

App is in Russian. UI, content and answer choices are Russian-language.

Listen to the first seconds of an OP, ED or insert and pick the anime. Score
beats the clock — answer faster, score higher.

GAME MODES
• Easy — chart hits, rating 8.3+
• Normal — solid base, rating 7.5+
• Random — anything from the catalog
• Hardcore — obscure tracks and niche seasons

DAILY CHALLENGE
Same track for every player worldwide, every day. Double points. Streak
counter rewards consecutive days.

CODE-BASED DUEL
Generate a 6-character code, share it with a friend, both play the same set
of 10 tracks. Compare scores. No account, no signup.

FRIENDS + LEADERBOARD
Add friends by code, see their best scores next to yours. Global top-30.

HINTS, COINS, ADS
50/50, reveal letter, skip — buy with coins or watch a short rewarded ad.

ACCESSIBILITY
Reduce motion, color-blind safe palette, +20% text scale, Lexend dyslexia
font.

PRIVACY
Anonymous Firebase auth. No emails, no passwords. No personal data.
```

---

## 6 · Visual assets  ·  what's required, current status, dimensions

These are the four Play-Console fields under **Graphics**. I cannot author
binary images from a terminal — these are the user-action items.

| Field | Required? | Spec | Current state |
|---|---|---|---|
| **App icon** | required | 512×512 PNG, 32-bit, no alpha (web Play Store icon — separate from launcher) | ❌ Missing. Today `android:icon` points at `@drawable/launch` (a single JPEG in `mipmap-hdpi/`). Need Image Asset Studio to generate the full adaptive set + a 512×512 export for Play. |
| **Feature graphic** | required | 1024×500 PNG/JPG, no transparency | ❌ Missing. Use the manga style — speech-bubble logo, dark ink border, halftone hatching. Bangers/Russo One title. |
| **Phone screenshots** | required (≥ 2, max 8) | 16:9 to 9:16, short edge 320-3840px | ❌ Missing. See section 7 below for the suggested shot list. |
| **7" tablet screenshots** | optional | 16:9 to 9:16, short edge 320-3840px | Skip unless you want broader Play Console favorability. |
| **10" tablet screenshots** | optional | same | Skip — app isn't tablet-adaptive yet. |
| **Promo video** | optional | YouTube URL | Skip. |

### Adaptive launcher icon (separate from the 512×512 Play icon)

Right now `AndroidManifest.xml` has:
```xml
android:icon="@drawable/launch"
android:roundIcon="@drawable/launch"
```
`mipmap-mdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/` are empty;
`mipmap-anydpi-v26/` is empty. To fix:

1. **Android Studio → File → New → Image Asset → Launcher Icons (Adaptive and
   Legacy)**.
2. Foreground: your icon glyph (transparent PNG/SVG, 108×108 dp content area).
3. Background: solid `QuizColors.tint` (orange) or a halftone-ink swatch.
4. Generate — it populates all five density folders + `mipmap-anydpi-v26/ic_launcher.xml`.
5. In `AndroidManifest.xml`, swap `@drawable/launch` → `@mipmap/ic_launcher` for
   both `android:icon` and `android:roundIcon`.
6. `git rm app/src/main/res/mipmap-hdpi/launch.jpeg` and
   `git rm app/src/main/res/drawable/launch.xml` (or whatever variant exists).
7. Export the SAME foreground+background flattened to 512×512 PNG (no alpha) for
   the Play Console "App icon" field.

---

## 7 · Phone screenshot shot list

Recommended order (Play renders left-to-right; first 2 do most of the lifting).

1. **Home / mode picker** — `GameModeMenuScreen`. The 2×2 mode grid + daily
   hero + streak row. Caption: "Четыре режима, ежедневный вызов, серия".
2. **Quiz in progress** — `QuizScreen` mid-question with the blurred poster
   visible, timer running, hint bar at the bottom. Caption: "10 секунд на ответ".
3. **Result screen** — `ResultScreen` with a perfect run, "+N МОНЕТ", share
   button. Caption: "Награды за каждый трек".
4. **Daily challenge** — `DailyChallengeScreen` showing the countdown +
   live stats (place / players / solved). Caption: "Один трек на всех".
5. **Duel setup** — `DuelSetupScreen` with the 6-character code generated +
   share sheet. Caption: "Дуэль по коду".
6. **Leaderboard** — `LeaderboardScreen` top-30. Caption: "Глобальный рейтинг".
7. **Profile / achievements** — `ProfileScreen` with the streak calendar +
   unlocked badges. Caption: "Прогресс и достижения".
8. **Hint shop** — `HintShopScreen` with the three hint cards + coin balance.
   Caption: "Магазин подсказок".

### How to capture cleanly

- Use a Pixel 7 / Pixel 8 emulator (1080×2400) so the aspect ratio matches what
  Play renders best.
- `adb shell screencap -p /sdcard/01.png && adb pull /sdcard/01.png` (or use
  Android Studio Logcat → Screenshot button — it crops the status bar
  automatically).
- For consistent demo data:
  - Set `userPrefs.userName` to "Yers" before capturing.
  - Pre-seed `coins` to a roomy number (e.g. 1240) so the pill reads well.
  - Run a quick warm-up quiz so the streak shows a few days.
- If you want a flawless "magic moment" shot, run with `LocalA11y.reduceMotion =
  false` so animations bloom (konfetti on correct answer, score pulse).

If you'd like, I can write a Compose UI test under `androidTest/` that drives
the app to each of these states and captures a PNG to `app/build/screenshots/` —
say the word and I'll wire it.

---

## 8 · Content rating, target audience, data safety

Play Console will refuse the v5.0 release until each of these is current.

### Content rating questionnaire  (Policy → App content → Content ratings)
- Violence: **No**
- Sexual content: **No**
- Profanity: **No** (Russian song titles only; verify your dataset doesn't
  include 18+ insert songs)
- Drugs/alcohol/tobacco: **No**
- Gambling: **No** (rewarded ads + virtual coins are NOT gambling per Google's
  definition because the coins are non-cashable)
- User-generated content: **No** (player names are user-typed but not surfaced
  publicly beyond leaderboard/friends; consider declaring "Yes — but limited"
  if Google flags it)

Expected rating after submission: **PEGI 3 / ESRB Everyone / IARC 3+**.

### Target audience  (Policy → App content → Target audience)
- Target age: **13+** (because of ads — Google's "Designed for Families"
  program requires no rewarded ads for under-13).
- Children's API usage: **No**.

### Data safety  (Policy → App content → Data safety)

Declare these, then re-check `firestore.rules` and our code matches.

| Category | Collected? | Shared? | Optional? | Purpose |
|---|---|---|---|---|
| Personal info → Name | Yes (user-entered display name, ≤ 24 chars) | Yes (visible on leaderboard / to friends) | No | App functionality |
| App activity → App interactions | Yes (Firebase Analytics events) | No | Yes (no opt-out switch yet) | Analytics |
| Device or other IDs → Device ID | Yes (Firebase anon UID, AdMob ID) | Yes (AdMob) | No | Functionality + ads |
| App info & performance → Crash logs | Yes (Crashlytics) | No | No | Analytics |
| App info & performance → Diagnostics | Yes (perf + exit reasons) | No | No | Analytics |

Not collected: email, password, photos, location, contacts, calendar, audio
recordings, files. The "anonymous" leaderboard uid means user identity is not
recoverable from the data.

### Ads declaration  (Policy → App content → Ads)
- **Contains ads: Yes** (AdMob rewarded ads in the hint shop).
- Ad ID permission already in the manifest (auto-injected by play-services-ads).

### Policy: ads + COPPA  (Policy → App content → Ads → "Are your ads compliant…")
- "Are users under 13 in your target audience": **No** (we target 13+).
- "Have you implemented UMP / consent for EEA users": **No (pending)** — your
  next AdMob TODO is wiring the User Messaging Platform consent flow for
  GDPR/Switzerland/UK before serving to EEA users at scale.

---

## 9 · After-submission checklist

1. **Promote a closed test track first.** Internal testing (up to 100 testers
   by email) → Closed testing → Production. Each track lets you catch a
   region-specific bug before the public rollout.
2. **Use staged rollout for Production.** Start at 10 % for 24 h, then 50 %,
   then 100 %. If the Crashlytics dashboard spikes during the 10 % phase, you
   pause without affecting the existing v4 user base.
3. **Run pre-launch report.** Play Console auto-runs your APK on a fleet of
   real devices. It will catch the A03-class memory issues we've been chasing
   *before* real users see them — see the report after upload.
4. **Watch:**
   - Crashlytics for the new `Prior process exit: low_memory` non-fatals from
     `ExitReasonReporter`.
   - Firebase Analytics → DebugView for live event flow.
   - AdMob → Apps → kz.yers.quiz → eCPM / impressions over the first 48 h.
     New ad units serve test ads + no-fill for up to 48 h before real
     inventory ramps up — don't panic.
5. **Remove your dev device from AdMob test devices.** Play Console → AdMob
   account → Settings (gear) → Test devices — make sure no device IDs are
   pinned there for the production app.

---

## 10 · TL;DR — what I can hand to you vs. what you need to make

- **From me, ready to paste:** title options, short description, full
  description, release notes, English fallback copy, content-rating /
  data-safety / ads answers.
- **From you, in design tools / on a device:**
  - 512×512 Play icon PNG.
  - Adaptive launcher icon set via Image Asset Studio.
  - 1024×500 feature graphic.
  - 2-8 phone screenshots from the list in §7.
- **Optional from me:** Compose UI test that drives the app through the §7
  shot list and exports PNGs — ask if you want it.
