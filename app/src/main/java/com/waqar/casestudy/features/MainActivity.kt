package com.waqar.casestudy.features

import android.os.Bundle
import com.waqar.casestudy.R
import com.waqar.casestudy.base.view.BaseActivity
import com.waqar.casestudy.features.home.fragments.HomeFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show home fragment as default view
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().add(getContainerId(), HomeFragment()).commit()
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    override fun getContainerId(): Int {
        return R.id.content_container;
    }
}