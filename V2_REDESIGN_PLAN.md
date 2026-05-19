# V2 Redesign — Manga Direction Roadmap

This document captures the full v2 vision (from the Claude Design handoff bundle
*Anime Quiz Design System*) and the path from the **current** code to it. The
v2 main menu + navigation contract have been implemented; **everything marked
STUB below is a placeholder with a concrete path to "real" here.**

## Navigation contract

Three screen archetypes, each a `Scaffold` variant. Implemented in
`ui/composable/scaffold/MainScaffold.kt`.

| Archetype | Top bar | Bottom nav | Screens |
|---|---|---|---|
| **Root tab** | Coins · Brand · Notif/Settings · *no back* | **shown** | Home, Дневной, Дуэль, Топ, Профиль |
| **Pushed** | ← Back · Title · Actions | hidden | Settings, Onboarding |
| **Immersive** | × Close · Progress · Score | hidden | Quiz, Loading, Result, Duel handoff/result |

`AppState` drives the archetype. `isRootTab()` → bottom nav;
`scaffoldOwnsTopBar()` → scaffold renders the root top bar (currently **Home**
and **Leaderboard** only).

## Done in this pass

- `MainScaffold` shell: root top app bar (coins pill, brand, notif bell +
  badge, settings) + pinned manga bottom nav with the ink "splotch" active
  indicator; chrome hidden on pushed/immersive screens.
- `GameModeMenuScreen` rebuilt pixel-faithful to `v2-main-menu.html`: daily
  hero (live countdown to local midnight), streak row (real streak days), 2×2
  mode grid with per-mode records + "НОВЫЙ!" ribbon, duel CTA.
- `LeaderboardScreen` ("Топ") — new route/`AppState`, static stub.
- New `MangaIcons`: `Home, Coin, Daily, Versus, Leaderboard, Bell`.
- `UserPrefs.coins` (DataStore, default 240) + ViewModel `coins`,
  `streakDays`, `bestScoreByMode`, `recordModeJustSet`, `openLeaderboard()`.

## Stubs → how to make them real

### 1. Coins economy + Hint Shop + AdMob — IMPLEMENTED
Earning: `finishRun()` awards `score / COINS_PER_SCORE` (5) per non-duel run
plus a `STREAK_BONUS_COINS` (50) bonus every 7th streak day (returned by
`updateStreak()`); persisted via `UserPrefs.setCoins`, shown as "+N МОНЕТ"
on the Result screen, and the top-bar coins pill reflects it.
Spending: the pill opens `HintShopScreen` (`AppState.Shop`, Pushed
archetype — back+title, no bottom nav). Three hint cards (50:50 / Буква /
Пропуск, prices on `HintType.coinPrice` = 50/40/80) with **Купить** (deduct
coins → `HintInventory` +1, persisted) and **Реклама** (rewarded ad → +1).
AdMob: real `play-services-ads` SDK, `MobileAds.initialize` in
`MyApplication`, manifest `APPLICATION_ID`, `RewardedAdEffect` loads + shows
a real `RewardedAd`; reward → hint, dismiss/no-fill → graceful no-op (dim
spinner scrim, no crash). The old simulated `RewardedAdDialog` was deleted.
**Remaining (the documented swap):** all AdMob ids are Google's official
**sample/test** ids (`ca-app-pub-3940256099942544~3347511713` /
`…/5224354917`). Before release create a real AdMob app + rewarded unit and
swap the manifest `APPLICATION_ID` and `TEST_REWARDED_UNIT` in
`RewardedAdEffect.kt`. Also: no consent/UMP (GDPR) flow yet; coin balance is
client-only (not synced to Firestore — fine, it's not competitive).

### 2. Notifications — STUB
Bell badge is the constant `2` (`notifCount = 2` in `AppNav.kt`); the bell
button is a no-op.
**Real:** FCM channel for duel results / daily reminders; an inbox screen
(Pushed archetype); badge = unread count from a `Notification` table.

### 3. Leaderboard — IMPLEMENTED (all-time)
Firebase-backed: anonymous Auth + Firestore `leaderboard/{uid}` (best solo
score per player). `LeaderboardRepository.load()` fetches top 30 +
aggregation-`count()` rank/total; `submitScore()` upserts monotonically in a
transaction after each non-daily run. `LeaderboardScreen` renders
Loading / Loaded / Offline (retry). Security in `firestore.rules`.
**Remaining:** period filters (День/Неделя/Месяц) are visual only — only
*Все время* is backed (single best-score doc). Time-bucketed docs or a
`scores/{uid}/{period}` sub-doc model would make the other chips real.

### 4. Daily hero live data — IMPLEMENTED
Countdown to next midnight is real. The track is already deterministic per
epochDay client-side (`AnimeRepository.getDailyQuestion` — same track for
everyone, no server needed). Shared stats are now Firebase-backed:
`daily/{epochDay}/attempts/{uid}` = { score, correct, updatedAt }.
`DailyStatsRepository.submitAttempt()` upserts (monotonic) after each daily
run; `loadStats()` returns players / solved-% / your-rank via `count()`
aggregations. Surfaced in `DailyState.liveStats`, rendered in the Daily
screen `StatsRow` (место/играют/угадали) and the Home hero live line
("N играют сегодня · ваше место #R"). Graceful when offline/unprovisioned:
panels show "—", hero shows "Сыграй первым сегодня". Security in
`firestore.rules` (`daily/{day}/attempts/{uid}` block, bounded + monotonic).
The "2× очки" multiplier already exists in `submitAnswer()`.
**Remaining:** the "топ-100 получают монеты" promise needs the coin economy
(Stub #1); the daily *track* is still locally deterministic, not curated —
authentic curation waits on the content pipeline (priority #8).

### 5. Per-mode records / "НОВЫЙ!" ribbon — PARTIAL
`bestScoreByMode` is derived from `RunHistoryDao.bestScoreForMode()` (real).
`recordModeJustSet` is set in `finishRun()` for the session; cleared on the
next `startQuiz()`. Edge cases (daily/duel) are excluded by design.
**Hardening:** persist "last record mode" so the ribbon survives process death
until the player re-enters that mode.

### 6. Top-bar unification across all screens — PARTIAL
`MainScaffold` now owns three archetypes: **Root** (brand bar, Home/Leaderboard
+ bottom nav), **Pushed** (back+title bar, Settings, no bottom nav), and
**Immersive** (no scaffold chrome, Quiz owns its × bar). `SettingsScreen`'s
private app bar was removed and replaced by the scaffold Pushed bar.
**Remaining:** Daily/Duel-setup/Profile are root tabs that intentionally keep
their bespoke v2-manga headers (dark gradient avatar header / daily hero bar —
not the "coins · brand" pattern), so they are *not* candidates for the root
brand bar. Duel-setup's "ОТМЕНА" is an in-form action, not chrome. If a future
design unifies these, feed their titles/actions through the scaffold; for now
the contract is satisfied.

### 7. Bottom-nav back-stack semantics — PARTIAL
System-back semantics are now correct via `BackHandler` in `AppNavHost`:
non-Home root tabs + Settings → Home; Home → exit app; Quiz → forfeit-confirm
dialog (also bound to system back inside `QuizScreen`); Loading/Result/Duel →
sensible parent. Navigation still uses `popUpTo(0){inclusive=true}` so there
are no *independent per-tab* back stacks.
**Remaining:** nested `NavHost` per tab (or `rememberSaveable` per-tab stacks)
if deep per-tab history is ever needed; not required for the current contract.

### 8. Icons / hero art — PARTIAL
Nav + chrome icons are ported `MangaIcons` vectors. Mode-card art still uses
the existing PNGs (`easy_icon`/`normal_icon`/`random_icon`/`hardcore_icon`).
The "ドン!!" SFX uses Bangers (Permanent Marker is not bundled).
**Optional:** port `mode-*.svg` / `hero-daily.svg` to vectors; add Permanent
Marker to `res/font/` for authentic SFX lettering.

### 9. Async Duel — IMPLEMENTED (no cross-app push)
Local hot-seat duel was **replaced** by a code-based async duel on the same
Firebase Spark model. `DuelRepository`: anon auth + `duels/{code}` doc —
`create()` allocates a unique 6-char code (create-if-absent transaction) and a
random `seed`; `join()` claims the guest slot (handles not-found / room-full);
`submitScore()` writes only this device's score, once; `listen()` is a
`callbackFlow` Firestore snapshot listener. The track is
`AnimeRepository.getSeededQuestions(seed,…)` — `Random(seed)` makes the
question *and* option order identical on both devices with nothing uploaded
(the `info.json` asset is the shared source of truth). Flow: Setup
(create / join-by-code) → Lobby (big code + system share-sheet + waiting) →
solo round → Result (live "ждём соперника…" → winner, updated by the
listener). Security in `firestore.rules` (`duels/{code}`: host-owned create,
one-time guest join, each side writes only its own score, field-diff bounded).
Degrades gracefully: offline/unprovisioned → error message on Setup, no crash.
**Remaining (the documented gap):** a push when the opponent finishes while
you're *not* on the result screen is impossible on Spark — sending FCM needs a
server (Admin SDK / Cloud Functions / HTTP v1 + service account). The
in-app realtime listener covers the both-present case; the offline-notify case
needs Blaze + a Function (or any tiny server) and is deliberately out of scope.
Also: no room TTL/cleanup (stale `duels` docs accumulate — a future offline
sweep script or a Blaze scheduled Function); rejoin after process death isn't
restored (the code/role aren't persisted).

### 10. Share v2 — IMPLEMENTED (store link, not a deep link)
`utils/ShareText.kt`: pure `emojiGridFor(correct,total)` (🟩 per correct, one
🟥 for the sudden-death miss, omitted on a perfect run, wrapped 5/row,
spoiler-safe — never leaks titles) + `buildScoreShareText(...)` (header /
grid / "Счёт:" / "Побей мой результат 👇" / store URL) + `shareText()`
plain-text `ACTION_SEND`. `QuizAppViewModel` tracks `lastRunCorrect` /
`lastRunTotal` in `finishRun()`; `ResultScreen`/`ShareResultDialog` show the
grid and a 3-way chooser (Отмена / **Текстом** = Wordle block / **Картинкой**
= the existing bitmap card). Verified: real system share sheet with the full
challenge message.
**Remaining:** the "challenge link" is the Play Store URL, not a deep link
into a *pre-seeded* duel/track — true challenge links need Android App Links
(a verified domain = hosting) or a Firebase Dynamic-Links-style resolver, both
out of scope under the no-hosting constraint. `emojiGridFor` is pure and
unit-test-ready (no test added yet — see cross-cutting "unit tests").

### 11. Accessibility — IMPLEMENTED
The four a11y flags (`A11yState`, persisted in `UserPrefs`, provided via
`LocalA11y` in `Theme.kt`) are all live:
- **Color-blind pairs:** `successColor()`/`errorColor()` swap green→blue and
  red→orange when `colorBlindSafe`; used by the quiz answer surfaces and the
  Settings `PreviewRow`.
- **Icon redundancy:** `OptionTile` carries a `✓`/`✕` glyph and the Settings
  `FeedbackChip` echoes it — correctness never relies on hue alone.
- **Reduce-motion:** already gated in `MangaButton`, `MangaSwitchRow`,
  `QuizScreen` (konfetti, shake, score pulse); this pass extended it to the
  `MainScaffold` chrome (top bars + bottom nav now snap instead of
  expand/shrink on every navigation), `BlurredImage` (poster blur snaps), and
  `OptionToggle` (segmented indicator snaps).
- **Larger-text / dyslexia:** `Theme.kt` scales body text ×1.2 and swaps to
  `DyslexiaFriendlyFamily`; the Settings ДОСТУПНОСТЬ section has a live
  `PreviewRow` (correct/wrong chips) that re-renders under all three toggles.
**Remaining (cross-cutting, not blocking):** no automated checks
(contrast-ratio / TalkBack content-description audit); `contentDescription`
coverage on decorative-vs-meaningful icons not formally swept.

## Larger v2 scope (from the design README BUILD PLAN, reconciled)

Already present in the codebase: Navigation-Compose, Room
(`RunHistory`/`DailyAttempt`), DataStore (`UserPrefs`), manga primitive
library, Onboarding, Daily Challenge, Duel (async/code-based — Stub #9),
Profile + achievements, hints + rewarded-ad scaffolding, accessibility flags.

Remaining bets, priority order:
1. ~~**Top-bar unification + tab back-stacks** (items 6–7)~~ — DONE.
2. **Backend service** — DECIDED: **Firebase, Spark (free, no card)**.
   Client SDK + Firestore + Security Rules + Auth(anon) + FCM + Analytics;
   **no Cloud Functions** (would force Blaze). Tradeoff: scores are
   client-asserted, bounded by `firestore.rules`. Content pipeline runs as an
   offline script, not a cron. *Leaderboard slice shipped (see Stub #3).*
   Next: Daily online + Duel + profile/friends on the same model.
3. ~~**Daily Challenge online** — live stats (item 4)~~ — DONE (shared
   stats shipped on the Firebase model; deterministic track stands in until
   the content pipeline curates one, priority #8).
4. ~~**Async Duel** — 6-char share code + shared track + result sync~~ —
   DONE (replaced hot-seat; realtime listener instead of FCM — see Stub #9.
   Cross-app push remains out of scope on Spark).
5. ~~**Hint Shop + coin economy + AdMob**~~ — DONE (Stub #1; real AdMob on
   test ids — documented swap. Notifications/FCM, item #2, still open).
6. ~~**Share v2** — Wordle-style emoji grid + "побей мой результат"~~ —
   DONE (Stub #10; challenge "link" is the store URL — true deep links need
   hosting, out of scope).
7. ~~**Accessibility completion**~~ — DONE (Stub #11; reduce-motion coverage
   extended to the scaffold chrome + blur + segmented toggle).
8. **Content pipeline** — PARTIAL. The in-app **90-day no-repeat daily
   picker** is shipped: `pickDailyTitle()` in `repo/AnimeRepository.kt` is a
   pure function that curates a quality pool (non-blank `titleRu`, `rating >
   7.0`, OP) from the existing `info.json`, fixed-seed-shuffles it, and
   indexes by `epochDay` so each anime recurs exactly every `pool.size`
   (≫90) days — deterministic and identical on every device (required by the
   Firestore daily-stats model). Falls back to the broad distinct pool if
   curation yields < 90. Invariants covered by `app/src/test/.../
   DailyPickerTest.kt`. **Remaining:** the AnimeThemes.moe puller + review
   queue stay out of scope — the data source is unchanged (anison.fm, RU
   titles), curation is the in-app heuristic, not an offline-curated feed.

Cross-cutting: RU-only strings, telemetry events, `Result<T>` network error
states with manga retry panel, offline mode for solo modes, unit tests
(`evaluateAchievements`, streak math, future `emojiGridFor`), CI lint+tests.

## Firebase setup — one-time manual steps (console)

The project already has `google-services.json` + Analytics/Crashlytics. To
activate the leaderboard, in the Firebase console for this project:

1. **Authentication → Sign-in method → Anonymous → Enable.**
2. **Firestore Database → Create database** (Production mode, nearest region).
3. **Deploy rules:** paste `firestore.rules` into Firestore → Rules (or
   `firebase deploy --only firestore:rules`).
4. (Auto) Firestore prompts to create the composite/single-field indexes the
   first time the `orderBy(score)` + `count()` queries run — accept them.

Until 1–3 are done the app degrades gracefully: the Топ tab shows the
"Топ недоступен" offline panel with a retry button; runs still work and
`submitScore` fails silently.

## Key files

- Shell: `ui/composable/scaffold/MainScaffold.kt`
- Home: `ui/composable/screen/GameModeMenuScreen.kt`
- Leaderboard: `ui/composable/screen/LeaderboardScreen.kt`,
  `data/remote/LeaderboardRepository.kt`, `model/Leaderboard.kt`,
  `firestore.rules`
- Daily online: `data/remote/DailyStatsRepository.kt`,
  `model/DailyState.kt` (`DailyLiveStats`),
  `ui/composable/screen/DailyChallengeScreen.kt`,
  `ui/composable/screen/GameModeMenuScreen.kt`, `firestore.rules`
- Async duel: `data/remote/DuelRepository.kt`, `model/DuelState.kt`,
  `repo/AnimeRepository.kt` (`getSeededQuestions`),
  `ui/composable/screen/DuelSetupScreen.kt` / `DuelHandoffScreen.kt` /
  `DuelResultScreen.kt`, `firestore.rules`
- Shop / coins / ads: `ui/composable/screen/HintShopScreen.kt`,
  `ui/composable/hints/RewardedAdEffect.kt`, `model/HintType.kt`
  (`coinPrice`), `MyApplication.kt`, `AndroidManifest.xml`,
  `QuizAppViewModel.kt` (`buyHintWithCoins`/coin award)
- Router: `navigation/AppNav.kt`, `navigation/Routes.kt`, `model/AppState.kt`
- State: `QuizAppViewModel.kt`, `data/prefs/UserPrefs.kt`
- Share v2: `utils/ShareText.kt`, `ui/composable/share/ShareResultDialog.kt`,
  `ui/composable/screen/ResultScreen.kt`, `QuizAppViewModel.kt`
  (`lastRunCorrect`/`lastRunTotal`)
- Tokens/icons: `ui/theme/Tokens.kt`, `ui/composable/manga/MangaIcons.kt`
