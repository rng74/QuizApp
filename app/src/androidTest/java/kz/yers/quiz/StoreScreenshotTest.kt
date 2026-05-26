package kz.yers.quiz

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kz.yers.quiz.data.local.dao.FriendDao
import kz.yers.quiz.data.local.dao.NotificationDao
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.FriendEntity
import kz.yers.quiz.data.local.entity.NotificationEntity
import kz.yers.quiz.data.local.entity.RunHistoryEntity
import kz.yers.quiz.data.prefs.UserPrefs
import kz.yers.quiz.model.AppState
import kz.yers.quiz.model.GameMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/**
 * Drives the running app through each Play-Store-worthy screen and writes a
 * PNG to the app's external-files dir for each one. Designed to be run on a
 * Pixel 8 AVD (or similar 1080×2400 emulator) so the aspect ratio matches what
 * Play Store wants without any post-crop step.
 *
 * Run:
 *   ./gradlew :app:connectedDebugAndroidTest \
 *     --tests kz.yers.quiz.StoreScreenshotTest
 *
 * Pull the results:
 *   adb pull /sdcard/Android/data/kz.yers.quiz/files/store_screenshots ./screenshots
 *
 * The PNGs are numbered to match STORE_LISTING.md §7 — paste them into Play
 * Console in that order.
 *
 * NOTE: this is best-effort. Network-dependent screens (Leaderboard, Daily live
 * stats) reflect whatever state the connected Firebase project is in — if
 * Firestore isn't provisioned they'll show the Offline panel. The "Quiz mid-run"
 * shot needs network to load the audio track and the poster.
 *
 * Each capture is wrapped in `runCatching` so one flaky screen doesn't lose
 * the others — check the test output and re-run as needed.
 */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val outDir: File by lazy {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        File(ctx.getExternalFilesDir(null), "store_screenshots").apply { mkdirs() }
    }

    @Test
    fun captureStoreScreens() {
        // 0. Let onboarding settle. completeOnboarding() is idempotent and fast;
        //    calling it now means the test doesn't have to drive the 3 slides.
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.activity.viewModel.onboardingResolved.value
        }
        runOnMain { composeRule.activity.viewModel.completeOnboarding() }
        composeRule.waitForIdle()

        // 1. Seed demo data BEFORE Home renders so the streak row + coins pill
        //    look populated in the screenshot. Direct Koin reads — we want the
        //    same singletons MyApplication wired up.
        seedDemoData()

        // 2. Give the menu refresh a beat to read the seeded values into the VM.
        runOnMain { composeRule.activity.viewModel.backToMenu() }
        composeRule.waitForIdle()
        Thread.sleep(800)
        capture("01_home")

        // 3. Daily — has its own header and uses Firestore for live stats.
        runOnMain { composeRule.activity.viewModel.openDaily() }
        composeRule.waitForIdle()
        Thread.sleep(800)
        capture("02_daily")

        // 4. Duel setup — pre-form state shows "Create / Join" cards.
        runOnMain { composeRule.activity.viewModel.openDuelSetup() }
        composeRule.waitForIdle()
        Thread.sleep(400)
        capture("03_duel_setup")

        // 5. Leaderboard — Loading → Loaded if Firestore is provisioned, else
        //    Offline panel. Either way it's a real screenshot.
        runOnMain { composeRule.activity.viewModel.openLeaderboard() }
        composeRule.waitForIdle()
        Thread.sleep(2_500) // give Firestore RTT time to settle.
        capture("04_leaderboard")

        // 6. Profile — needs the seeded RunHistory to look good.
        runOnMain { composeRule.activity.viewModel.openProfile() }
        composeRule.waitForIdle()
        Thread.sleep(800)
        capture("05_profile")

        // 7. Hint shop — three hint cards + coin balance from seedDemoData.
        runOnMain { composeRule.activity.viewModel.openShop() }
        composeRule.waitForIdle()
        Thread.sleep(400)
        capture("06_shop")

        // 8. Notifications inbox — populated from seedDemoData.
        runOnMain { composeRule.activity.viewModel.openNotifications() }
        composeRule.waitForIdle()
        Thread.sleep(600)
        capture("07_notifications")

        // 9. Settings — every toggle visible at default off-state.
        runOnMain { composeRule.activity.viewModel.openSettings() }
        composeRule.waitForIdle()
        Thread.sleep(400)
        capture("08_settings")

        // 10. Quiz in progress — best-effort: starting a quiz triggers a network
        //     load of the question pool and an ExoPlayer track. Allow several
        //     seconds for the poster to start blurring and the timer to begin.
        runOnMain { composeRule.activity.viewModel.backToMenu() }
        composeRule.waitForIdle()
        runOnMain { composeRule.activity.viewModel.startQuiz(GameMode.NORMAL) }
        val gotIntoQuiz =
            runCatching {
                composeRule.waitUntil(timeoutMillis = 20_000) {
                    composeRule.activity.viewModel.appState.value is AppState.Quiz
                }
                true
            }.getOrDefault(false)
        if (gotIntoQuiz) {
            // Wait a beat so the poster blur animation has applied + the timer
            // ring has started ticking down — both more photogenic than the
            // first frame.
            Thread.sleep(2_500)
            capture("09_quiz")
            // Forfeit to clean state.
            runOnMain { composeRule.activity.viewModel.abortQuiz() }
        }
    }

    private fun runOnMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    }

    private fun capture(name: String) {
        runCatching {
            composeRule.waitForIdle()
            val image = composeRule.onRoot().captureToImage().asAndroidBitmap()
            val file = File(outDir, "$name.png")
            file.outputStream().use { stream ->
                image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            println("StoreScreenshotTest: wrote ${file.absolutePath} (${image.width}x${image.height})")
        }.onFailure { println("StoreScreenshotTest: $name failed — ${it.message}") }
    }

    /**
     * Seed the runtime state that the screenshots care about. Writes directly to
     * the same Koin singletons MyApplication wired up — UserPrefs (DataStore) and
     * the Room DAOs — so the next `backToMenu` / `openProfile` refresh shows it.
     */
    private fun seedDemoData() {
        runCatching {
            val koin = GlobalContext.get()
            val userPrefs: UserPrefs = koin.get()
            val runHistoryDao: RunHistoryDao = koin.get()
            val friendDao: FriendDao = koin.get()
            val notificationDao: NotificationDao = koin.get()

            runBlocking {
                userPrefs.setUserName("Yers")
                userPrefs.setCoins(1240)
                val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
                userPrefs.setStreak(currentStreakDays = 5, lastPlayedEpochDay = today)
                userPrefs.setHintsFiftyFifty(3)
                userPrefs.setHintsReveal(2)
                userPrefs.setHintsSkip(1)

                // A spread of run history so Profile shows accuracy + recent
                // entries + level progression.
                val now = System.currentTimeMillis()
                listOf(
                    Triple(GameMode.NORMAL.name, 215, today),
                    Triple(GameMode.EASY.name, 178, today - 1),
                    Triple(GameMode.NORMAL.name, 240, today - 2),
                    Triple(GameMode.RANDOM.name, 132, today - 3),
                    Triple(GameMode.NORMAL.name, 280, today - 4),
                ).forEachIndexed { i, (mode, score, day) ->
                    runHistoryDao.insert(
                        RunHistoryEntity(
                            mode = mode,
                            score = score,
                            durationMs = 90_000L + i * 5_000L,
                            dateEpochDay = day,
                        ),
                    )
                }

                // A friend so the Friends button has something to point at.
                friendDao.upsert(FriendEntity(uid = "demo-friend-uid-1", name = "Akira"))
                friendDao.upsert(FriendEntity(uid = "demo-friend-uid-2", name = "Mai"))

                // A couple of inbox rows so Notifications screen isn't empty.
                notificationDao.insert(
                    NotificationEntity(
                        type = "NEW_RECORD",
                        title = "Новый рекорд! 🏆",
                        body = "Ты набрал 280 очков — это твой лучший результат.",
                        createdAt = now - 60_000L,
                        read = false,
                    ),
                )
                notificationDao.insert(
                    NotificationEntity(
                        type = "STREAK_MILESTONE",
                        title = "Серия 7 дней! 🔥",
                        body = "Бонус +50 монет за серию. Так держать!",
                        createdAt = now - 5 * 60 * 60 * 1000L,
                        read = false,
                    ),
                )
            }
        }
    }
}
