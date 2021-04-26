package  com.waqar.casestudy.core.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import java.lang.ref.WeakReference
import java.util.*

class Navigator : INavigator {

    companion object {
        val TAG = "Navigator"
        lateinit var application: Application
        var currentActivity: WeakReference<Activity>? = null
        private var isPortrait: Boolean = true
        private var navAdapter: INavigationAdapter? = null
        private val pendingNavigationDestinations = Stack<NavDestination>()

        fun initialize(application: Application) {
            initialize(
                application,
                null
            )
        }

        fun initialize(application: Application, adapter: INavigationAdapter?) {
            Companion.application = application

            application.registerActivityLifecycleCallbacks(createLifeCycleCallbackHandler())
            application.registerComponentCallbacks(createComponentLifeCycleCallbackHandler())
            ProcessLifecycleOwner.get().lifecycle.addObserver(createLifecycleObserver())

            isPortrait =
                application.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            navAdapter = adapter
        }

        private fun createLifeCycleCallbackHandler(): Application.ActivityLifecycleCallbacks {
            return object : Application.ActivityLifecycleCallbacks {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Log.d(TAG, "Activity ${activity.localClassName} created")
                }

                override fun onActivityStarted(activity: Activity) {
                    Log.d(TAG, "Activity ${activity.localClassName} started")
                }

                override fun onActivityResumed(activity: Activity) {
                    Log.d(TAG, "Activity ${activity.localClassName} resumed")
                    currentActivity = WeakReference<Activity>(activity)
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    Log.d(TAG, "Activity ${activity.localClassName} onSaveInstance")
                }

                override fun onActivityPaused(activity: Activity) {
                    Log.d(TAG, "Activity ${activity.localClassName} paused")
                }

                override fun onActivityStopped(activity: Activity) {
                    Log.d(TAG, "Activity ${activity.localClassName} stopped")
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Log.d(TAG, "Activity ${activity.localClassName} destroyed")
                }
            }
        }

        private fun createComponentLifeCycleCallbackHandler(): ComponentCallbacks2 {
            return object : ComponentCallbacks2 {
                override fun onLowMemory() {
                    Log.d(TAG, "onLowMemory called")
                }

                override fun onConfigurationChanged(newConfig: Configuration) {
                    Log.d(TAG, "onConfigurationChanged")
                    isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                }

                @SuppressLint("SwitchIntDef")
                override fun onTrimMemory(level: Int) {
                    Log.d(TAG, "onTrimMemory called")
                }
            }
        }

        private fun createLifecycleObserver(): LifecycleObserver {
            return object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onMoveToForeground() {
                    Log.d(TAG, "The Application has entered the foreground")
                    LocalBroadcastManager.getInstance(application)
                        .sendBroadcast(Intent(FILTER_NAVIGATION_APP_FOREGROUND))
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onMoveToBackground() {
                    Log.d(TAG, "The Application has entered the background")
                    LocalBroadcastManager.getInstance(application)
                        .sendBroadcast(Intent(FILTER_NAVIGATION_APP_BACKGROUND))
                }
            }
        }
    }

    override fun clearNavigationStack() {
        pendingNavigationDestinations.clear()
    }

    override fun navigateToSameActivity(destination: String, data: Bundle?) {
        clearNavigationStack()
        val navDestination =
            navAdapter!!.getDestination(destination)
                ?: return // This intentionally put to throw NPE in case the adapter is not set.
        navDestination.navigationType = NavigationType.CURRENT_ACTIVITY
        doNavigate(navDestination, data)
    }

    override fun navigateToNewActivity(destination: String, data: Bundle?) {
        clearNavigationStack()
        val navDestination =
            navAdapter!!.getDestination(destination)
                ?: return // This intentionally put to throw NPE in case the adapter is not set.

        navDestination.navigationType = NavigationType.NEW_ACTIVITY
        doNavigate(navDestination, data)
    }

    private fun doNavigate(navDestination: NavDestination, data: Bundle?) {
        /**Navigation Conventions:
         * 1) If navigation type is CURRENT_ACTIVITY, or the activity class is null: we add the fragment to the current activity
         * 2) If the navigation type is NEW_ACTIVITY, or the fragment is null. We send an intent to open an activity.
         * 3) */

        val sameActivity = navDestination.navigationType == NavigationType.CURRENT_ACTIVITY

        if (data != null) {
            if (navDestination.viewParams == null) {
                navDestination.viewParams = data

            } else {

                navDestination.viewParams?.putAll(data)
            }
        }

        val activityClass = navDestination.activityClass
        var intentFlags = navDestination.activityFlags

        var context: Context? = currentActivity?.get()

        if (context == null) { // For some reason, we are navigating without any activity, in this case, we use the the app context, but we need to start a new task.

            context =
                application
            intentFlags =
                intentFlags.or(Intent.FLAG_ACTIVITY_NEW_TASK).or(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        } else if (context is INavHost && sameActivity) { // We have an activity, if it implements 'NavHost' then let us just hand it the destination.

            // This case give the ability to navigate to the same activity conditionaly if you give a class name. If the class name does not match, then this qualifies as "NEW_ACTIVITY"
            if ((navDestination.activityClass == null)
                || ((currentActivity?.get() != null) && (activityClass?.canonicalName?.equals(
                    currentActivity?.get()!!::class.java.canonicalName
                ) == true))
            ) {
                context.openDestination(navDestination)
                return
            }
        }

        if (activityClass == null) {
            Log.e(TAG, "activity class is null, doing nothing")
            return
        }

        val intent = Intent(context, activityClass)

        intent.flags = intentFlags
        intent.putExtra(KEY_NAV_DESTINATION, navDestination)
        context.startActivity(intent)
    }

    override fun navigateToSameActivity(destination: NavDestination, data: Bundle?) {
        clearNavigationStack()
        doNavigate(destination, data)
    }

    override fun navigateToNewActivity(destination: NavDestination, data: Bundle?) {
        clearNavigationStack()
        doNavigate(destination, data)
    }

    override fun navigate(destination: NavDestination, data: Bundle?) {
        clearNavigationStack()
        val dest = resetDefaults(destination)
        doNavigate(dest, data)
    }

    override fun navigate(destination: String, data: Bundle?) {
        clearNavigationStack()
        var dest = navAdapter!!.getDestination(destination) ?: return
        dest = resetDefaults(dest)
        doNavigate(dest, data)
    }

    override fun pushNext(destination: String, data: Bundle?, sameActivity: Boolean) {
        val dest = navAdapter?.getDestination(destination) ?: return
        if (dest.viewParams == null) {
            dest.viewParams = Bundle()
        }
        if (data != null) {
            dest.viewParams!!.putAll(data)
        }
        pendingNavigationDestinations.push(dest)
    }

    override fun pushNext(destination: NavDestination, data: Bundle?, sameActivity: Boolean) {
        if (destination.viewParams == null) {
            destination.viewParams = Bundle()
        }
        if (data != null) {
            destination.viewParams!!.putAll(data)
        }
        pendingNavigationDestinations.push(destination)
    }

    override fun next() {
        if (!pendingNavigationDestinations.isEmpty()) {
            val destination = pendingNavigationDestinations.pop()
            doNavigate(destination, null)
        }
    }

    override fun hasNext(): Boolean {
        return !pendingNavigationDestinations.isEmpty()
    }

    override fun back() {
        currentActivity?.get()?.onBackPressed()
    }

    override fun close() {
        currentActivity?.get()?.finish()
    }

    override fun getCurrentActivity(): Activity? {
        return currentActivity?.get()
    }

    private fun resetDefaults(destination: NavDestination): NavDestination {
        return destination
    }

}