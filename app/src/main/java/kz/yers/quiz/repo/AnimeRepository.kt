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

class AnimeRepository(
    private val context: Context,
    private val gson: Gson,
    private val sharedPreferences: SharedPreferences
) {

    private var animeList: List<AnimeInfo> = emptyList()

    suspend fun getRandomizedQuestions(numberOfQuestions: Int): List<QuizQuestion> {
        if (animeList.isEmpty())
            loadAnimeData()

        return generateQuizQuestions(animeList, numberOfQuestions)
    }

    suspend fun getRandomizedQuestionsGt(
        numberOfQuestions: Int,
        rating: Float
    ): List<QuizQuestion> {
        if (animeList.isEmpty())
            loadAnimeData()

        return generateQuizQuestions(animeList, numberOfQuestions, rating, true)
    }

    suspend fun getRandomizedQuestionsLte(
        numberOfQuestions: Int,
        rating: Float
    ): List<QuizQuestion> {
        if (animeList.isEmpty())
            loadAnimeData()

        return generateQuizQuestions(animeList, numberOfQuestions, rating, false)
    }

    private suspend fun loadAnimeData() {
        withContext(Dispatchers.IO) {
            val jsonString = context.assets.open("info.json")
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
        isGreater: Boolean? = null
    ): List<QuizQuestion> {
        val shuffledAnimeList = animeList.filter {
            if (rating != null && isGreater != null) {
                if (isGreater) it.rating > rating
                else it.rating <= rating
            } else true
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
        isGreater: Boolean? = null
    ): List<String> {
        val allTitles = animeList.filter {
            (if (rating != null && isGreater != null) {
                if (isGreater) it.rating > rating
                else it.rating <= rating
            } else true) && it.titleRu != correctAnime.titleRu
        }.map { it.titleRu }.distinct()
        val incorrectOptions = allTitles.shuffled().take(3)
        val options = incorrectOptions + correctAnime.titleRu
        return options.shuffled()
    }

    fun setHighScore(score: Int) {
        val highScore = getHighScore()
        if (score > highScore)
            sharedPreferences.edit {
                putInt(HIGH_SCORE, score)
            }
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt(HIGH_SCORE, 0)
    }
}