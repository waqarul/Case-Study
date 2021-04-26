package com.waqar.casestudy.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.waqar.casestudy.constants.DatabaseConstants
import com.waqar.casestudy.core.model.enum.ExerciseStatusType

@JsonClass(generateAdapter = true)
@Entity(tableName = DatabaseConstants.TABLE_EXERCISE)
data class ExerciseModel(
        @PrimaryKey
        @Json(name = "id") val id: Long,
        @Json(name = "name") var name: String,
        @Json(name = "cover_image_url") var coverImageUrl: String,
        @Json(name = "video_url") var videoUrl: String,
        var isFavorite: Boolean = false,
        var status: Int = ExerciseStatusType.NONE.status
)