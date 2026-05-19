package kz.yers.quiz

import kz.yers.quiz.model.AnimeInfo
import kz.yers.quiz.repo.pickDailyTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DailyPickerTest {
    private val curatedPoolSize = 200

    /** 200 qualifying entries + noise that the curation predicate must drop. */
    private fun sampleList(): List<AnimeInfo> {
        val qualifying =
            (0 until curatedPoolSize).map { i ->
                AnimeInfo(
                    titleRu = "Аниме %03d".format(i),
                    titleOrig = "Anime $i",
                    albumName = "Anime $i OP",
                    songName = "Song $i",
                    songLink = "/resources/preplay/$i.mp3",
                    posterLink = "/resources/poster/150/$i.jpg",
                    rating = 7.5f,
                )
            }
        val noise =
            listOf(
                // blank titleRu
                AnimeInfo(titleRu = "", albumName = "X OP", rating = 9.0f),
                // below rating threshold
                AnimeInfo(titleRu = "Низкий рейтинг", albumName = "Y OP", rating = 6.0f),
                // not an opening
                AnimeInfo(titleRu = "Эндинг", albumName = "Z ED", rating = 9.0f),
                // duplicate titleRu of a qualifying entry (must dedupe)
                AnimeInfo(titleRu = "Аниме 000", albumName = "Dup OP", rating = 8.0f),
            )
        return qualifying + noise
    }

    @Test
    fun deterministic_sameDaySameTitle() {
        val list = sampleList()
        for (day in listOf(0L, 1L, 42L, 199L, 200L, 1_000L)) {
            val a = pickDailyTitle(list, day)
            val b = pickDailyTitle(list, day)
            assertNotNull(a)
            assertEquals(a!!.titleRu, b!!.titleRu)
        }
    }

    @Test
    fun noRepeatWithinNinetyDayWindow() {
        val list = sampleList()
        // For every start day across more than a full cycle, the 90 picks in
        // [d, d+90) must all be distinct anime.
        for (start in 0 until curatedPoolSize + 50) {
            val window =
                (start until start + 90).map { d ->
                    pickDailyTitle(list, d.toLong())!!.titleRu
                }
            assertEquals(
                "window starting at day $start had a repeat",
                90,
                window.toSet().size,
            )
        }
    }

    @Test
    fun recursExactlyEveryPoolSizeDays() {
        val list = sampleList()
        for (day in 0L until 5L) {
            assertEquals(
                pickDailyTitle(list, day)!!.titleRu,
                pickDailyTitle(list, day + curatedPoolSize)!!.titleRu,
            )
        }
    }

    @Test
    fun curationExcludesNoise() {
        val list = sampleList()
        // Sweep a full cycle: no picked title should ever be a noise entry.
        val picked =
            (0 until curatedPoolSize).map { pickDailyTitle(list, it.toLong())!!.titleRu }.toSet()
        assertEquals(curatedPoolSize, picked.size)
        assert(picked.none { it.isBlank() || it == "Низкий рейтинг" || it == "Эндинг" })
    }

    @Test
    fun emptyListYieldsNull() {
        assertNull(pickDailyTitle(emptyList(), 0L))
    }
}
