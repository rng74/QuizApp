package kz.yers.quiz.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    /**
     * v5: per-run correct/answered counts (real profile accuracy) and multi-track daily
     * attempts (correct count out of total tracks).
     */
    val M_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE run_history ADD COLUMN correct INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE run_history ADD COLUMN answered INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daily_attempt ADD COLUMN correctCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daily_attempt ADD COLUMN totalTracks INTEGER NOT NULL DEFAULT 1")
                // Old single-track attempts: a correct answer was 1 of 1.
                db.execSQL("UPDATE daily_attempt SET correctCount = correct")
            }
        }

    val ALL: Array<Migration> = arrayOf(M_4_5)
}
