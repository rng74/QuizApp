package kz.yers.quiz

import kz.yers.quiz.model.AnimeInfo
import kz.yers.quiz.repo.DAILY_TRACKS
import kz.yers.quiz.repo.pickDailyTitles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPickerTest {
    // Must be ≥ 90 days × DAILY_TRACKS for the curated path to engage.
    private val curatedPoolSize = 500

    /** Qualifying entries + noise that the curation predicate must drop. */
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

    private fun titles(
        list: List<AnimeInfo>,
        day: Long,
    ) = pickDailyTitles(list, day).map { it.titleRu }

    @Test
    fun deterministic_sameDaySameTitlesInOrder() {
        val list = sampleList()
        for (day in listOf(0L, 1L, 42L, 199L, 500L, 1_000L)) {
            val a = titles(list, day)
            assertEquals(DAILY_TRACKS, a.size)
            assertEquals(a, titles(list, day))
        }
    }

    @Test
    fun noRepeatWithinNinetyDayWindow() {
        val list = sampleList()
        // For every start day across more than a full cycle, all tracks in [d, d+90) are distinct.
        for (start in 0 until curatedPoolSize / DAILY_TRACKS + 50) {
            val window = (start until start + 90).flatMap { titles(list, it.toLong()) }
            assertEquals(
                "window starting at day $start had a repeat",
                90 * DAILY_TRACKS,
                window.toSet().size,
            )
        }
    }

    @Test
    fun recursAfterFullPoolCycle() {
        val list = sampleList()
        val cycleDays = (curatedPoolSize / DAILY_TRACKS).toLong()
        for (day in 0L until 5L) {
            assertEquals(titles(list, day), titles(list, day + cycleDays))
        }
    }

    @Test
    fun curationExcludesNoise() {
        val list = sampleList()
        val picked = (0 until curatedPoolSize / DAILY_TRACKS).flatMap { titles(list, it.toLong()) }.toSet()
        assertEquals(curatedPoolSize, picked.size)
        assertTrue(picked.none { it.isBlank() || it == "Низкий рейтинг" || it == "Эндинг" })
    }

    @Test
    fun emptyListYieldsNothing() {
        assertTrue(pickDailyTitles(emptyList(), 0L).isEmpty())
    }
}
