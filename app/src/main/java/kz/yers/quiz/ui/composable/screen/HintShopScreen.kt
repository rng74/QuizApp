package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.HintInventory
import kz.yers.quiz.model.HintType
import kz.yers.quiz.ui.composable.hints.RewardedAdEffect
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import kz.yers.quiz.utils.formatCoins

private data class HintMeta(
    val type: HintType,
    val title: String,
    val desc: String,
)

private val HINTS =
    listOf(
        HintMeta(HintType.FIFTY_FIFTY, "50 : 50", "Убирает два неверных ответа"),
        HintMeta(HintType.REVEAL_LETTER, "Буква", "Открывает первую букву ответа"),
        HintMeta(HintType.SKIP, "Пропуск", "Пропустить трек без штрафа"),
    )

@Composable
fun HintShopScreen(
    coins: Int,
    inventory: HintInventory,
    pendingAdType: HintType?,
    onBuyWithCoins: (HintType) -> Unit,
    onWatchAd: (HintType) -> Unit,
    onAdReward: () -> Unit,
    onAdDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BalanceCard(coins)
        HINTS.forEach { meta ->
            HintCard(
                meta = meta,
                owned = inventory.countFor(meta.type),
                coins = coins,
                onBuyWithCoins = { onBuyWithCoins(meta.type) },
                onWatchAd = { onWatchAd(meta.type) },
            )
        }
    }

    RewardedAdEffect(
        active = pendingAdType != null,
        onReward = onAdReward,
        onDismiss = onAdDismiss,
    )
}

@Composable
private fun BalanceCard(coins: Int) {
    OffsetBox(background = QuizColors.paper2) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(QuizColors.coinDeep)
                        .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MangaIcons.Coin,
                    contentDescription = null,
                    tint = QuizColors.ink,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ВАШ БАЛАНС",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Text(
                    text = "${formatCoins(coins)} монет",
                    fontFamily = RussoOneFamily,
                    fontSize = 22.sp,
                    color = QuizColors.ink,
                )
            }
        }
    }
}

@Composable
private fun HintCard(
    meta: HintMeta,
    owned: Int,
    coins: Int,
    onBuyWithCoins: () -> Unit,
    onWatchAd: () -> Unit,
) {
    val price = meta.type.coinPrice
    val canAfford = coins >= price
    OffsetBox(background = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meta.title.uppercase(),
                        fontFamily = RussoOneFamily,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = QuizColors.ink,
                    )
                    Text(
                        text = meta.desc,
                        fontSize = 12.sp,
                        color = QuizColors.ink.copy(alpha = 0.6f),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(QuizRadii.pill))
                            .background(QuizColors.ink)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "× $owned",
                        fontFamily = RussoOneFamily,
                        fontSize = 13.sp,
                        color = Color.White,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MangaButton(
                    label = "$price МОНЕТ",
                    variant = if (canAfford) MangaButtonVariant.Tint else MangaButtonVariant.Ghost,
                    onClick = onBuyWithCoins,
                    modifier = Modifier.weight(1f),
                    minHeight = 46.dp,
                )
                MangaButton(
                    label = "РЕКЛАМА",
                    variant = MangaButtonVariant.Ghost,
                    onClick = onWatchAd,
                    modifier = Modifier.weight(1f),
                    minHeight = 46.dp,
                )
            }
        }
    }
}

@Composable
private fun OffsetBox(
    background: Color,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .clip(shape)
                .background(background)
                .border(QuizStrokes.panel, QuizColors.ink, shape),
    ) {
        content()
    }
}
