package com.waqar.casestudy.common.diffutil

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.waqar.casestudy.base.viewitem.IViewItem
import com.waqar.casestudy.common.viewitems.ExerciseViewItem

class ExerciseViewItemDiffCallback(
        private val oldList: List<IViewItem>,
        private val newList: List<IViewItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        if (oldItem is ExerciseViewItem && newItem is ExerciseViewItem) {
            return oldItem.title == newItem.title
        }

        return true
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem is ExerciseViewItem && newItem is ExerciseViewItem) {
            return oldItem == newItem
        }

        return true
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}
