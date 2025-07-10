package kz.yers.quiz.ui.composable.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kz.yers.quiz.R

@Composable
fun ResultScreen(
    score: Int,
    needToAskReview: Boolean,
    onReviewSuccess: () -> Unit,
    onRestart: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.quiz_finished),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.your_score, score),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(text = stringResource(R.string.play_again), color = Color.White)
        }
    }
    if (needToAskReview) {
        val localContext = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val reviewManager =
            remember {
                ReviewManagerFactory.create(localContext)
            }
        LaunchedEffect("") {
            coroutineScope.launch {
                val success = launchInAppReview(localContext as Activity, reviewManager)
                if (success) {
                    onReviewSuccess()
                }
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
