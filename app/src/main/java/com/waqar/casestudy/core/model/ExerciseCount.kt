package com.waqar.casestudy.core.model

data class ExerciseCount(
    val totalCount: Int,
    val completeCount: Int
) {
    val skipCount = totalCount - completeCount
}