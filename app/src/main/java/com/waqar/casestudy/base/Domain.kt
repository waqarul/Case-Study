package com.waqar.casestudy.base

import android.app.Application

// https://www.linkedin.com/pulse/dependency-injection-clean-architecture-ahmed-adel-ismail/
object Domain {

    lateinit var applicationContext: Application
        private set

    fun integrateWith(application: Application) {
        applicationContext = application
    }
}