package com.waqar.casestudy.extentions

import com.waqar.casestudy.R
import com.waqar.casestudy.core.model.ExerciseModel
import com.waqar.casestudy.core.model.enum.ExerciseStatusType


fun ExerciseModel.getFavoriteIcon(): Int {
    return if (isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
}

fun ExerciseModel.getExerciseStatusIcon(): Int {
    return if (status == ExerciseStatusType.IS_COMPLETE.status) R.drawable.ic_complete else R.drawable.ic_skip
}
