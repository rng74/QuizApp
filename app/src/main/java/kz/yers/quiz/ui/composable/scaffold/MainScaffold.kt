package kz.yers.quiz.ui.composable.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.AppState
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private enum class Tab { HOME, DAILY, DUEL, TOP, PROFILE }

/** Screens that get the pinned bottom navigation (root-level destinations). */
private fun AppState.isRootTab(): Boolean =
    this is AppState.Menu ||
        this is AppState.Daily ||
        this is AppState.DuelSetup ||
        this is AppState.Leaderboard ||
        this is AppState.Profile

/** Screens whose top app bar is owned by the scaffold (the two we redesigned). */
private fun AppState.scaffoldOwnsTopBar(): Boolean =
    this is AppState.Menu || this is AppState.Leaderboard

private fun AppState.activeTab(): Tab? =
    when (this) {
        is AppState.Menu -> Tab.HOME
        is AppState.Daily -> Tab.DAILY
        is AppState.DuelSetup -> Tab.DUEL
        is AppState.Leaderboard -> Tab.TOP
        is AppState.Profile -> Tab.PROFILE
        else -> null
    }

/**
 * Navigation-contract shell. Three archetypes drive the chrome:
 *  - Root tab → scaffold top bar (Home/Leaderboard only) + pinned bottom nav.
 *  - Pushed / Immersive → no scaffold chrome; the screen owns its own header.
 *
 * Daily/Duel/Profile are root tabs (bottom nav shows) but keep their existing in-screen
 * headers for now — full per-archetype top-bar unification is tracked in V2_REDESIGN_PLAN.md.
 */
@Composable
fun MainScaffold(
    appState: AppState,
    coins: Int,
    notifCount: Int,
    onTab: (tabIndex: Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenShop: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(QuizColors.paper)) {
        if (appState.scaffoldOwnsTopBar()) {
            RootTopBar(
                appState = appState,
                coins = coins,
                notifCount = notifCount,
                onOpenSettings = onOpenSettings,
                onOpenShop = onOpenShop,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
        if (appState.isRootTab()) {
            BottomNav(active = appState.activeTab(), onTab = onTab)
        }
    }
}

@Composable
private fun RootTopBar(
    appState: AppState,
    coins: Int,
    notifCount: Int,
    onOpenSettings: () -> Unit,
    onOpenShop: () -> Unit,
) {
    val isLeaderboard = appState is AppState.Leaderboard
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(QuizColors.paper)
                .statusBarsPadding()
                .drawBehind {
                    // Faint diagonal hatching so the manga texture starts at the very top.
                    val step = 30.dp.toPx()
                    var x = -size.height
                    while (x < size.width) {
                        drawLine(
                            color = QuizColors.ink.copy(alpha = 0.05f),
                            start = Offset(x, size.height),
                            end = Offset(x + size.height, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                        x += step
                    }
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = QuizStrokes.panel.toPx(),
                    )
                }
                .heightIn(min = 52.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CoinsPill(coins = coins, onClick = onOpenShop)
        Text(
            text = if (isLeaderboard) "ТОП ИГРОКОВ" else "АНИМЕ КВИЗ!",
            modifier = Modifier.weight(1f).rotate(-1f),
            fontFamily = BangersFamily,
            fontSize = 20.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style =
                TextStyle(
                    shadow =
                        Shadow(
                            color = if (isLeaderboard) QuizColors.hintPurple else QuizColors.tint,
                            offset = Offset(3f, 3f),
                            blurRadius = 0f,
                        ),
                ),
        )
        if (!isLeaderboard) {
            IconBtn(icon = MangaIcons.Bell, badge = notifCount, onClick = {})
        }
        IconBtn(icon = MangaIcons.Settings, badge = 0, onClick = onOpenSettings)
    }
}

@Composable
private fun CoinsPill(
    coins: Int,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(QuizRadii.pill)
    Box(
        modifier =
            Modifier
                .padding(end = QuizStrokes.regular, bottom = QuizStrokes.regular)
                .drawBehind {
                    val o = QuizStrokes.regular.toPx()
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(o, o),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(size.height, size.height),
                    )
                }
                .height(36.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(QuizColors.coinLight, QuizColors.coinDeep)))
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = MangaIcons.Coin,
                contentDescription = null,
                tint = QuizColors.ink,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = coins.toString(),
                fontFamily = RussoOneFamily,
                fontSize = 13.sp,
                color = QuizColors.ink,
            )
        }
    }
}

@Composable
private fun IconBtn(
    icon: ImageVector,
    badge: Int,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .padding(end = QuizStrokes.regular, bottom = QuizStrokes.regular)
                    .drawBehind {
                        val o = QuizStrokes.regular.toPx()
                        val r = QuizRadii.button.toPx()
                        drawRoundRect(
                            color = QuizColors.ink,
                            topLeft = Offset(o, o),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(r, r),
                        )
                    }
                    .size(40.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(QuizStrokes.regular, QuizColors.ink, shape)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = QuizColors.ink,
                modifier = Modifier.size(22.dp),
            )
        }
        if (badge > 0) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(QuizColors.streakFire)
                        .border(1.5.dp, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    color = Color.White,
                )
            }
        }
    }
}

private val SPLOTCH =
    listOf(
        0.08f to 0.28f, 0.18f to 0.12f, 0.32f to 0.24f, 0.46f to 0.08f,
        0.60f to 0.22f, 0.74f to 0.10f, 0.88f to 0.26f, 0.96f to 0.18f,
        0.98f to 0.76f, 0.88f to 0.92f, 0.74f to 0.80f, 0.60f to 0.94f,
        0.46f to 0.82f, 0.32f to 0.96f, 0.18f to 0.84f, 0.06f to 0.92f,
    )

@Composable
private fun BottomNav(
    active: Tab?,
    onTab: (tabIndex: Int) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = QuizStrokes.panel.toPx(),
                    )
                }
                .background(Color.White)
                .navigationBarsPadding()
                .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
    ) {
        NavItem("Главная", MangaIcons.Home, active == Tab.HOME, Modifier.weight(1f)) { onTab(0) }
        NavItem("Дневной", MangaIcons.Daily, active == Tab.DAILY, Modifier.weight(1f)) { onTab(1) }
        NavItem("Дуэль", MangaIcons.Versus, active == Tab.DUEL, Modifier.weight(1f)) { onTab(2) }
        NavItem("Топ", MangaIcons.Leaderboard, active == Tab.TOP, Modifier.weight(1f)) { onTab(3) }
        NavItem("Профиль", MangaIcons.Profile, active == Tab.PROFILE, Modifier.weight(1f)) { onTab(4) }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .drawBehind {
                    if (!selected) return@drawBehind
                    val insetX = 6.dp.toPx()
                    val insetY = 2.dp.toPx()
                    val w = size.width - insetX * 2
                    val h = size.height + insetY * 2
                    val path =
                        Path().apply {
                            SPLOTCH.forEachIndexed { i, (fx, fy) ->
                                val px = insetX + fx * w
                                val py = -insetY + fy * h
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                    drawPath(path = path, color = QuizColors.ink)
                }
                .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else QuizColors.ink,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            color = if (selected) Color.White else QuizColors.ink,
        )
    }
}
