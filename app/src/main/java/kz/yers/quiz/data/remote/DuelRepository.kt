package kz.yers.quiz.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kz.yers.quiz.data.analytics.Analytics
import kz.yers.quiz.data.analytics.Events
import kz.yers.quiz.data.analytics.Params
import kotlin.random.Random

/** Live view of a duel doc, pushed by the Firestore snapshot listener. */
data class DuelSnapshot(
    val hostName: String,
    val guestName: String?,
    val hostScore: Int?,
    val guestScore: Int?,
    val seed: Long,
)

sealed interface DuelCreateResult {
    data class Ok(
        val code: String,
        val seed: Long,
    ) : DuelCreateResult

    data object Offline : DuelCreateResult
}

sealed interface DuelJoinResult {
    data class Ok(
        val seed: Long,
        val hostName: String,
    ) : DuelJoinResult

    data object NotFound : DuelJoinResult

    data object Full : DuelJoinResult

    data object Offline : DuelJoinResult
}

/**
 * Firebase-backed async duel. No Cloud Functions / FCM (Spark): both players answer the same
 * seed-deterministic track on their own device and scores reconcile through a Firestore
 * snapshot listener while either player has the result screen open. Writes are constrained by
 * Security Rules (see firestore.rules); until Firestore/anon-auth are provisioned every call
 * degrades to an Offline/NotFound result and the duel simply can't be created/joined.
 */
class DuelRepository(
    private val analytics: Analytics,
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

    private fun randomCode(): String =
        (1..CODE_LEN).map { ALPHABET[Random.nextInt(ALPHABET.length)] }.joinToString("")

    /** Create a fresh duel with a unique 6-char code; the host owns the doc. */
    suspend fun create(myName: String): DuelCreateResult =
        runCatching {
            val uid = ensureUid()
            val seed = Random.nextLong()
            repeat(CODE_TRIES) {
                val code = randomCode()
                val ref = col.document(code)
                val created =
                    await(
                        db.runTransaction { txn ->
                            if (txn.get(ref).exists()) {
                                false
                            } else {
                                txn.set(
                                    ref,
                                    mapOf(
                                        "hostUid" to uid,
                                        "hostName" to myName.take(24),
                                        "seed" to seed,
                                        "guestUid" to "",
                                        "guestName" to "",
                                        "createdAt" to FieldValue.serverTimestamp(),
                                    ),
                                )
                                true
                            }
                        },
                    )
                if (created) {
                    analytics.log(
                        Events.DUEL_CREATE,
                        Params.RESULT to "ok",
                        Params.DUEL_ROLE to "HOST",
                    )
                    return@runCatching DuelCreateResult.Ok(code, seed)
                }
            }
            error("could not allocate a unique duel code")
        }.getOrElse {
            analytics.log(
                Events.DUEL_CREATE,
                Params.RESULT to "offline",
                Params.ERROR_MESSAGE to (it.message ?: "unknown"),
            )
            DuelCreateResult.Offline
        }

    /** Claim the guest slot of an existing duel. Idempotent if this device already joined. */
    suspend fun join(
        code: String,
        myName: String,
    ): DuelJoinResult {
        val result =
            runCatching {
                val uid = ensureUid()
                val ref = col.document(code.uppercase())
                await(
                    db.runTransaction<DuelJoinResult> { txn ->
                        val snap = txn.get(ref)
                        if (!snap.exists()) return@runTransaction DuelJoinResult.NotFound
                        val hostUid = snap.getString("hostUid").orEmpty()
                        val guestUid = snap.getString("guestUid").orEmpty()
                        val seed = snap.getLong("seed") ?: 0L
                        val hostName = snap.getString("hostName").orEmpty().ifBlank { "Соперник" }
                        when {
                            hostUid == uid -> DuelJoinResult.NotFound
                            guestUid.isNotEmpty() && guestUid != uid -> DuelJoinResult.Full
                            else -> {
                                if (guestUid.isEmpty()) {
                                    txn.update(
                                        ref,
                                        mapOf(
                                            "guestUid" to uid,
                                            "guestName" to myName.take(24),
                                        ),
                                    )
                                }
                                DuelJoinResult.Ok(seed = seed, hostName = hostName)
                            }
                        }
                    },
                )
            }.getOrElse { DuelJoinResult.Offline }
        analytics.log(
            Events.DUEL_JOIN,
            Params.RESULT to
                when (result) {
                    is DuelJoinResult.Ok -> "ok"
                    DuelJoinResult.NotFound -> "not_found"
                    DuelJoinResult.Full -> "full"
                    DuelJoinResult.Offline -> "offline"
                },
            Params.DUEL_ROLE to "GUEST",
        )
        return result
    }

    /** Write this device's final score once. Best-effort — a failure must not break the run. */
    suspend fun submitScore(
        code: String,
        isHost: Boolean,
        score: Int,
    ) {
        val field = if (isHost) "hostScore" else "guestScore"
        runCatching {
            ensureUid()
            val ref = col.document(code)
            await(
                db.runTransaction { txn ->
                    val snap = txn.get(ref)
                    if (snap.getLong(field) == null) {
                        txn.update(ref, mapOf(field to score.toLong()))
                    }
                    null
                },
            )
        }
    }

    /** Emits the duel doc on every change; emits null on error/deletion. Auto-detaches on close. */
    fun listen(code: String): Flow<DuelSnapshot?> =
        callbackFlow {
            val reg =
                col.document(code).addSnapshotListener { snap, err ->
                    if (err != null || snap == null || !snap.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(
                        DuelSnapshot(
                            hostName = snap.getString("hostName").orEmpty().ifBlank { "Хост" },
                            guestName = snap.getString("guestName").orEmpty().ifBlank { null },
                            hostScore = snap.getLong("hostScore")?.toInt(),
                            guestScore = snap.getLong("guestScore")?.toInt(),
                            seed = snap.getLong("seed") ?: 0L,
                        ),
                    )
                }
            awaitClose { reg.remove() }
        }

    private companion object {
        const val COLLECTION = "duels"
        const val CODE_LEN = 6
        const val CODE_TRIES = 5

        // No 0/O/1/I — unambiguous when read aloud or typed.
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
