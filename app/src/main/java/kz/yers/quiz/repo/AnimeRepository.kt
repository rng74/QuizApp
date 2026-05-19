package kz.yers.quiz.repo

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.yers.quiz.HIGH_SCORE
import kz.yers.quiz.model.AnimeInfo
import kz.yers.quiz.model.QuizQuestion
import kotlin.random.Random

class AnimeRepository(
    private val context: Context,
    private val gson: Gson,
    private val sharedPreferences: SharedPreferences,
) {
    private var animeList: List<AnimeInfo> = emptyList()

    suspend fun getRandomizedQuestions(numberOfQuestions: Int): List<QuizQuestion> {
        if (animeList.isEmpty()) {
            loadAnimeData()
        }

        return generateQuizQuestions(animeList, numberOfQuestions)
    }

    suspend fun getRandomizedQuestionsGt(
        numberOfQuestions: Int,
        rating: Float,
    ): List<QuizQuestion> {
        if (animeList.isEmpty()) {
            loadAnimeData()
        }

        return generateQuizQuestions(animeList, numberOfQuestions, rating, true)
    }

    suspend fun getRandomizedQuestionsLte(
        numberOfQuestions: Int,
        rating: Float,
    ): List<QuizQuestion> {
        if (animeList.isEmpty()) {
            loadAnimeData()
        }

        return generateQuizQuestions(animeList, numberOfQuestions, rating, false)
    }

    /**
     * Deterministic single track for the Daily Challenge.
     *
     * The track for a given [epochDay] is identical on every device/run (no server needed) — the
     * Firestore daily-stats model assumes one shared track per day. The pool is heuristically
     * curated (recognizable openings, [DAILY_MIN_RATING]+), then put through a fixed-seed
     * permutation so the order is non-sequential to players. Indexing that permutation by
     * [epochDay] makes each anime recur exactly every `pool.size` days, so as long as the
     * curated pool has at least [DAILY_NO_REPEAT_WINDOW] entries no anime can repeat within a
     * 90-day window. Falls back to the broad distinct pool if curation yields too few.
     *
     * Caveat: the epochDay→track map is stable only while `info.json` and the curation predicate
     * are unchanged. A future content-pipeline regen would shift the schedule; clients on
     * different app versions could then see different daily tracks for the same day (already true
     * before this change, and acceptable).
     */
    suspend fun getDailyQuestion(epochDay: Long): QuizQuestion? {
        if (animeList.isEmpty()) loadAnimeData()
        val correct = pickDailyTitle(animeList, epochDay) ?: return null
        val options = generateOptions(correct)
        return QuizQuestion(correctAnswer = correct, options = options)
    }

    /**
     * Deterministic track for an async duel. The same [seed] produces the identical question
     * order *and* option order on every device (the `info.json` asset is the shared source of
     * truth), so two players answer exactly the same quiz without uploading any questions.
     */
    suspend fun getSeededQuestions(
        seed: Long,
        count: Int,
        minRatingGt: Float,
    ): List<QuizQuestion> {
        if (animeList.isEmpty()) loadAnimeData()
        val rng = Random(seed)
        val pool =
            animeList
                .filter { it.rating > minRatingGt && it.albumName.endsWith("OP") }
                .distinctBy { it.titleRu }
                .shuffled(rng)
        val total = minOf(count, pool.size)
        return (0 until total).map { i ->
            val correct = pool[i]
            QuizQuestion(correctAnswer = correct, options = seededOptions(correct, rng))
        }
    }

    private fun seededOptions(
        correct: AnimeInfo,
        rng: Random,
    ): List<String> {
        val distractors =
            animeList
                .filter { it.titleRu != correct.titleRu }
                .map { it.titleRu }
                .distinct()
                .shuffled(rng)
                .take(3)
        return (distractors + correct.titleRu).shuffled(rng)
    }

    private suspend fun loadAnimeData() {
        withContext(Dispatchers.IO) {
            val jsonString =
                context.assets.open("info.json")
                    .bufferedReader()
                    .use { it.readText() }

            val listType = object : TypeToken<List<AnimeInfo>>() {}.type
            animeList = gson.fromJson(jsonString, listType)
        }
    }

    private fun generateQuizQuestions(
        animeList: List<AnimeInfo>,
        numberOfQuestions: Int,
        rating: Float? = null,
        isGreater: Boolean? = null,
    ): List<QuizQuestion> {
        val shuffledAnimeList =
            animeList.filter {
                (
                    if (rating != null && isGreater != null) {
                        if (isGreater) {
                            it.rating > rating && it.albumName.endsWith("OP")
                        } else {
                            it.rating <= rating
                        }
                    } else {
                        true
                    }
                )
            }.shuffled().distinctBy { it.titleRu }
        val totalQuestions = minOf(numberOfQuestions, shuffledAnimeList.size)
        val questions = mutableListOf<QuizQuestion>()

        for (i in 0 until totalQuestions) {
            val correctAnime = shuffledAnimeList[i]
            val options = generateOptions(correctAnime)
            questions.add(QuizQuestion(correctAnswer = correctAnime, options = options))
        }

        return questions
    }

    private fun generateOptions(
        correctAnime: AnimeInfo,
        rating: Float? = null,
        isGreater: Boolean? = null,
    ): List<String> {
        val allTitles =
            animeList.filter {
                (
                    if (rating != null && isGreater != null) {
                        if (isGreater) {
                            it.rating > rating
                        } else {
                            it.rating <= rating
                        }
                    } else {
                        true
                    }
                ) && it.titleRu != correctAnime.titleRu
            }.map { it.titleRu }.distinct()
        val incorrectOptions = allTitles.shuffled().take(3)
        val options = incorrectOptions + correctAnime.titleRu
        return options.shuffled()
    }

    fun setHighScore(score: Int) {
        val highScore = getHighScore()
        if (score > highScore) {
            sharedPreferences.edit {
                putInt(HIGH_SCORE, score)
            }
        }
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt(HIGH_SCORE, 0)
    }

    fun clearHighScore() {
        sharedPreferences.edit { remove(HIGH_SCORE) }
    }
}

private const val DAILY_MIN_RATING = 7.0f
private const val DAILY_SHUFFLE_SEED = 0x5DA17_4D41L
private const val DAILY_NO_REPEAT_WINDOW = 90

/**
 * Pure daily picker (no I/O, no un-seeded randomness) so the 90-day no-repeat invariant is
 * unit-testable. Same [list] + [epochDay] always yields the same [AnimeInfo] on every device.
 *
 * The pool is heuristically curated (recognizable openings, [DAILY_MIN_RATING]+), then put
 * through a fixed-seed permutation so the order is non-sequential. Indexing that permutation by
 * [epochDay] makes each anime recur exactly every `pool.size` days, so a curated pool of at
 * least [DAILY_NO_REPEAT_WINDOW] entries can never repeat an anime within a 90-day window.
 * Falls back to the broad distinct pool if curation yields too few.
 */
internal fun pickDailyTitle(
    list: List<AnimeInfo>,
    epochDay: Long,
): AnimeInfo? {
    val curated =
        list
            .filter {
                it.titleRu.isNotBlank() &&
                    it.rating > DAILY_MIN_RATING &&
                    it.albumName.endsWith("OP")
            }
            .distinctBy { it.titleRu }
            .sortedBy { it.titleRu }
    val pool =
        if (curated.size >= DAILY_NO_REPEAT_WINDOW) {
            curated.shuffled(Random(DAILY_SHUFFLE_SEED))
        } else {
            list.filter { it.titleRu.isNotBlank() }.distinctBy { it.titleRu }
        }
    if (pool.isEmpty()) return null
    val index = (epochDay.mod(pool.size.toLong())).toInt()
    return pool[index]
}
