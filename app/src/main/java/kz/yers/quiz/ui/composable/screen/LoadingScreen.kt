package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kz.yers.quiz.R
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.theme.QuizColors

@Composable
fun LoadingScreen(modeTint: Color? = null) {
    val tint = modeTint ?: QuizColors.tint
    Box(
        modifier = Modifier.fillMaxSize().background(QuizColors.paper),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = tint,
                strokeWidth = 5.dp,
                strokeCap = StrokeCap.Square,
                trackColor = QuizColors.paper2,
            )
            Spacer(modifier = Modifier.height(24.dp))
            SpeechBubble(text = stringResource(R.string.loading))
        }
    }
}
