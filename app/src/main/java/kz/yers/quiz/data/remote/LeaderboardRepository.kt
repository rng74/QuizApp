package kz.yers.quiz.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.yers.quiz.model.LeaderboardEntry
import kz.yers.quiz.model.LeaderboardUiState

/**
 * Firebase-backed global leaderboard. Anonymous Auth + a single `leaderboard/{uid}` doc per
 * player holding their best solo score. No Cloud Functions: reads/writes go straight from the
 * client and are constrained by Firestore Security Rules (see firestore.rules).
 *
 * Requires (one-time, Firebase console): Anonymous auth enabled + Firestore database created
 * with firestore.rules deployed. Until then every call degrades to [LeaderboardUiState.Offline].
 */
class LeaderboardRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val col get() = db.collection(COLLECTION)

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T =
        withContext(Dispatchers.IO) { Tasks.await(task) }

    private suspend fun ensureUid(): String {
        auth.currentUser?.let { return it.uid }
        val result = await(auth.signInAnonymously())
        return result.user?.uid ?: error("anonymous sign-in returned no user")
    }

    suspend fun load(): LeaderboardUiState =
        runCatching {
            val uid = ensureUid()
            val topSnap =
                await(
                    col.orderBy("score", Query.Direction.DESCENDING).limit(TOP_LIMIT).get(),
                )
            val rows =
                topSnap.documents.mapIndexed { index, doc ->
                    LeaderboardEntry(
                        rank = index + 1,
                        name = doc.getString("name").orEmpty().ifBlank { "Игрок" },
                        mode = doc.getString("mode").orEmpty(),
                        score = (doc.getLong("score") ?: 0L).toInt(),
                        isYou = doc.id == uid,
                    )
                }
            val mySnap = await(col.document(uid).get())
            val myScore = (mySnap.getLong("score") ?: 0L).toInt()
            val total = await(col.count().get(AggregateSource.SERVER)).count.toInt()
            val myRank =
                if (myScore > 0) {
                    await(
                        col.whereGreaterThan("score", myScore)
                            .count()
                            .get(AggregateSource.SERVER),
                    ).count.toInt() + 1
                } else {
                    null
                }
            LeaderboardUiState.Loaded(
                rows = rows,
                myRank = myRank,
                myScore = myScore,
                totalPlayers = total,
            )
        }.getOrElse { LeaderboardUiState.Offline }

    /** Upsert the player's best score. Monotonic: only writes when [score] beats the stored one.
     *
     *  Scores ≤ 0 never write — a brand-new player whose first run timed out at 0
     *  should not appear on the global board with 0 points (the original `?: -1L`
     *  default for a missing doc would let `0 > -1` pass through). The caller in
     *  QuizAppViewModel.finishRun() also gates on `finalScore > 0`; this is the
     *  belt-and-suspenders inside the repo.
     */
    suspend fun submitScore(
        name: String,
        score: Int,
        modeLabel: String,
    ) {
        if (score <= 0) return
        runCatching {
            val uid = ensureUid()
            val ref = col.document(uid)
            await(
                db.runTransaction { txn ->
                    val current = txn.get(ref).getLong("score") ?: 0L
                    if (score.toLong() > current) {
                        txn.set(
                            ref,
                            mapOf(
                                "name" to name.take(24),
                                "score" to score.toLong(),
                                "mode" to modeLabel,
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

    private companion object {
        const val COLLECTION = "leaderboard"
        const val TOP_LIMIT = 30L
    }
}
