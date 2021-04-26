package  com.waqar.casestudy.core.navigation

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.waqar.casestudy.base.view.BaseFragment
import com.waqar.casestudy.base.viewmodel.BaseViewModel


data class NavDestination(
    var navigationType: NavigationType = NavigationType.CURRENT_ACTIVITY,

    var activityClass: Class<out Any>? = null,
    var activityFlags: Int = 0,
    var activityParams: Bundle?,

    var fragmentClass: Class<out BaseFragment<out BaseViewModel>>? = null,
    var viewParams: Bundle? = null
) : Parcelable {
    companion object {
        const val KEY_ACTIVITY_PARAMS_FRAGMENT_ORDER = "key.activity.params.fragment.order"

        const val VALUE_ACTIVITY_PARAMS_ADD_FRAGMENT = "value.activity.params.fragment.order.add"
        const val VALUE_ACTIVITY_PARAMS_REPLACE_FRAGMENT =
            "value.activity.params.fragment.order.replace"
        const val VALUE_ACTIVITY_PARAMS_ROOT_FRAGMENT = "value.activity.params.fragment.order.root"

        @JvmField
        val CREATOR = object : Parcelable.Creator<NavDestination> {
            override fun createFromParcel(parcel: Parcel): NavDestination {
                return NavDestination(
                    parcel
                )
            }

            override fun newArray(size: Int): Array<NavDestination?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        navigationType = NavigationType.values()[parcel.readInt()],
        activityClass = parcel.readSerializable() as Class<out Any>?,
        activityFlags = parcel.readInt(),
        activityParams = parcel.readBundle(Bundle::class.java.classLoader),
        fragmentClass = parcel.readSerializable() as Class<out BaseFragment<out BaseViewModel>>?,
        viewParams = parcel.readBundle()
    ) {
    }

    constructor() : this(NavigationType.CURRENT_ACTIVITY, null, 0, Bundle(), null, Bundle())


    class Builder {
        private val destination =
            NavDestination()

        fun setNavigationType(navigationType: NavigationType): Builder {
            destination.navigationType = navigationType
            return this
        }

        fun setActivityClass(activityClass: Class<out Any>?): Builder {
            destination.activityClass = activityClass
            return this
        }

        fun setActivityFlags(flags: Int): Builder {
            destination.activityFlags = flags
            return this
        }

        fun setActivityParams(params: Bundle): Builder {
            destination.activityParams = params
            return this
        }

        fun setFragmentClass(fragmentClass: Class<out BaseFragment<out BaseViewModel>>?): Builder {
            destination.fragmentClass = fragmentClass
            return this
        }

        fun setViewParams(params: Bundle): Builder {
            destination.viewParams = params
            return this
        }

        fun build(): NavDestination {
            return destination
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(navigationType.ordinal)
        parcel.writeSerializable(activityClass)
        parcel.writeInt(activityFlags)
        parcel.writeBundle(activityParams)
        parcel.writeSerializable(fragmentClass)
        parcel.writeBundle(viewParams)
    }

    override fun describeContents(): Int {
        return 0
    }
}

enum class NavigationType {
    CURRENT_ACTIVITY, // We try to add it to the current activity
    NEW_ACTIVITY // We send an intent to open a new activity. This can be changed by changing the launch mode of the activity.
}