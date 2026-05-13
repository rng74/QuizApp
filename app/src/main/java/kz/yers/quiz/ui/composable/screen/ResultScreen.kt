package kz.yers.quiz.ui.composable.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kz.yers.quiz.R
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.share.ShareResultDialog
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.RussoOneFamily

@Composable
fun ResultScreen(
    score: Int,
    needToAskReview: Boolean,
    isNewRecord: Boolean,
    onReviewSuccess: () -> Unit,
    onRestart: () -> Unit,
    modeTint: Color? = null,
    modeDisplayName: String? = null,
    onPlayAgain: () -> Unit = onRestart,
) {
    // Freeze the result snapshot on first composition. onRestart resets the underlying state
    // (score → 0, isNewRecord → false) before navigation completes, which would otherwise
    // flash a zeroed result. The screen is short-lived so freezing here is safe.
    val frozenScore = remember { score }
    val frozenIsNewRecord = remember { isNewRecord }
    val frozenTint = remember { modeTint ?: QuizColors.tint }
    val frozenModeName = remember { modeDisplayName }
    val tint = frozenTint
    var showShare by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ImpactText(
            text = stringResource(R.string.quiz_finished).uppercase(),
            style = MaterialTheme.typography.displaySmall,
            tintColor = tint,
        )
        Spacer(Modifier.height(24.dp))

        if (frozenIsNewRecord) {
            MangaPanel(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .rotate(-3f),
                background = QuizColors.streakFire,
                shadowOffset = QuizShadows.medium,
                contentPadding = 12.dp,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier =
                            Modifier
                                .height(180.dp)
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                        contentScale = ContentScale.FillWidth,
                        painter = painterResource(id = R.drawable.burst),
                        contentDescription = null,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MangaIcons.Trophy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = "НОВЫЙ РЕКОРД",
                            color = Color.White,
                            fontFamily = RussoOneFamily,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp,
                        )
                        Icon(
                            imageVector = MangaIcons.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = QuizShadows.large,
            contentPadding = 28.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "ВАШ РЕЗУЛЬТАТ",
                    fontFamily = RussoOneFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = frozenScore.toString(),
                    fontFamily = BangersFamily,
                    fontSize = 72.sp,
                    color = QuizColors.accentBlue,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        MangaButton(
            label = stringResource(R.string.play_again).uppercase(),
            variant = MangaButtonVariant.Tint,
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )
        Spacer(Modifier.height(12.dp))
        MangaButton(
            label = stringResource(R.string.back_to_menu).uppercase(),
            variant = MangaButtonVariant.Ghost,
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 48.dp,
        )
        Spacer(Modifier.height(8.dp))
        MangaButton(
            label = stringResource(R.string.share_button).uppercase(),
            variant = MangaButtonVariant.Ghost,
            onClick = { showShare = true },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 44.dp,
        )
    }

    if (showShare) {
        ShareResultDialog(
            score = frozenScore,
            isNewRecord = frozenIsNewRecord,
            modeTint = tint,
            modeDisplayName = frozenModeName,
            onDismiss = { showShare = false },
        )
    }

    if (needToAskReview) {
        val localContext = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val reviewManager = remember { ReviewManagerFactory.create(localContext) }
        LaunchedEffect("") {
            coroutineScope.launch {
                val success = launchInAppReview(localContext as Activity, reviewManager)
                if (success) onReviewSuccess()
            }
        }
    }
}

suspend fun launchInAppReview(
    activity: Activity,
    reviewManager: ReviewManager,
): Boolean =
    try {
        val request =
            withContext(Dispatchers.IO) {
                reviewManager.requestReviewFlow().await()
            }
        if (request != null) {
            reviewManager.launchReviewFlow(activity, request).await()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
