package kz.yers.quiz.ui.composable.manga

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kz.yers.quiz.model.AchievementId
import kz.yers.quiz.model.GameMode

object MangaIcons {
    private fun build(
        name: String,
        block: ImageVector.Builder.() -> Unit,
    ): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

    private fun ImageVector.Builder.stroke(
        width: Float = 2f,
        block: PathBuilder.() -> Unit,
    ) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }

    private fun ImageVector.Builder.fill(
        fillType: PathFillType = PathFillType.NonZero,
        block: PathBuilder.() -> Unit,
    ) {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = fillType,
            pathBuilder = block,
        )
    }

    val Profile: ImageVector =
        build("Profile") {
            stroke {
                moveTo(12f, 5f)
                arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 7f)
                arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -7f)
                close()
                moveTo(4.5f, 20.5f)
                curveTo(4.5f, 15.5f, 8.5f, 14f, 12f, 14f)
                curveTo(15.5f, 14f, 19.5f, 15.5f, 19.5f, 20.5f)
            }
        }

    val Settings: ImageVector =
        build("Settings") {
            stroke {
                // 8-tooth gear outline
                moveTo(21.5f, 12f)
                lineTo(18.46f, 14.68f)
                lineTo(18.72f, 18.72f)
                lineTo(14.68f, 18.46f)
                lineTo(12f, 21.5f)
                lineTo(9.32f, 18.46f)
                lineTo(5.28f, 18.72f)
                lineTo(5.54f, 14.68f)
                lineTo(2.5f, 12f)
                lineTo(5.54f, 9.32f)
                lineTo(5.28f, 5.28f)
                lineTo(9.32f, 5.54f)
                lineTo(12f, 2.5f)
                lineTo(14.68f, 5.54f)
                lineTo(18.72f, 5.28f)
                lineTo(18.46f, 9.32f)
                close()
                moveTo(15f, 12f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6f, 0f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 0f)
                close()
            }
        }

    val ChevronRight: ImageVector =
        build("ChevronRight") {
            stroke {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }

    val Clock: ImageVector =
        build("Clock") {
            stroke {
                // Outer ring
                moveTo(21f, 12f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, 0f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 0f)
                close()
                // Hour hand
                moveTo(12f, 12f)
                lineTo(12f, 7f)
                // Minute hand
                moveTo(12f, 12f)
                lineTo(16f, 14f)
            }
        }

    val Swords: ImageVector =
        build("Swords") {
            stroke {
                // Sword 1: NW blade to SE hilt
                moveTo(4f, 4f)
                lineTo(8f, 4f)
                lineTo(8f, 8f)
                lineTo(20f, 20f)
                moveTo(18f, 20f)
                lineTo(20f, 20f)
                lineTo(20f, 18f)
                // Sword 2: NE blade to SW hilt
                moveTo(20f, 4f)
                lineTo(16f, 4f)
                lineTo(16f, 8f)
                lineTo(4f, 20f)
                moveTo(6f, 20f)
                lineTo(4f, 20f)
                lineTo(4f, 18f)
            }
        }

    /** Power level I — single manga energy spark (4-point sparkle). */
    val ModeEasy: ImageVector =
        build("ModeEasy") {
            fill {
                moveTo(12f, 3f)
                lineTo(13f, 11f)
                lineTo(21f, 12f)
                lineTo(13f, 13f)
                lineTo(12f, 21f)
                lineTo(11f, 13f)
                lineTo(3f, 12f)
                lineTo(11f, 11f)
                close()
            }
        }

    /** Power level II — twin sparkle burst, diagonal. */
    val ModeNormal: ImageVector =
        build("ModeNormal") {
            fill {
                // Larger sparkle (upper-left)
                moveTo(9f, 3f)
                lineTo(10f, 9.5f)
                lineTo(16.5f, 10.5f)
                lineTo(10f, 11.5f)
                lineTo(9f, 18f)
                lineTo(8f, 11.5f)
                lineTo(1.5f, 10.5f)
                lineTo(8f, 9.5f)
                close()
            }
            fill {
                // Smaller satellite sparkle (lower-right)
                moveTo(17f, 13f)
                lineTo(17.7f, 16.5f)
                lineTo(21.5f, 17.2f)
                lineTo(17.7f, 17.9f)
                lineTo(17f, 21.5f)
                lineTo(16.3f, 17.9f)
                lineTo(12.5f, 17.2f)
                lineTo(16.3f, 16.5f)
                close()
            }
        }

    /** Power level "?" — chaotic shuffle, two crossing curved arrows. */
    val ModeRandom: ImageVector =
        build("ModeRandom") {
            stroke(width = 2.5f) {
                // Top curve, rightward
                moveTo(4f, 9f)
                curveTo(7f, 5f, 17f, 5f, 20f, 9f)
                moveTo(17f, 6f)
                lineTo(20f, 9f)
                lineTo(17f, 12f)
                // Bottom curve, leftward
                moveTo(20f, 15f)
                curveTo(17f, 19f, 7f, 19f, 4f, 15f)
                moveTo(7f, 12f)
                lineTo(4f, 15f)
                lineTo(7f, 18f)
            }
        }

    /** Power level MAX — skull with sharp diamond eye sockets. */
    val ModeShit: ImageVector =
        build("ModeShit") {
            fill(PathFillType.EvenOdd) {
                // Cranium + jaw silhouette with tooth row
                moveTo(5f, 11f)
                curveTo(5f, 4f, 19f, 4f, 19f, 11f)
                lineTo(19f, 14f)
                curveTo(19f, 15.5f, 18f, 16f, 17f, 16f)
                lineTo(17f, 19f)
                lineTo(15f, 19f)
                lineTo(15f, 17f)
                lineTo(13f, 17f)
                lineTo(13f, 19f)
                lineTo(11f, 19f)
                lineTo(11f, 17f)
                lineTo(9f, 17f)
                lineTo(9f, 19f)
                lineTo(7f, 19f)
                lineTo(7f, 16f)
                curveTo(6f, 16f, 5f, 15.5f, 5f, 14f)
                close()
                // Left eye (diamond)
                moveTo(8f, 8.5f)
                lineTo(10.5f, 11f)
                lineTo(8f, 13.5f)
                lineTo(5.5f, 11f)
                close()
                // Right eye (diamond)
                moveTo(16f, 8.5f)
                lineTo(18.5f, 11f)
                lineTo(16f, 13.5f)
                lineTo(13.5f, 11f)
                close()
            }
        }

    /** Manga flame — sharp peak, tapered shoulders, plump base, hollow inner tongue. */
    val Flame: ImageVector =
        build("Flame") {
            fill(PathFillType.EvenOdd) {
                // Outer silhouette — pointed top, tapered shoulders, plump base
                moveTo(12f, 2f)
                curveTo(13.5f, 5f, 16f, 7.5f, 16.5f, 10.5f)
                curveTo(17f, 13f, 18f, 14f, 19f, 16f)
                curveTo(20.5f, 19.5f, 17f, 22f, 12f, 22f)
                curveTo(7f, 22f, 3.5f, 19.5f, 5f, 16f)
                curveTo(6f, 14f, 7f, 13f, 7.5f, 10.5f)
                curveTo(8f, 7.5f, 10.5f, 5f, 12f, 2f)
                close()
                // Hollow inner flame tongue
                moveTo(12f, 10f)
                curveTo(11f, 12.5f, 10f, 15f, 10.5f, 17.5f)
                curveTo(11f, 19f, 13f, 19f, 13.5f, 17.5f)
                curveTo(14f, 15f, 13f, 12.5f, 12f, 10f)
                close()
            }
        }

    /** Trophy with handles + base. */
    val Trophy: ImageVector =
        build("Trophy") {
            stroke {
                // Cup
                moveTo(8f, 3f)
                horizontalLineTo(16f)
                verticalLineTo(11f)
                curveTo(16f, 14f, 14f, 16f, 12f, 16f)
                curveTo(10f, 16f, 8f, 14f, 8f, 11f)
                close()
                // Left handle
                moveTo(8f, 5f)
                curveTo(5f, 5f, 4f, 8f, 5f, 11f)
                // Right handle
                moveTo(16f, 5f)
                curveTo(19f, 5f, 20f, 8f, 19f, 11f)
                // Stem + base
                moveTo(12f, 16f)
                verticalLineTo(19f)
                moveTo(8f, 19f)
                horizontalLineTo(16f)
                verticalLineTo(21f)
                horizontalLineTo(8f)
                close()
            }
        }

    /** Filled 5-point manga star. */
    val Star: ImageVector =
        build("Star") {
            fill {
                moveTo(12f, 2f)
                lineTo(14.7f, 9f)
                lineTo(22f, 9.5f)
                lineTo(16.3f, 14.2f)
                lineTo(18.2f, 21f)
                lineTo(12f, 17f)
                lineTo(5.8f, 21f)
                lineTo(7.7f, 14.2f)
                lineTo(2f, 9.5f)
                lineTo(9.3f, 9f)
                close()
            }
        }

    /** Z-shaped lightning bolt. */
    val Lightning: ImageVector =
        build("Lightning") {
            fill {
                moveTo(13f, 2f)
                lineTo(5f, 13f)
                lineTo(11f, 13f)
                lineTo(9f, 22f)
                lineTo(19f, 10f)
                lineTo(13f, 10f)
                close()
            }
        }

    /** Sunglasses (connoisseur). */
    val Sunglasses: ImageVector =
        build("Sunglasses") {
            stroke {
                // Left lens
                moveTo(3f, 9f)
                horizontalLineTo(10f)
                verticalLineTo(13f)
                curveTo(10f, 15f, 8f, 16f, 6.5f, 16f)
                curveTo(5f, 16f, 3f, 15f, 3f, 13f)
                close()
                // Right lens
                moveTo(14f, 9f)
                horizontalLineTo(21f)
                verticalLineTo(13f)
                curveTo(21f, 15f, 19f, 16f, 17.5f, 16f)
                curveTo(16f, 16f, 14f, 15f, 14f, 13f)
                close()
                // Bridge
                moveTo(10f, 10f)
                lineTo(14f, 10f)
            }
        }

    /** Medal: ribbon V + circular medal with "1" inside (EvenOdd hole). */
    val Medal: ImageVector =
        build("Medal") {
            fill(PathFillType.EvenOdd) {
                // Ribbon left
                moveTo(6f, 2f)
                lineTo(8f, 2f)
                lineTo(13f, 11f)
                lineTo(11f, 11f)
                close()
                // Ribbon right
                moveTo(16f, 2f)
                lineTo(18f, 2f)
                lineTo(13f, 11f)
                lineTo(11f, 11f)
                close()
                // Medal disc
                moveTo(17f, 16f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10f, 0f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 0f)
                close()
                // "1" cutout
                moveTo(11.4f, 13.5f)
                lineTo(12.6f, 13.5f)
                lineTo(12.6f, 18.5f)
                lineTo(11.4f, 18.5f)
                close()
            }
        }
}

val GameMode.icon: ImageVector
    get() =
        when (this) {
            GameMode.EASY -> MangaIcons.ModeEasy
            GameMode.NORMAL -> MangaIcons.ModeNormal
            GameMode.RANDOM -> MangaIcons.ModeRandom
            GameMode.SHIT -> MangaIcons.ModeShit
        }

val AchievementId.icon: ImageVector
    get() =
        when (this) {
            AchievementId.TopRanked -> MangaIcons.Trophy
            AchievementId.Streak30, AchievementId.Streak7 -> MangaIcons.Flame
            AchievementId.Played100, AchievementId.Played10 -> MangaIcons.Star
            AchievementId.Lightning -> MangaIcons.Lightning
            AchievementId.ConnoisseurFinish -> MangaIcons.Sunglasses
            AchievementId.FirstWin -> MangaIcons.Medal
        }
