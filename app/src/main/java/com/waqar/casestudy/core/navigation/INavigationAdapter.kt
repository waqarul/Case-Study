package  com.waqar.casestudy.core.navigation

interface INavigationAdapter {
    fun getDestination(destination: String): NavDestination?
}