package kz.yers.quiz.ui.composable.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kz.yers.quiz.R
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private const val AD_DURATION_SECONDS = 4

@Composable
fun RewardedAdDialog(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    var secondsRemaining by remember { mutableIntStateOf(AD_DURATION_SECONDS) }
    var rewardReady by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1_000L)
            secondsRemaining -= 1
        }
        rewardReady = 1
    }

    Dialog(
        onDismissRequest = onCancel,
        properties =
            DialogProperties(
                dismissOnBackPress = rewardReady == 1,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            MangaPanel(
                modifier = Modifier.padding(28.dp).fillMaxWidth(),
                background = QuizColors.paper,
                shadowOffset = 6.dp,
                contentPadding = 20.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(QuizRadii.pill))
                                .background(QuizColors.ink)
                                .border(QuizStrokes.panel, QuizColors.ink, RoundedCornerShape(QuizRadii.pill))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.ad_dialog_title),
                            color = QuizColors.tint,
                            fontFamily = RussoOneFamily,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    ImpactText(
                        text =
                            if (rewardReady == 1) {
                                stringResource(R.string.ad_dialog_reward_ready)
                            } else {
                                stringResource(R.string.ad_dialog_subtitle)
                            },
                        style = MaterialTheme.typography.titleLarge,
                        tintColor = QuizColors.ink,
                    )
                    Spacer(Modifier.height(12.dp))
                    if (rewardReady == 0) {
                        Text(
                            text = stringResource(R.string.ad_dialog_remaining, secondsRemaining),
                            color = QuizColors.ink.copy(alpha = 0.7f),
                            fontFamily = RussoOneFamily,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    if (rewardReady == 1) {
                        MangaButton(
                            label = stringResource(R.string.ad_dialog_reward_ready),
                            variant = MangaButtonVariant.Tint,
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth(),
                            minHeight = 52.dp,
                        )
                    } else {
                        MangaButton(
                            label = stringResource(R.string.ad_dialog_cancel),
                            variant = MangaButtonVariant.Ghost,
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                            minHeight = 52.dp,
                        )
                    }
                }
            }
        }
    }
}
