package kz.yers.quiz.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.yers.quiz.model.FriendProfile

sealed interface FriendLookup {
    data class Found(
        val profile: FriendProfile,
    ) : FriendLookup

    data object NotFound : FriendLookup

    data object Offline : FriendLookup
}

/**
 * Friends on the existing Spark model — no new collection, no Cloud Functions.
 * A "friend code" is the player's anonymous Firebase uid; profiles are read
 * from the public, already-rules-guarded `leaderboard/{uid}` doc, so no
 * `firestore.rules` change is required. The friend list itself is local (Room);
 * we only ever read others' docs, never write them.
 */
class FriendsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val col get() = db.collection(COLLECTION)

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T =
        withContext(Dispatchers.IO) { Tasks.await(task) }

    /** This device's friend code (its anon uid). Null if sign-in isn't available. */
    suspend fun myUid(): String? =
        runCatching {
            auth.currentUser?.uid
                ?: await(auth.signInAnonymously()).user?.uid
        }.getOrNull()

    /** Resolve a friend code to a public profile. Distinguishes "no such player" from offline. */
    suspend fun lookup(uid: String): FriendLookup =
        runCatching {
            myUid()
            val snap = await(col.document(uid).get())
            if (!snap.exists()) {
                FriendLookup.NotFound
            } else {
                FriendLookup.Found(
                    FriendProfile(
                        uid = uid,
                        name = snap.getString("name").orEmpty().ifBlank { "Игрок" },
                        bestScore = (snap.getLong("score") ?: 0L).toInt(),
                    ),
                )
            }
        }.getOrElse { FriendLookup.Offline }

    /** Best-effort live score for an already-added friend; null on any failure. */
    suspend fun fetch(uid: String): FriendProfile? =
        runCatching {
            val snap = await(col.document(uid).get())
            if (!snap.exists()) {
                null
            } else {
                FriendProfile(
                    uid = uid,
                    name = snap.getString("name").orEmpty().ifBlank { "Игрок" },
                    bestScore = (snap.getLong("score") ?: 0L).toInt(),
                )
            }
        }.getOrNull()

    private companion object {
        const val COLLECTION = "leaderboard"
    }
}
