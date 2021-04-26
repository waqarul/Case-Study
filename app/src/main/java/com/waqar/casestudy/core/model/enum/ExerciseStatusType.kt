package com.waqar.casestudy.core.model.enum

enum class ExerciseStatusType(val status: Int) {

    NONE(0),
    IS_COMPLETE(1),
    IS_SKIP(2);

    companion object {
        private val map = values().associateBy(ExerciseStatusType::status)
        fun fromValue(type: Int) = map[type]
    }
}