package com.waqar.casestudy.core.navigation

import android.content.Context
import android.os.Bundle
import com.waqar.casestudy.constants.NavigationConstants
import com.waqar.casestudy.features.exercise.fragments.ExerciseFragment
import com.waqar.casestudy.features.MainActivity
import com.waqar.casestudy.features.posedetector.fragment.PoseDetectorFragment
import com.waqar.casestudy.features.summary.fragment.SummaryFragment

class NavigationAdapter(var context: Context) : INavigationAdapter {
    override fun getDestination(destination: String): NavDestination? {
        return when (destination) {
            NavigationConstants.POSE_DETECTOR -> buildPoseDetectorDestination()
            NavigationConstants.EXERCISE -> buildExerciseDestination()
            NavigationConstants.SUMMARY -> buildSummaryDestination()
            else -> null
        }
    }

    private fun buildPoseDetectorDestination(): NavDestination {
        val params = Bundle()
        params.putString(
            NavDestination.KEY_ACTIVITY_PARAMS_FRAGMENT_ORDER,
            NavDestination.VALUE_ACTIVITY_PARAMS_ADD_FRAGMENT
        )
        return NavDestination.Builder()
            .setNavigationType(NavigationType.CURRENT_ACTIVITY)
            .setActivityClass(MainActivity::class.java)
            .setActivityParams(params)
            .setFragmentClass(PoseDetectorFragment::class.java)
            .build()
    }

    private fun buildExerciseDestination(): NavDestination {
        val params = Bundle()
        params.putString(
            NavDestination.KEY_ACTIVITY_PARAMS_FRAGMENT_ORDER,
            NavDestination.VALUE_ACTIVITY_PARAMS_ADD_FRAGMENT
        )
        return NavDestination.Builder()
            .setNavigationType(NavigationType.CURRENT_ACTIVITY)
            .setActivityClass(MainActivity::class.java)
            .setActivityParams(params)
            .setFragmentClass(ExerciseFragment::class.java)
            .build()
    }

    private fun buildSummaryDestination(): NavDestination {
        val params = Bundle()
        params.putString(
            NavDestination.KEY_ACTIVITY_PARAMS_FRAGMENT_ORDER,
            NavDestination.VALUE_ACTIVITY_PARAMS_ADD_FRAGMENT
        )
        return NavDestination.Builder()
            .setNavigationType(NavigationType.CURRENT_ACTIVITY)
            .setActivityClass(MainActivity::class.java)
            .setActivityParams(params)
            .setFragmentClass(SummaryFragment::class.java)
            .build()
    }

}