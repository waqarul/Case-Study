package com.waqar.casestudy.features.home.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import com.waqar.casestudy.R
import com.waqar.casestudy.base.view.AbstractToolbarFragment
import com.waqar.casestudy.common.adapters.ExerciseRecyclerViewAdapter
import com.waqar.casestudy.features.home.viewmodel.HomeViewModel

class HomeFragment : AbstractToolbarFragment<HomeViewModel>(),
    SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.rv_exercise)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.tv_no_record)
    lateinit var tvNoRecordFound: TextView

    @BindView(R.id.btn_start)
    lateinit var btnStart: Button

    @BindView(R.id.swipe_container)
    lateinit var swipeContainer: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setupSwipeRefreshLayout()

        requestAllRequiredPermissions()
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.home_label)
    }

    override fun getViewLayout(): Int {
        return R.layout.fragment_home
    }

    override fun getOrCreateViewModel(): HomeViewModel {
        return ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    override fun setupBindings() {
        super.setupBindings()

        viewModel.viewItems.observe(viewLifecycleOwner, { viewItems ->
            val adapter = recyclerView.adapter as ExerciseRecyclerViewAdapter
            adapter.setViewItems(viewItems)
        })

        viewModel.showRefreshIndicator.observe(viewLifecycleOwner, { shouldRefresh ->
            swipeContainer.isRefreshing = shouldRefresh
        })

        viewModel.isRecordFound.observe(viewLifecycleOwner, { isRecordFound ->
            tvNoRecordFound.visibility = if (isRecordFound) View.GONE else View.VISIBLE
            recyclerView.visibility = if (isRecordFound) View.VISIBLE else View.GONE
            btnStart.visibility = if (isRecordFound) View.VISIBLE else View.GONE
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData(arguments)
    }

    override fun onRefresh() {
        swipeContainer.isRefreshing = true
        viewModel.loadData()
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = ExerciseRecyclerViewAdapter(createAdapterListener())
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    private fun createAdapterListener(): ExerciseRecyclerViewAdapter.OnItemClickedListener {
        return object : ExerciseRecyclerViewAdapter.OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                //viewModel.onItemClicked(position)
            }

            override fun onItemStatusClicked(position: Int) {
                viewModel.onItemFavoriteClicked(position)
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeContainer.setOnRefreshListener(this)

        // Scheme colors for animation
        swipeContainer.setColorSchemeColors(
            ContextCompat.getColor(swipeContainer.context, android.R.color.holo_blue_bright),
            ContextCompat.getColor(swipeContainer.context, android.R.color.holo_green_light),
            ContextCompat.getColor(swipeContainer.context, android.R.color.holo_orange_light),
            ContextCompat.getColor(swipeContainer.context, android.R.color.holo_red_light)
        )
    }

    @OnClick(R.id.btn_start)
    fun onNavigateToPoseDetector() {
        viewModel.navigateToPoseDetector()
    }

    override fun onDestroyView() {
        viewModel.viewItems.removeObservers(viewLifecycleOwner)
        viewModel.showRefreshIndicator.removeObservers(viewLifecycleOwner)
        viewModel.isRecordFound.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}