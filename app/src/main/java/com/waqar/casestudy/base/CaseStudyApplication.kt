package com.waqar.casestudy.base

import android.app.Application
import com.waqar.casestudy.core.navigation.NavigationAdapter
import com.waqar.casestudy.core.navigation.Navigator

class CaseStudyApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()

        initialize()

        initializeNavigation()
    }

    private fun initialize() {
        instance = this

        Domain.integrateWith(this)
    }

    private fun initializeNavigation() {
        Navigator.initialize(this, NavigationAdapter(this))
    }

}