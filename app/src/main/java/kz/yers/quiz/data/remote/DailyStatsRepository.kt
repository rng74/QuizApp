package kz.yers.quiz.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.yers.quiz.model.DailyLiveStats

/**
 * Firebase-backed shared stats for the Daily Challenge. The track itself is picked
 * deterministically client-side (same epochDay → same track for everyone), so no server is
 * needed; this only aggregates how the player base did today.
 *
 * Model: `daily/{epochDay}/attempts/{uid}` = { score, correct, updatedAt }. No Cloud Functions —
 * writes go straight from the client, bounded by Firestore Security Rules (see firestore.rules).
 * Until Anonymous auth + Firestore are provisioned every call degrades to null (UI shows "—").
 */
class DailyStatsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun attempts(epochDay: Long) =
        db.collection(COLLECTION).document(epochDay.toString()).collection(ATTEMPTS)

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T =
        withContext(Dispatchers.IO) { Tasks.await(task) }

    private suspend fun ensureUid(): String {
        auth.currentUser?.let { return it.uid }
        val result = await(auth.signInAnonymously())
        return result.user?.uid ?: error("anonymous sign-in returned no user")
    }

    /** Record this player's daily result. Monotonic on score so re-entry can't lower it. */
    suspend fun submitAttempt(
        epochDay: Long,
        score: Int,
        correct: Boolean,
    ) {
        runCatching {
            val uid = ensureUid()
            val ref = attempts(epochDay).document(uid)
            await(
                db.runTransaction { txn ->
                    val current = txn.get(ref).getLong("score") ?: -1L
                    if (score.toLong() > current) {
                        txn.set(
                            ref,
                            mapOf(
                                "score" to score.toLong(),
                                "correct" to correct,
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    }
                    null
                },
            )
        }
        // Best-effort: a failed submit (offline / rules not deployed) must not break the run flow.
    }

    /** Cross-player stats for [epochDay]; null on any failure so the UI can show "—" gracefully. */
    suspend fun loadStats(
        epochDay: Long,
        myScore: Int?,
    ): DailyLiveStats? =
        runCatching {
            ensureUid()
            val col = attempts(epochDay)
            val players = await(col.count().get(AggregateSource.SERVER)).count.toInt()
            if (players == 0) return@runCatching DailyLiveStats(players = 0, solvedPct = 0, myRank = null)
            val solved =
                await(
                    col.whereEqualTo("correct", true).count().get(AggregateSource.SERVER),
                ).count.toInt()
            val myRank =
                if (myScore != null && myScore > 0) {
                    await(
                        col.whereGreaterThan("score", myScore.toLong())
                            .count()
                            .get(AggregateSource.SERVER),
                    ).count.toInt() + 1
                } else {
                    null
                }
            DailyLiveStats(
                players = players,
                solvedPct = (solved * 100 / players),
                myRank = myRank,
            )
        }.getOrNull()

    private companion object {
        const val COLLECTION = "daily"
        const val ATTEMPTS = "attempts"
    }
}
