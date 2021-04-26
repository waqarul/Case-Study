package com.waqar.casestudy.features.home.viewitems

import androidx.annotation.DrawableRes
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewitem.IViewItem

class ExerciseViewItem(
    val title: String,
    val url: String,
    @DrawableRes val statusDrawable: Int // isFavorite/Completed/Skipped
) : IViewItem() {
    constructor(title: String, url: String) : this(title, url, R.drawable.placeholder)
}