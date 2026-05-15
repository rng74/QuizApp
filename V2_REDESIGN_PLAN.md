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

### 1. Coins economy — STUB
`UserPrefs.coins` defaults to 240; the coins pill is read-only and the shop
tap is a no-op (`onOpenShop = {}` in `AppNav.kt`).
**Real:** award `score / 5` per run + daily/weekly bonuses in `finishRun()` /
`updateStreak()`; build the Hint Shop screen (`v2-extras.html` slide 07) —
4 hint cards with `Купить` (deduct coins) + `Бесплатно за рекламу` (AdMob
`RewardedAd`); wire `onOpenShop` to it; spend coins on `HintInventory`.

### 2. Notifications — STUB
Bell badge is the constant `2` (`notifCount = 2` in `AppNav.kt`); the bell
button is a no-op.
**Real:** FCM channel for duel results / daily reminders; an inbox screen
(Pushed archetype); badge = unread count from a `Notification` table.

### 3. Leaderboard — STUB
`LeaderboardScreen` renders hardcoded sample rows; period chips are inert; no
search icon in its top bar.
**Real:** backend `GET /v1/leaderboard?period=&mode=&limit=` →
ranked rows; player's real rank/gap; period + mode filters; pull-to-refresh;
offline banner. (See design README Phase 6 endpoint contract.)

### 4. Daily hero live data — PARTIAL
Countdown to next midnight is **real**. "847 играют сейчас · ваше место #23"
is a static literal.
**Real:** `GET /v1/daily` returns live player count + the user's standing;
the "2× очки" multiplier already exists in `submitAnswer()`.

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

## Larger v2 scope (from the design README BUILD PLAN, reconciled)

Already present in the codebase: Navigation-Compose, Room
(`RunHistory`/`DailyAttempt`), DataStore (`UserPrefs`), manga primitive
library, Onboarding, Daily Challenge, Duel (local hot-seat), Profile +
achievements, hints + rewarded-ad scaffolding, accessibility flags.

Remaining bets, priority order:
1. **Top-bar unification + tab back-stacks** (items 6–7) — finish the
   navigation contract everywhere.
2. **Backend service** (Firebase or Ktor — decide w/ PM): auth, daily,
   leaderboard, duel, profile, friends endpoints.
3. **Daily Challenge online** — curated track per UTC day + live stats
   (item 4).
4. **Async Duel** — 6-char share code, server-fixed track list, FCM result
   push (current Duel is local hot-seat only).
5. **Hint Shop + coin economy + AdMob** (items 1–2).
6. **Share v2** — Wordle-style emoji grid (`🟩🟥` spoiler-safe) +
   "побей мой результат" challenge link (`v2-extras.html` slide 08).
7. **Accessibility completion** — verify color-blind pairs + icon redundancy,
   reduce-motion coverage, larger-text/dyslexia preview row.
8. **Content pipeline** — AnimeThemes.moe puller + review queue + 90-day
   no-repeat daily picker.

Cross-cutting: RU-only strings, telemetry events, `Result<T>` network error
states with manga retry panel, offline mode for solo modes, unit tests
(`evaluateAchievements`, streak math, future `emojiGridFor`), CI lint+tests.

## Key files

- Shell: `ui/composable/scaffold/MainScaffold.kt`
- Home: `ui/composable/screen/GameModeMenuScreen.kt`
- Leaderboard stub: `ui/composable/screen/LeaderboardScreen.kt`
- Router: `navigation/AppNav.kt`, `navigation/Routes.kt`, `model/AppState.kt`
- State: `QuizAppViewModel.kt`, `data/prefs/UserPrefs.kt`
- Tokens/icons: `ui/theme/Tokens.kt`, `ui/composable/manga/MangaIcons.kt`
