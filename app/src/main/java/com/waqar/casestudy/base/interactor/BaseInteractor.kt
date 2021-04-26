package com.waqar.casestudy.base.interactor

import com.waqar.casestudy.base.Domain
import com.waqar.casestudy.core.database.KaiaDatabase
import com.waqar.casestudy.core.network.IApiClient
import com.waqar.casestudy.core.network.RetrofitApiClient

abstract class BaseInteractor {
    protected val apiClient: IApiClient = RetrofitApiClient()
    protected val exerciseDao = KaiaDatabase.getDatabase(Domain.applicationContext).getExerciseDao()
}