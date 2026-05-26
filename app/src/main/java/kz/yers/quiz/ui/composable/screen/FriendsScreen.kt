package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.AddFriendResult
import kz.yers.quiz.model.FriendCard
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import kz.yers.quiz.utils.shareText

@Composable
fun FriendsScreen(
    myCode: String?,
    friends: List<FriendCard>,
    loading: Boolean,
    myBestScore: Int,
    addStatus: AddFriendResult?,
    onAddFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onClearStatus: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    LaunchedEffect(addStatus) {
        if (addStatus == AddFriendResult.Ok) input = ""
    }
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { MyCodeCard(myCode) }
        item {
            AddFriendCard(
                input = input,
                onInputChange = {
                    input = it
                    if (addStatus != null) onClearStatus()
                },
                onAdd = { onAddFriend(input) },
                status = addStatus,
            )
        }
        item {
            Text(
                text = "ВАШИ ДРУЗЬЯ · ${friends.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
        }
        if (friends.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    SpeechBubble(
                        text =
                            if (loading) {
                                "Загружаем…"
                            } else {
                                "Пока никого.\nДобавь друга по коду выше!"
                            },
                    )
                }
            }
        } else {
            items(friends, key = { it.uid }) { friend ->
                FriendRow(
                    friend = friend,
                    myBestScore = myBestScore,
                    onRemove = { onRemoveFriend(friend.uid) },
                )
            }
        }
    }
}

@Composable
private fun MyCodeCard(myCode: String?) {
    val context = LocalContext.current
    OffsetCard(background = QuizColors.paper2) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "ВАШ КОД ДРУГА",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = myCode ?: "недоступен — нужен интернет",
                fontFamily = RussoOneFamily,
                fontSize = 14.sp,
                color = QuizColors.ink,
            )
            Spacer(Modifier.height(12.dp))
            MangaButton(
                label = "ПОДЕЛИТЬСЯ КОДОМ",
                variant = if (myCode != null) MangaButtonVariant.Tint else MangaButtonVariant.Ghost,
                onClick = {
                    myCode?.let { code ->
                        shareText(
                            context = context,
                            text = "Добавь меня в Аниме Квиз! Мой код друга:\n$code",
                            chooserTitle = "Поделиться кодом",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minHeight = 46.dp,
            )
        }
    }
}

@Composable
private fun AddFriendCard(
    input: String,
    onInputChange: (String) -> Unit,
    onAdd: () -> Unit,
    status: AddFriendResult?,
) {
    OffsetCard(background = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "ДОБАВИТЬ ДРУГА",
                fontFamily = RussoOneFamily,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink,
            )
            Spacer(Modifier.height(10.dp))
            val shape = RoundedCornerShape(QuizRadii.button)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(QuizColors.paper)
                        .border(QuizStrokes.panel, QuizColors.ink, shape)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { onInputChange(it.trim()) },
                    singleLine = true,
                    cursorBrush = SolidColor(QuizColors.ink),
                    textStyle =
                        TextStyle(color = QuizColors.ink, fontFamily = RussoOneFamily, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (input.isEmpty()) {
                            Text(
                                text = "Вставьте код друга",
                                fontSize = 14.sp,
                                color = QuizColors.ink.copy(alpha = 0.35f),
                            )
                        }
                        inner()
                    },
                )
            }
            status?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text =
                        when (it) {
                            AddFriendResult.Ok -> "Друг добавлен ✓"
                            AddFriendResult.NotFound -> "Игрок не найден. Проверьте код."
                            AddFriendResult.Self -> "Это ваш собственный код 🙂"
                            AddFriendResult.Offline -> "Нет соединения. Попробуйте позже."
                        },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (it == AddFriendResult.Ok) QuizColors.success else QuizColors.error,
                )
            }
            Spacer(Modifier.height(12.dp))
            MangaButton(
                label = "ДОБАВИТЬ",
                variant = if (input.isNotBlank()) MangaButtonVariant.Tint else MangaButtonVariant.Ghost,
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 46.dp,
            )
        }
    }
}

@Composable
private fun FriendRow(
    friend: FriendCard,
    myBestScore: Int,
    onRemove: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.panel, QuizColors.ink, shape),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(QuizColors.tintGlow)
                        .border(QuizStrokes.regular, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = friend.name.take(1).uppercase(),
                    fontFamily = RussoOneFamily,
                    fontSize = 18.sp,
                    color = QuizColors.ink,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    fontFamily = RussoOneFamily,
                    fontSize = 15.sp,
                    color = QuizColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = comparisonLabel(friend.bestScore, myBestScore),
                    fontSize = 12.sp,
                    color = QuizColors.ink.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(QuizColors.paper2)
                        .border(QuizStrokes.regular, QuizColors.ink, CircleShape)
                        .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    // Back glyph was semantically wrong for "remove"; Close (×) reads as delete.
                    imageVector = MangaIcons.Close,
                    contentDescription = "Удалить друга",
                    tint = QuizColors.ink,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun comparisonLabel(
    friendScore: Int?,
    myBestScore: Int,
): String {
    if (friendScore == null) return "Рекорд скрыт"
    val diff = friendScore - myBestScore
    return when {
        diff > 0 -> "Рекорд $friendScore · впереди вас на $diff"
        diff < 0 -> "Рекорд $friendScore · вы впереди на ${-diff}"
        else -> "Рекорд $friendScore · ничья"
    }
}

@Composable
private fun OffsetCard(
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
