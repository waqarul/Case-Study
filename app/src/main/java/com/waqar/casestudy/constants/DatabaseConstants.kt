package com.waqar.casestudy.constants

class DatabaseConstants {

    companion object {

        const val DATABASE_NAME = "kaia_database"
        const val DATABASE_VERSION = 1

        const val TABLE_EXERCISE = "Exercise"
        const val QUERY_SELECT_ALL_EXERCISE = "SELECT * FROM $TABLE_EXERCISE"
        const val QUERY_SELECT_EXERCISE = "SELECT * FROM $TABLE_EXERCISE WHERE id == :id"
        const val QUERY_DELETE_ALL_EXERCISES = "DELETE FROM $TABLE_EXERCISE"
        const val QUERY_GET_EXERCISES_STATUS_COUNT =
            "SELECT COUNT(id) FROM $TABLE_EXERCISE WHERE status == :status"
        const val QUERY_GET_ALL_EXERCISES_COUNT = "SELECT COUNT(id) FROM $TABLE_EXERCISE"
    }
}