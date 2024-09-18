package kz.yers.quiz.ui.composable.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kz.yers.quiz.R
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.ui.composable.OptionToggle

@Composable
fun GameModeMenuScreen(
    highScore: Int,
    onGameModeSelected: (GameMode) -> Unit,
    isPosterEnabled: Boolean,
    onPosterToggle: (Boolean) -> Unit
) {
    val withPosterStr = stringResource(R.string.with_poster)
    val withoutPosterStr = stringResource(R.string.without_poster)
    val highScoreStr = stringResource(R.string.your_high_score_is, highScore)
    val selectGameModeStr = stringResource(R.string.select_game_mode)

    val options = listOf(
        withPosterStr,
        withoutPosterStr
    )
    val selectedOption =
        if (isPosterEnabled) withPosterStr else withoutPosterStr

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = highScoreStr,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(128.dp))
        Text(
            text = selectGameModeStr,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OptionToggle(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selected ->
                val enabled = selected == withPosterStr
                onPosterToggle(enabled)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        GameMode.entries.forEach { gameMode ->
            Button(
                onClick = { onGameModeSelected(gameMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = gameMode.displayName)
            }
        }
    }
}