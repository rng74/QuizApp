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
     * Deterministic single track for the Daily Challenge — same epochDay always picks the same
     * track, distinct anime (by `titleRu`) for a stable pool. Stub for the v2 backend endpoint.
     */
    suspend fun getDailyQuestion(epochDay: Long): QuizQuestion? {
        if (animeList.isEmpty()) loadAnimeData()
        val pool = animeList.filter { it.titleRu.isNotBlank() }.distinctBy { it.titleRu }
        if (pool.isEmpty()) return null
        val index = (epochDay.mod(pool.size.toLong())).toInt()
        val correct = pool[index]
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
