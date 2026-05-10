package kz.yers.quiz.ui.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kz.yers.quiz.BASE_URL
import kz.yers.quiz.R
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.ui.composable.AudioPlayer
import kz.yers.quiz.ui.composable.BlurredImage
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import kz.yers.quiz.ui.theme.errorColor
import kz.yers.quiz.ui.theme.successColor
import kz.yers.quiz.utils.SoundManager
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun QuizScreen(
    question: QuizQuestion,
    userAnswer: String?,
    timeRemaining: Long,
    maxTime: Long,
    score: Int,
    streak: Int,
    isPosterEnabled: Boolean,
    modeTint: Color? = null,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onPlaybackReady: () -> Unit,
) {
    val tint = modeTint ?: QuizColors.tint
    val a11y = LocalA11y.current
    val correctSurface = successColor()
    val errorSurface = errorColor()
    val timerProgress by remember(timeRemaining) {
        mutableFloatStateOf(timeRemaining / maxTime.toFloat())
    }
    val isTimerCritical = timeRemaining < 3_000L
    val timerColor = if (isTimerCritical) errorSurface else tint

    var previousScore by remember { mutableIntStateOf(score) }
    val scale = remember { Animatable(1f) }
    val density = LocalDensity.current
    val imageHeight = 220.dp

    val shakeAnim = remember { Animatable(0f) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.flame_animation))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
    )

    val isCorrect = userAnswer != null && userAnswer == question.correctAnswer.titleRu
    val isWrong = userAnswer != null && !isCorrect

    LaunchedEffect(userAnswer) {
        if (isCorrect) {
            SoundManager.playCorrectAnswer()
        } else if (isWrong && !a11y.reduceMotion) {
            shakeAnim.snapTo(0f)
            shakeAnim.animateTo(
                targetValue = 0f,
                animationSpec =
                    keyframes {
                        durationMillis = 300
                        -16f at 0
                        16f at 50
                        -12f at 100
                        12f at 150
                        -6f at 200
                        6f at 250
                        0f at 300
                    },
            )
        }
    }
    LaunchedEffect(score) {
        if (score > previousScore && !a11y.reduceMotion) {
            scale.animateTo(1.5f, animationSpec = tween(durationMillis = 300))
            scale.animateTo(1f, animationSpec = tween(durationMillis = 300))
            previousScore = score
        } else if (score > previousScore) {
            previousScore = score
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper),
    ) {
        if (isCorrect && !a11y.reduceMotion) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties =
                    listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                            position = Position.Relative(0.5, 0.3),
                        ),
                    ),
                updateListener =
                    object : OnParticleSystemUpdateListener {
                        override fun onParticleSystemEnded(
                            system: PartySystem,
                            activeSystems: Int,
                        ) {}
                    },
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Top: timer bar + score chip + streak.
            TimerBar(progress = timerProgress, color = timerColor)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScoreChip(score = score, tint = tint, modifier = Modifier.scale(scale.value))
                if (streak > 5) {
                    Spacer(Modifier.width(8.dp))
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(streak).coerceAtMost(30)}/30",
                    fontFamily = RussoOneFamily,
                    fontSize = 13.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Poster panel.
            MangaPanel(
                modifier = Modifier.fillMaxWidth().height(imageHeight),
                contentPadding = 0.dp,
                shadowOffset = QuizShadows.medium,
                background = QuizColors.paper2,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isPosterEnabled) {
                        BlurredImage(
                            url = BASE_URL + question.correctAnswer.posterLink,
                            isBlurred = userAnswer == null,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(QuizRadii.card)),
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f),
                                            ),
                                        startY = 0f,
                                        endY = with(density) { imageHeight.toPx() },
                                    ),
                                ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AudioPlayer(
                url = BASE_URL + question.correctAnswer.songLink,
                needPlay = userAnswer == null,
                onPlaybackReady = onPlaybackReady,
                onPlaybackEnded = onNextQuestion,
            )

            Spacer(Modifier.height(20.dp))

            // Answer options — 2x2 grid.
            val opts = question.options
            opts.chunked(2).forEach { rowOpts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowOpts.forEach { option ->
                        val optionCorrect = userAnswer != null && option == question.correctAnswer.titleRu
                        val optionSelectedWrong = userAnswer != null && userAnswer == option && !optionCorrect

                        val selectionShake =
                            if (optionSelectedWrong) {
                                Modifier.offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                            } else {
                                Modifier
                            }

                        OptionTile(
                            text = option,
                            modifier = Modifier.weight(1f).then(selectionShake),
                            highlight =
                                when {
                                    optionCorrect -> correctSurface
                                    optionSelectedWrong -> errorSurface
                                    else -> null
                                },
                            iconGlyph =
                                when {
                                    optionCorrect -> "✓"
                                    optionSelectedWrong -> "✕"
                                    else -> null
                                },
                            enabled = userAnswer == null,
                            onClick = { onAnswerSelected(option) },
                        )
                    }
                    if (rowOpts.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            if (userAnswer != null) {
                MangaButton(
                    label =
                        if (isCorrect) {
                            stringResource(R.string.next_question)
                        } else {
                            stringResource(R.string.check_results)
                        },
                    variant = if (isCorrect) MangaButtonVariant.Tint else MangaButtonVariant.Ink,
                    onClick = onNextQuestion,
                    modifier = Modifier.fillMaxWidth(),
                    minHeight = 56.dp,
                )
            }
        }

        // Big celebratory feedback overlay — Bangers display, fades+scales in/out (or static when
        // reduce-motion is on). Always paired with a redundant ✓/✕ glyph for colour-blind safety.
        val enter =
            if (a11y.reduceMotion) {
                fadeIn(animationSpec = tween(120))
            } else {
                fadeIn(animationSpec = tween(180)) +
                    scaleIn(initialScale = 0.6f, animationSpec = tween(280))
            }
        val exit =
            if (a11y.reduceMotion) {
                fadeOut(animationSpec = tween(120))
            } else {
                fadeOut(animationSpec = tween(120)) +
                    scaleOut(targetScale = 0.85f, animationSpec = tween(120))
            }
        AnimatedVisibility(
            visible = userAnswer != null,
            enter = enter,
            exit = exit,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 96.dp),
        ) {
            ImpactText(
                text = if (isCorrect) "✓ ВЕРНО!" else "✕ МИМО!",
                style = MaterialTheme.typography.displayLarge,
                tintColor = if (isCorrect) correctSurface else errorSurface,
            )
        }
    }
}

@Composable
private fun TimerBar(
    progress: Float,
    color: Color,
) {
    val shape = RoundedCornerShape(QuizRadii.pill)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, shape),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(shape)
                    .background(color),
        )
    }
}

@Composable
private fun ScoreChip(
    score: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(QuizRadii.pill)
    Row(
        modifier =
            modifier
                .clip(shape)
                .background(tint)
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = score.toString(),
            fontFamily = RussoOneFamily,
            fontSize = 18.sp,
            letterSpacing = 1.sp,
            color = QuizColors.ink,
        )
    }
}

@Composable
private fun OptionTile(
    text: String,
    modifier: Modifier,
    highlight: Color?,
    iconGlyph: String?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = highlight ?: QuizColors.ink
    val borderWidth = if (highlight != null) QuizStrokes.hero else QuizStrokes.panel
    val backgroundColor =
        when {
            highlight != null -> highlight.copy(alpha = 0.15f)
            else -> Color.White
        }
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            modifier
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .clip(shape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, shape)
                .then(
                    if (enabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconGlyph != null) {
                Text(
                    text = iconGlyph,
                    color = highlight ?: QuizColors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = QuizColors.ink.copy(alpha = if (enabled || highlight != null) 1f else 0.6f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
