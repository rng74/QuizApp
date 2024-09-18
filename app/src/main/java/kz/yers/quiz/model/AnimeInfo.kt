package kz.yers.quiz.model

import com.google.gson.annotations.SerializedName

data class AnimeInfo(
    @SerializedName("title_ru")
    val titleRu: String = "",
    @SerializedName("title_orig")
    val titleOrig: String = "",
    @SerializedName("album_name")
    val albumName: String = "",
    @SerializedName("song_name")
    val songName: String = "",
    @SerializedName("song_link")
    val songLink: String = "",
    @SerializedName("poster_link")
    val posterLink: String = "",
    @SerializedName("rating")
    val rating: Float = 0.0f
)