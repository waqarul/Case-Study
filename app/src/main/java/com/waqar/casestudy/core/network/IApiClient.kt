package com.waqar.casestudy.core.network

import com.waqar.casestudy.core.model.ExerciseModel
import io.reactivex.Observable

interface IApiClient {
    fun getExercises(): Observable<List<ExerciseModel>>
}