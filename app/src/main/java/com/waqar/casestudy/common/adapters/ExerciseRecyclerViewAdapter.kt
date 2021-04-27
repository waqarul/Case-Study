package com.waqar.casestudy.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewitem.IViewItem
import com.waqar.casestudy.common.diffutil.ExerciseViewItemDiffCallback
import com.waqar.casestudy.common.viewitems.ExerciseViewItem

class ExerciseRecyclerViewAdapter(val itemClickListener: OnItemClickedListener? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewItems: List<IViewItem> = ArrayList()

    fun setViewItems(viewItems: List<IViewItem>?) {
        viewItems?.let {
            val result =
                    DiffUtil.calculateDiff(ExerciseViewItemDiffCallback(this.viewItems, viewItems), false)
            this.viewItems = viewItems
            result.dispatchUpdatesTo(this)
        } ?: run {
            clear()
        }
    }

    private fun clear() {
        this.viewItems = ArrayList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when (viewType) {
            R.layout.exercise_view_item -> ExerciseViewHolder(view)
            else -> throw IllegalArgumentException("Unhandled view type in onCreateViewHolder ExerciseRecyclerViewAdapter.")
        }
    }

    override fun getItemCount(): Int {
        return viewItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (viewItems[position]) {
            is ExerciseViewItem -> R.layout.exercise_view_item
            else -> throw IllegalArgumentException("Unhandled view type in getItemViewType ExerciseRecyclerViewAdapter.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val viewItem = viewItems[position]) {
            is ExerciseViewItem -> {
                val viewHolder = (holder as ExerciseViewHolder)
                viewHolder.bindData(viewItem, position)
            }
        }
    }

    private inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var container: CardView = view.findViewById(R.id.cardView)
        private var title: TextView = view.findViewById(R.id.tv_title)
        private var coverImageView: ImageView = view.findViewById(R.id.iv_exercise)
        private var statusImageView: ImageView = view.findViewById(R.id.iv_status)

        fun bindData(viewItem: ExerciseViewItem, position: Int) {
            title.text = viewItem.title

            statusImageView.setImageResource(viewItem.statusDrawable)

            container.setOnClickListener { itemClickListener?.onItemClicked(position) }
            statusImageView.setOnClickListener { itemClickListener?.onItemStatusClicked(position) }

            Glide.with(coverImageView.context)
                .load(viewItem.url)
                .placeholder(R.drawable.placeholder)
                .skipMemoryCache(true)
                .into(coverImageView)
        }
    }

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
        fun onItemStatusClicked(position: Int)
    }
}