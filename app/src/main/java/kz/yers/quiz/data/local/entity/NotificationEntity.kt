package kz.yers.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** [kz.yers.quiz.data.notifications.NotificationType] name. */
    val type: String,
    val title: String,
    val body: String,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
