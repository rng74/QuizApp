package kz.yers.quiz.data.local

import androidx.room.migration.Migration

/**
 * Room migrations for [AppDatabase].
 *
 * Policy (v4.0 release onward): every entity change requires a real [Migration].
 * Bumping `@Database(version = …)` without adding a corresponding `M_x_y` here
 * and appending it to [ALL] will cause Room to throw at startup — by design.
 * The previous `fallbackToDestructiveMigration` was wiping all player data
 * (runs, streaks, friends, notifications, coins) on every schema bump; we
 * traded that silent wipe for a loud build-time reminder.
 *
 * When you add a new schema version:
 * 1. Bump `version` in [AppDatabase].
 * 2. Build once so KSP exports `schemas/.../<new>.json`. Commit that JSON.
 * 3. Add `val M_4_5 = object : Migration(4, 5) { … }` below.
 * 4. Append it to [ALL].
 * 5. Smoke-test by installing the new APK over an old v4 build (`adb install -r`).
 */
object Migrations {
    // No migrations yet — v4 is the baseline. Example for the next bump:
    //
    // val M_4_5 = object : Migration(4, 5) {
    //     override fun migrate(db: SupportSQLiteDatabase) {
    //         db.execSQL("ALTER TABLE friend ADD COLUMN avatar_url TEXT")
    //     }
    // }

    val ALL: Array<Migration> = emptyArray()
}
