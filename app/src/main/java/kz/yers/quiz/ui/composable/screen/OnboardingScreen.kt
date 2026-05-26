package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kz.yers.quiz.ui.composable.manga.HalftoneOverlay
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes

private data class OnboardingPage(
    val art: @Composable () -> Unit,
    val headline: String,
    val body: String,
)

private val pages =
    listOf(
        OnboardingPage(
            art = { OnboardingArtTap() },
            headline = "СЛУШАЙ.\nУГАДЫВАЙ.",
            body = "Мы играем трек из аниме, ты выбираешь нужное название из четырёх вариантов.",
        ),
        OnboardingPage(
            art = { OnboardingArtBlur() },
            headline = "ПОСТЕР\nВ ТУМАНЕ.",
            body = "Постер размытый, пока играет музыка. Когда отвечаешь — фокус наводится. Загадка раскрыта.",
        ),
        OnboardingPage(
            art = { OnboardingArtTimer() },
            headline = "БЫСТРЕЕ —\nОЧКОВ БОЛЬШЕ.",
            body = "10 секунд на ответ. Чем быстрее — тем больше очков. Серия побед — больше бонусов.",
        ),
    )

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.size - 1

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            repeat(pages.size) { i ->
                val isActive = i == pagerState.currentPage
                Box(
                    modifier =
                        Modifier
                            .height(8.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(if (isActive) 4.dp else 8.dp))
                            .background(if (isActive) QuizColors.ink else QuizColors.ink.copy(alpha = 0.25f)),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        MangaButton(
            label = if (isLast) "ИГРАТЬ!" else "ДАЛЬШЕ",
            variant = MangaButtonVariant.Tint,
            onClick = {
                if (isLast) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )

        if (!isLast) {
            Spacer(Modifier.height(8.dp))
            MangaButton(
                label = "ПРОПУСТИТЬ",
                variant = MangaButtonVariant.Ghost,
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 44.dp,
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = 16.dp, end = QuizShadows.large, bottom = QuizShadows.large)
                    .size(240.dp)
                    .drawBehind {
                        val o = QuizShadows.large.toPx()
                        val r = QuizRadii.button.toPx()
                        drawRoundRect(
                            color = QuizColors.ink,
                            topLeft = Offset(o, o),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(r, r),
                        )
                    }
                    .clip(RoundedCornerShape(QuizRadii.button))
                    .background(QuizColors.paper2)
                    .border(QuizStrokes.panel, QuizColors.ink, RoundedCornerShape(QuizRadii.button)),
        ) {
            page.art()
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = page.headline,
            fontFamily = BangersFamily,
            fontSize = 36.sp,
            lineHeight = 38.sp,
            letterSpacing = 1.sp,
            color = QuizColors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.body,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = QuizColors.ink.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun OnboardingArtTap() {
    Box(modifier = Modifier.fillMaxSize()) {
        HalftoneOverlay(modifier = Modifier.fillMaxSize(), opacity = 0.45f)
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(QuizColors.tint)
                        .border(QuizStrokes.hero, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "▶",
                    fontSize = 56.sp,
                    color = QuizColors.ink,
                )
            }
        }
    }
}

@Composable
private fun OnboardingArtBlur() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clip(RoundedCornerShape(QuizRadii.card))
                .drawBehind {
                    drawRect(
                        color = QuizColors.onboardingPosterBody,
                    )
                    val mid = size.width / 2f
                    drawRect(
                        color = QuizColors.onboardingPosterTitle,
                        size = Size(mid, size.height),
                    )
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(mid, 0f),
                        end = Offset(mid, size.height),
                        strokeWidth = QuizStrokes.panel.toPx(),
                    )
                },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = "→ ЯСНО!",
            fontFamily = BangersFamily,
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(end = 16.dp),
        )
    }
}

@Composable
private fun OnboardingArtTimer() {
    Box(modifier = Modifier.fillMaxSize()) {
        HalftoneOverlay(modifier = Modifier.fillMaxSize(), opacity = 0.4f)
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(140.dp)
                        .aspectRatio(1f)
                        .drawBehind {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f - QuizStrokes.hero.toPx() / 2f,
                                center = Offset(cx, cy),
                            )
                            drawCircle(
                                color = QuizColors.ink,
                                radius = size.minDimension / 2f - QuizStrokes.hero.toPx() / 2f,
                                center = Offset(cx, cy),
                                style = Stroke(width = QuizStrokes.hero.toPx()),
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                ImpactText(
                    text = "10",
                    style =
                        androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                        ),
                    tintColor = QuizColors.streakFire,
                )
            }
        }
    }
}
