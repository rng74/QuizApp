package kz.yers.quiz.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object QuizColors {
    val tint = Color(0xFFFE9400)
    val tintSoft = Color(0x1FFE9400)
    val tintDeep = Color(0xFFE07A00)
    val tintGlow = Color(0xFFFFD35B)
    val ink = Color(0xFF0A0A0A)
    val paper = Color(0xFFFBF6EC)
    val paper2 = Color(0xFFF3EBD9)
    val streakFire = Color(0xFFFF3C2A)
    val hintPurple = Color(0xFF7B3AFF)
    val accentBlue = Color(0xFF006AFE)
    val success = Color(0xFF00D400)
    val error = Color(0xFFFE1500)

    val modeEasy = Color(0xFF2E9E5B)
    val modeNormal = tint
    val modeRandom = hintPurple
    val modeShit = Color(0xFFD12A2A)

    // Duel CTA gradient stops + leaderboard podium accents.
    val duelStart = Color(0xFF6A4CFF)
    val duelEnd = Color(0xFF3A8AC9)
    val gold = Color(0xFFFFD95B)
    val goldDeep = Color(0xFFFF9C00)
    val silver = Color(0xFFE6E6E6)
    val silverDeep = Color(0xFF888888)
    val bronze = Color(0xFFE2A36F)
    val bronzeDeep = Color(0xFFA05A2C)
    val coinLight = Color(0xFFFFF8B6)
    val coinDeep = Color(0xFFFFC933)
    val coinDark = Color(0xFFCC8800)
    val flameMid = Color(0xFFFF8A00)
    val flameHot = Color(0xFFFFEA00)

    // Achievement badge medals. Slightly muted vs. leaderboard podium gold/silver
    // so locked/unlocked states read clearly on the paper background. Use these
    // (not `gold`/`silver`) for ProfileScreen badges.
    val badgeSilver = Color(0xFFB0B0B0)

    // Onboarding "Без постера" demo card.
    val onboardingPosterTitle = Color(0xFF3A2A1D)
    val onboardingPosterBody = Color(0xFF8A4A2A)

    // Profile header gradient end (paired with `ink` for the dark-purple wash).
    val profileHeaderEnd = Color(0xFF3A2A4A)

    // BlurredImage placeholder + error backgrounds (dark muted neutrals).
    val posterPlaceholder = Color(0xFF1F1F1F)
    val posterError = Color(0xFF3A1F1F)

    // MangaChip red variant background.
    val chipRedBg = Color(0xFFFF726D)

    // StreakDay empty-state outline & MangaSwitchRow off-state track.
    val mutedStroke = Color(0xFF999999)
    val switchOffTrack = Color(0xFFDDDDDD)
}

object QuizStrokes {
    val thin: Dp = 1.5.dp
    val regular: Dp = 2.dp
    val panel: Dp = 2.5.dp
    val hero: Dp = 3.5.dp
}

object QuizShadows {
    val small: Dp = 2.dp
    val medium: Dp = 4.dp
    val large: Dp = 6.dp
}

object QuizRadii {
    val sharp: Dp = 0.dp
    val card: Dp = 6.dp
    val button: Dp = 8.dp
    val pill: Dp = 999.dp
}
