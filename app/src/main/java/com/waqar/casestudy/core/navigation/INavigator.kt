package  com.waqar.casestudy.core.navigation

import android.app.Activity
import android.os.Bundle

interface INavigator : IActivityProvider {
    // Add navigation type to destination, navigate immediately to destination using adapter to get 'NavDestination', and reset the internal navigation stack.
    fun navigateToSameActivity(destination: String, data: Bundle? = Bundle())

    fun navigateToNewActivity(destination: String, data: Bundle? = Bundle())

    // Add navigation type to destination, navigate immediately to destination, and reset the internal navigation stack.
    fun navigateToSameActivity(destination: NavDestination, data: Bundle? = Bundle())

    fun navigateToNewActivity(destination: NavDestination, data: Bundle? = Bundle())

    // Navigate immediately, and reset the internal navigation stack.
    fun navigate(destination: NavDestination, data: Bundle? = Bundle())

    // Navigate immediately to destination using adapter to get 'NavDestination', and reset internal navigation stack.
    fun navigate(destination: String, data: Bundle? = Bundle())

    // Add next to navigation stack.
    fun pushNext(destination: String, data: Bundle? = Bundle(), sameActivity: Boolean = true)

    // Add back to navigation stack.
    fun pushNext(destination: NavDestination, data: Bundle? = Bundle(), sameActivity: Boolean = true)

    // Go to the next destination in the navigation stack
    fun next()

    // Go back.
    fun back()

    // Close
    fun close()

    // Clear navigation stack
    fun clearNavigationStack()


    fun hasNext(): Boolean
}

interface IActivityProvider {
    fun getCurrentActivity(): Activity?
}