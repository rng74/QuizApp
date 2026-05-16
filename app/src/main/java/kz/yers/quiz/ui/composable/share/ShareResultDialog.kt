package kz.yers.quiz.ui.composable.share

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kz.yers.quiz.R
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.utils.buildScoreShareText
import kz.yers.quiz.utils.emojiGridFor
import kz.yers.quiz.utils.saveBitmapToCache
import kz.yers.quiz.utils.shareImage
import kz.yers.quiz.utils.shareText

private const val SHARE_OUTPUT_PX = 1080

@Composable
fun ShareResultDialog(
    score: Int,
    isNewRecord: Boolean,
    modeTint: Color,
    modeDisplayName: String?,
    correct: Int,
    total: Int,
    isDaily: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val captionText = stringResource(R.string.share_caption, score)
    val chooserTitle = stringResource(R.string.share_chooser_title)
    val gridPreview = remember(correct, total) { emojiGridFor(correct, total) }
    val layer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(QuizColors.ink.copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            val buttonsAndSpacing = 84.dp
            val cardSize =
                minOf(maxWidth, maxHeight - buttonsAndSpacing).coerceAtMost(560.dp)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(cardSize)
                            .drawWithContent {
                                layer.record(
                                    size = IntSize(size.width.toInt(), size.height.toInt()),
                                ) { this@drawWithContent.drawContent() }
                                drawLayer(layer)
                            },
                ) {
                    ResultShareCard(
                        score = score,
                        isNewRecord = isNewRecord,
                        modeTint = modeTint,
                        modeDisplayName = modeDisplayName,
                        size = cardSize,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = gridPreview,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MangaButton(
                        label = stringResource(R.string.share_cancel),
                        variant = MangaButtonVariant.Ghost,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    MangaButton(
                        label = stringResource(R.string.share_as_text),
                        variant = MangaButtonVariant.Ghost,
                        onClick = {
                            shareText(
                                context = context,
                                text =
                                    buildScoreShareText(
                                        score = score,
                                        correct = correct,
                                        total = total,
                                        isDaily = isDaily,
                                        modeName = modeDisplayName,
                                    ),
                                chooserTitle = chooserTitle,
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MangaButton(
                        label = stringResource(R.string.share_as_image),
                        variant = MangaButtonVariant.Tint,
                        enabled = !sharing,
                        onClick = {
                            if (sharing) return@MangaButton
                            sharing = true
                            scope.launch {
                                val captured = layer.toImageBitmap().asAndroidBitmap()
                                val output =
                                    withContext(Dispatchers.Default) {
                                        val side = minOf(captured.width, captured.height)
                                        val cropped =
                                            if (captured.width != captured.height) {
                                                val xOffset = (captured.width - side) / 2
                                                val yOffset = (captured.height - side) / 2
                                                Bitmap.createBitmap(captured, xOffset, yOffset, side, side)
                                            } else {
                                                captured
                                            }
                                        Bitmap.createScaledBitmap(
                                            cropped,
                                            SHARE_OUTPUT_PX,
                                            SHARE_OUTPUT_PX,
                                            true,
                                        )
                                    }
                                val uri =
                                    saveBitmapToCache(
                                        context = context,
                                        bitmap = output,
                                        fileName =
                                            "result_${output.width}x${output.height}_" +
                                                "${System.currentTimeMillis()}.png",
                                    )
                                shareImage(
                                    context = context,
                                    uri = uri,
                                    captionText = captionText,
                                    chooserTitle = chooserTitle,
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
