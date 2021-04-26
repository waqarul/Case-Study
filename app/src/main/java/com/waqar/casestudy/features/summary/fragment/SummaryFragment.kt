package com.waqar.casestudy.features.summary.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.waqar.casestudy.R
import com.waqar.casestudy.base.view.AbstractToolbarFragment
import com.waqar.casestudy.common.adapters.ExerciseRecyclerViewAdapter
import com.waqar.casestudy.features.home.viewmodel.SummaryViewModel

class SummaryFragment : AbstractToolbarFragment<SummaryViewModel>() {

    @BindView(R.id.rv_exercise)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.tv_summary)
    lateinit var tvSummaryExercise: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the screen orientation portrait.
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        showBackButton(true)

        setupRecyclerView()
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.exercise_label)
    }

    override fun loadData() {
        super.loadData()
        viewModel.loadData(arguments)
    }

    override fun getViewLayout(): Int {
        return R.layout.fragment_summary
    }

    override fun getOrCreateViewModel(): SummaryViewModel {
        return ViewModelProvider(this).get(SummaryViewModel::class.java)
    }

    override fun setupBindings() {
        super.setupBindings()

        viewModel.viewItems.observe(viewLifecycleOwner, { viewItems ->
            val adapter = recyclerView.adapter as ExerciseRecyclerViewAdapter
            adapter.setViewItems(viewItems)
        })

        viewModel.summaryMessage.observe(viewLifecycleOwner, { text ->
            tvSummaryExercise.text = text
        })
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = ExerciseRecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    @OnClick(R.id.btn_finish_training)
    fun onNavigateToHome() {
        viewModel.navigateToHome()
    }

    override fun onDestroyView() {
        viewModel.viewItems.removeObservers(viewLifecycleOwner)
        viewModel.summaryMessage.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}