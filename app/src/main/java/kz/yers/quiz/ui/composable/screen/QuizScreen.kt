package kz.yers.quiz.ui.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.BASE_URL
import kz.yers.quiz.R
import kz.yers.quiz.model.HintInventory
import kz.yers.quiz.model.HintType
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.model.QuestionHintState
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.ui.composable.AudioPlayer
import kz.yers.quiz.ui.composable.BlurredImage
import kz.yers.quiz.ui.composable.hints.HintBar
import kz.yers.quiz.ui.composable.hints.RewardedAdDialog
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import kz.yers.quiz.ui.theme.bodyFontFamily
import kz.yers.quiz.ui.theme.bodyScale
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
    totalQuestions: Int = 30,
    modeTint: Color? = null,
    hintsAvailable: Boolean = false,
    hintInventory: HintInventory = HintInventory(),
    questionHintState: QuestionHintState = QuestionHintState(),
    pendingAdType: HintType? = null,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onPlaybackReady: () -> Unit,
    onUseHint: (HintType) -> Unit = {},
    onRequestAd: (HintType) -> Unit = {},
    onAdComplete: () -> Unit = {},
    onAdCancel: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    var showExitDialog by remember { mutableStateOf(false) }
    val tint = modeTint ?: QuizColors.tint
    val a11y = LocalA11y.current
    val haptic = LocalHapticFeedback.current
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

    val isCorrect = userAnswer != null && userAnswer == question.correctAnswer.titleRu
    val isWrong = userAnswer != null && !isCorrect

    // Stable feedback used by the big overlay. Captured on the userAnswer transition so a
    // `question` change before `userAnswer` clears (during onNextQuestion) doesn't flip the
    // displayed result. The SideEffect-backed fallback keeps the last value visible while the
    // exit animation runs after userAnswer becomes null.
    val capturedPick: Boolean? =
        remember(userAnswer) {
            if (userAnswer != null) userAnswer == question.correctAnswer.titleRu else null
        }
    var lastPickCorrect by remember { mutableStateOf(false) }
    SideEffect {
        if (capturedPick != null) lastPickCorrect = capturedPick
    }
    val overlayCorrect = capturedPick ?: lastPickCorrect

    LaunchedEffect(userAnswer) {
        if (isCorrect) {
            SoundManager.playCorrectAnswer()
            if (!a11y.reduceMotion) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else if (isWrong && !a11y.reduceMotion) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        ) {
                        }
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
                CloseButton(onClick = { showExitDialog = true })
                Spacer(Modifier.width(10.dp))
                ScoreChip(score = score, tint = tint, modifier = Modifier.scale(scale.value))
                if (streak > 5) {
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.flame),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(streak).coerceAtMost(totalQuestions)}/$totalQuestions",
                    fontFamily = RussoOneFamily,
                    fontSize = 13.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Poster panel.
            MangaPanel(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(imageHeight),
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
                paused = pendingAdType != null,
                onPlaybackReady = onPlaybackReady,
                onPlaybackEnded = onNextQuestion,
            )

            Spacer(Modifier.height(20.dp))

            if (hintsAvailable && userAnswer == null) {
                HintBar(
                    fiftyFiftyCount = hintInventory.fiftyFifty,
                    revealCount = hintInventory.revealLetter,
                    skipCount = hintInventory.skip,
                    fiftyFiftyDisabled = questionHintState.fiftyFiftyUsed,
                    revealDisabled = questionHintState.revealUsed,
                    onUseHint = onUseHint,
                    onRequestAd = onRequestAd,
                )
                Spacer(Modifier.height(12.dp))
            }

            if (questionHintState.revealedFirstLetter != null && userAnswer == null) {
                SpeechBubble(
                    text = stringResource(R.string.hint_revealed_letter, questionHintState.revealedFirstLetter),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }

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
                        val eliminated = userAnswer == null && option in questionHintState.eliminatedOptions

                        val selectionShake =
                            if (optionSelectedWrong) {
                                Modifier.offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                            } else {
                                Modifier
                            }

                        OptionTile(
                            text = option,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .then(selectionShake),
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
                                    eliminated -> "✕"
                                    else -> null
                                },
                            enabled = userAnswer == null && !eliminated,
                            dimmed = eliminated,
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
                    scaleIn(
                        initialScale = 0.4f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                    )
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
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    modifier = Modifier.height(180.dp).fillMaxWidth().padding(top = 16.dp),
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(id = R.drawable.burst),
                    contentDescription = null,
                )
                ImpactText(
                    text = if (overlayCorrect) "ВЕРНО!" else "МИМО!",
                    style = MaterialTheme.typography.displayLarge,
                    tintColor = if (overlayCorrect) correctSurface else errorSurface,
                )
            }
        }

        if (pendingAdType != null) {
            RewardedAdDialog(
                onComplete = onAdComplete,
                onCancel = onAdCancel,
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Выйти из викторины?") },
                text = { Text("Прогресс не сохранится.") },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        onClose()
                    }) {
                        Text("Выйти", color = errorSurface)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Остаться")
                    }
                },
            )
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = kz.yers.quiz.ui.composable.manga.MangaIcons.Close,
            contentDescription = "Закрыть",
            tint = QuizColors.ink,
            modifier = Modifier.size(20.dp),
        )
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
    dimmed: Boolean = false,
    onClick: () -> Unit,
) {
    val borderColor =
        when {
            dimmed -> QuizColors.ink.copy(alpha = 0.35f)
            else -> highlight ?: QuizColors.ink
        }
    val borderWidth = if (highlight != null) QuizStrokes.hero else QuizStrokes.panel
    val backgroundColor =
        when {
            dimmed -> QuizColors.ink.copy(alpha = 0.06f)
            highlight != null -> highlight.copy(alpha = 0.15f)
            else -> Color.White
        }
    val textAlpha =
        when {
            dimmed -> 0.35f
            enabled || highlight != null -> 1f
            else -> 0.6f
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
                ).padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconGlyph != null) {
                Text(
                    text = iconGlyph,
                    color =
                        when {
                            dimmed -> QuizColors.ink.copy(alpha = 0.4f)
                            else -> highlight ?: QuizColors.ink
                        },
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.width(8.dp))
            }
            val scale = bodyScale()
            Text(
                text = text,
                color = QuizColors.ink.copy(alpha = textAlpha),
                fontWeight = FontWeight.Medium,
                fontFamily = bodyFontFamily(),
                fontSize = (14f * scale).sp,
                lineHeight = (18f * scale).sp,
            )
        }
    }
}
