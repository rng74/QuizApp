package kz.yers.quiz.ui.composable.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kz.yers.quiz.BASE_URL
import kz.yers.quiz.R
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.ui.composable.AudioPlayer
import kz.yers.quiz.ui.composable.BlurredImage
import kz.yers.quiz.ui.theme.Green700
import kz.yers.quiz.ui.theme.Red700

@Composable
fun QuizScreen(
    question: QuizQuestion,
    userAnswer: String?,
    timeRemaining: Long,
    maxTime: Long,
    score: Int,
    isPosterEnabled: Boolean,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onPlaybackReady: () -> Unit
) {
    val progress by remember(timeRemaining) {
        mutableFloatStateOf(timeRemaining / maxTime.toFloat())
    }
    var previousScore by remember {
        mutableIntStateOf(score)
    }
    val scale = remember { Animatable(1f) }
    val density = LocalDensity.current
    val imageHeight = 250.dp

    LaunchedEffect(score) {
        if (score > previousScore) {
            // Animate scale up
            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(durationMillis = 300)
            )
            // Animate scale back to normal
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
            previousScore = score
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.score, score),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .scale(scale.value)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            if (isPosterEnabled) {
                BlurredImage(
                    url = BASE_URL + question.correctAnswer.posterLink,
                    isBlurred = userAnswer == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = with(density) { imageHeight.toPx() }
                        )
                    )
            )
            LinearProgressIndicator(
                progress = {
                    progress.coerceIn(0f, 1f)
                },
                color = MaterialTheme.colorScheme.secondary,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.TopCenter)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AudioPlayer(
            url = BASE_URL + question.correctAnswer.songLink,
            onPlaybackReady = {
                onPlaybackReady()
            }, onPlaybackEnded = {
                onNextQuestion()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        question.options.forEach { option ->
            val isCorrect = userAnswer != null && option == question.correctAnswer.titleRu
            val isSelected = userAnswer != null && userAnswer == option
            OutlinedButton(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = userAnswer == null,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    1.dp, when {
                        isCorrect -> Green700
                        isSelected -> Red700
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(text = option, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (userAnswer != null) {
            Spacer(modifier = Modifier.height(32.dp))
            val isCorrect = userAnswer == question.correctAnswer.titleRu
            val feedbackText =
                if (isCorrect) stringResource(R.string.correct) else stringResource(R.string.incorrect)
            val feedbackColor = if (isCorrect) Green700 else Red700

            Text(
                text = feedbackText,
                color = feedbackColor,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNextQuestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                val text =
                    if (isCorrect) stringResource(R.string.next_question) else stringResource(R.string.check_results)
                Text(text = text, color = Color.White)
            }
        }
    }
}