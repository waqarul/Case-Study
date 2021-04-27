package com.waqar.casestudy.common.viewitems

import androidx.annotation.DrawableRes
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewitem.IViewItem

class ExerciseViewItem(
        val title: String,
        val url: String,
        @DrawableRes val statusDrawable: Int // isFavorite/Completed/Skipped
) : IViewItem() {
    constructor(title: String, url: String) : this(title, url, R.drawable.placeholder)

    override fun equals(other: Any?): Boolean {
        if (other !is ExerciseViewItem) return false

        return title == other.title && url == other.url && statusDrawable == other.statusDrawable
    }
}