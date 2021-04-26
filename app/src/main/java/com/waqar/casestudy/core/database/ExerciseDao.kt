package com.waqar.casestudy.core.database

import androidx.room.Dao
import androidx.room.Query
import com.waqar.casestudy.constants.DatabaseConstants
import com.waqar.casestudy.core.model.ExerciseModel


@Dao
interface ExerciseDao : BaseDao<ExerciseModel> {

    // This method returns the all exercises
    @Query(DatabaseConstants.QUERY_SELECT_ALL_EXERCISE)
    fun getExercises(): List<ExerciseModel>

    // This method returns the completed exercise count
    @Query(DatabaseConstants.QUERY_GET_EXERCISES_STATUS_COUNT)
    fun getCompletedExercisesCount(status: Int): Int

    // This method is used to return the no. of records
    @Query(DatabaseConstants.QUERY_GET_ALL_EXERCISES_COUNT)
    fun getAllExercisesCount(): Int

    // This method is used to delete all exercises
    @Query(DatabaseConstants.QUERY_DELETE_ALL_EXERCISES)
    fun deleteAllExercises()

    // This method is used to return the exercise
    @Query(DatabaseConstants.QUERY_SELECT_EXERCISE)
    fun getExercise(id: Long): ExerciseModel?

    // This method is used to insert item if not exist,
    // otherwise update the item
    fun insertOrUpdateAll(exercises: List<ExerciseModel>) {
        exercises.forEach {
            val temp = getExercise(it.id)
            temp?.let { exercise ->
                exercise.name = it.name
                exercise.coverImageUrl = it.coverImageUrl
                exercise.videoUrl = it.videoUrl
                update(exercise)
            } ?: kotlin.run { insert(it) }
        }
    }
}