package com.waqar.casestudy.common.interactor

import com.waqar.casestudy.base.interactor.BaseInteractor
import com.waqar.casestudy.core.model.ExerciseCount
import com.waqar.casestudy.core.model.ExerciseModel
import com.waqar.casestudy.core.model.enum.ExerciseStatusType
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class ExerciseInteractor : BaseInteractor() {

    fun getExerciseItems(): Observable<List<ExerciseModel>> {
        return Observable.create { emitter ->
            val storedExercise = exerciseDao.getExercises()
            emitter.onNext(storedExercise)

            apiClient.getExercises()
                .subscribeOn(Schedulers.io())
                .subscribe({ exercises ->
                    saveExercises(exercises)
                    emitter.onNext(exerciseDao.getExercises())
                    emitter.onComplete()
                }, { error ->
                    emitter.onError(error)
                })
        }
    }

    fun getSavedExercisesItems(): Observable<List<ExerciseModel>> {
        return Observable.create { emitter ->
            val storedExercise = exerciseDao.getExercises()
            emitter.onNext(storedExercise)
            emitter.onComplete()
        }
    }

    fun getCompletedExercisesCount(): Observable<Int> {
        return Observable.create { emitter ->
            val completedExerciseCount = exerciseDao.getCompletedExercisesCount(ExerciseStatusType.IS_COMPLETE.status)
            emitter.onNext(completedExerciseCount)
            emitter.onComplete()
        }
    }

    fun getAllExercisesCount(): Observable<Int> {
        return Observable.create { emitter ->
            val exerciseCount = exerciseDao.getAllExercisesCount()
            emitter.onNext(exerciseCount)
            emitter.onComplete()
        }
    }

    fun getExercisesCount(): Observable<ExerciseCount> {
        val totalExercisesCountObservable = getAllExercisesCount().subscribeOn(Schedulers.io())
        val completeExerciseCountObservable =
            getCompletedExercisesCount().subscribeOn(Schedulers.io())

        return Observable.zip(
            totalExercisesCountObservable,
            completeExerciseCountObservable,
            BiFunction { allExerciseCount, completeExerciseCount ->
                ExerciseCount(
                    allExerciseCount,
                    completeExerciseCount
                )
            })
    }

    fun updateExercise(exercise: ExerciseModel) {
        exerciseDao.update(exercise)
    }

    private fun saveExercises(exercises: List<ExerciseModel>) {
        exerciseDao.insertOrUpdateAll(exercises)
    }
}