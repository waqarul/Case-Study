package com.waqar.casestudy.core.network

import com.waqar.casestudy.constants.ApiConstants
import com.waqar.casestudy.core.model.ExerciseModel
import io.reactivex.Observable
import retrofit2.http.GET

interface CaseStudyService {
    @GET(ApiConstants.GET_EXERCISES)
    fun getExercises(): Observable<List<ExerciseModel>>
}