package com.waqar.casestudy.core.network

import com.waqar.casestudy.BuildConfig
import com.waqar.casestudy.constants.ApiConstants
import com.waqar.casestudy.core.model.ExerciseModel
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitApiClient : IApiClient {

    private val caseStudyService by lazy {
        getRetrofit(ApiConstants.BASE_API_URL).create(
            CaseStudyService::class.java
        )
    }

    override fun getExercises(): Observable<List<ExerciseModel>> {
        return caseStudyService.getExercises()
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getHttpClient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    private fun getHttpClient(): OkHttpClient {

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient().newBuilder()
                .readTimeout(ApiConstants.READ_TIME_OUT_DELAY, TimeUnit.SECONDS)
                .connectTimeout(ApiConstants.CONNECT_TIME_OUT_DELAY, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build()
        }

        return OkHttpClient().newBuilder()
            .readTimeout(ApiConstants.READ_TIME_OUT_DELAY, TimeUnit.SECONDS)
            .connectTimeout(ApiConstants.CONNECT_TIME_OUT_DELAY, TimeUnit.SECONDS)
            .addInterceptor { chain -> chain.proceed(chain.request()) }
            .build()
    }
}