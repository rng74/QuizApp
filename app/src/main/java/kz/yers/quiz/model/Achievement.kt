package kz.yers.quiz.model

enum class AchievementVariant { Gold, Silver, Fire, Purple, Regular }

enum class AchievementId(
    val title: String,
    val variant: AchievementVariant,
) {
    TopRanked("ТОП-10", AchievementVariant.Gold),
    Streak30("30 ДНЕЙ", AchievementVariant.Fire),
    Played100("100 ИГР", AchievementVariant.Silver),
    Lightning("МОЛНИЯ", AchievementVariant.Purple),
    ConnoisseurFinish("ШАРЮ", AchievementVariant.Regular),
    FirstWin("ПЕРВАЯ", AchievementVariant.Regular),
    Streak7("7 ДНЕЙ", AchievementVariant.Fire),
    Played10("10 ИГР", AchievementVariant.Silver),
}

data class UserStats(
    val totalGames: Int,
    val highScore: Int,
    val currentStreakDays: Int,
    val finishedConnoisseur: Boolean,
    val anyLightningRun: Boolean,
)

object Achievements {
    /**
     * Pure derivation — given the user's current stats, return the set of achievements they have
     * unlocked. Drives the badge case in Profile.
     */
    fun evaluate(stats: UserStats): Set<AchievementId> {
        val unlocked = mutableSetOf<AchievementId>()
        if (stats.totalGames >= 1) unlocked += AchievementId.FirstWin
        if (stats.totalGames >= 10) unlocked += AchievementId.Played10
        if (stats.totalGames >= 100) unlocked += AchievementId.Played100
        if (stats.currentStreakDays >= 7) unlocked += AchievementId.Streak7
        if (stats.currentStreakDays >= 30) unlocked += AchievementId.Streak30
        if (stats.highScore >= 100) unlocked += AchievementId.TopRanked
        if (stats.finishedConnoisseur) unlocked += AchievementId.ConnoisseurFinish
        if (stats.anyLightningRun) unlocked += AchievementId.Lightning
        return unlocked
    }
}
