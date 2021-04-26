package com.waqar.casestudy.base.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.core.navigation.INavHost
import com.waqar.casestudy.core.navigation.KEY_NAV_DESTINATION
import com.waqar.casestudy.core.navigation.NavDestination

abstract class BaseActivity : AppCompatActivity(), INavHost {

    private var viewArgs: Bundle? =
        null // Should only have value if there is no fragment in NavDestination to forward the params to.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentLayout())

        ButterKnife.bind(this)
        navigateOnIntent()
        supportFragmentManager.addOnBackStackChangedListener { updateTitleFromFragment() }
    }

    override fun openDestination(destination: NavDestination) {
        try {

            val fragmentClass = if (destination.fragmentClass == null) {
                viewArgs = destination.viewParams
                return
            } else {
                destination.fragmentClass
            }

            if (destination.fragmentClass?.name?.equals(getCurrentFragment()?.javaClass?.name) == true) {
                return
            }

            val fragment: BaseFragment<BaseViewModel> =
                fragmentClass?.newInstance() as BaseFragment<BaseViewModel>

            fragment.arguments = destination.viewParams

            var shouldReplace = false
            if (destination.activityParams != null) {
                val value =
                    destination.activityParams!!.get(NavDestination.KEY_ACTIVITY_PARAMS_FRAGMENT_ORDER)
                shouldReplace = value?.equals(NavDestination.VALUE_ACTIVITY_PARAMS_ROOT_FRAGMENT)
                    ?: false
            }

            if (shouldReplace) {
                addFragment(fragment, true)
            } else {
                addFragment(fragment, false)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateOnIntent(intent)
    }

    private fun navigateOnIntent(intent: Intent? = null) {
        val _intent = intent ?: this.intent
        val navDestination: NavDestination? =
            _intent?.getParcelableExtra(KEY_NAV_DESTINATION)

        if (navDestination != null) {
            openDestination(navDestination)
        }
    }

    private fun addFragment(fragment: Fragment, root: Boolean = false) {
        if (root) {
            popAllFragments()
        }
        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(getContainerId(), fragment)

        if (supportFragmentManager.findFragmentById(getContainerId()) != null) {
            transaction.addToBackStack(fragment.tag)
        }

        transaction.commit()
    }

    private fun popAllFragments() {
        val entryCount = supportFragmentManager.backStackEntryCount
        if (entryCount == 0) {

            for (fragment in supportFragmentManager.fragments) {
                if (fragment != null) {
                    supportFragmentManager.beginTransaction().detach(fragment).remove(fragment)
                        .commitNow()
                }
            }

            return
        }

        for (index in 1..entryCount) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(getContainerId())
        if (fragment is OnBackPressedHandler) {
            if (!fragment.shouldHandleBack()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(getContainerId())
    }


    private fun updateTitleFromFragment() {
        val fragment = getCurrentFragment()
        if (fragment is BaseFragment<*>) {
            val title = fragment.getTitle()
            if (title.isNotEmpty()) {
                setTitle(title)
            }
        } else {
            title = getActivityTitle();
        }
    }

    protected open fun getActivityTitle(): String {
        val applicationInfo = applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(
            stringId
        )
    }

    abstract fun getContentLayout(): Int

    open fun getContainerId(): Int {
        return 0
    }
}