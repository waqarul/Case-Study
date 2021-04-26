package com.waqar.casestudy.features.home.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewitem.IViewItem
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.core.model.ExerciseModel
import com.waqar.casestudy.extentions.getExerciseStatusIcon
import com.waqar.casestudy.features.home.viewitems.ExerciseViewItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SummaryViewModel : BaseViewModel() {

    // Live data
    var viewItems = MutableLiveData<List<IViewItem>>()
    var summaryMessage = MutableLiveData<String>()

    override fun loadData(params: Bundle?) {
        populateExerciseSummaryMessage()
        fetchExerciseList()
    }

    private fun populateExerciseSummaryMessage() {
        disposables.add(interactor.getExercisesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { exerciseCount ->
                summaryMessage.value =  String.format(
                    context.getString(R.string.summary_message),
                    exerciseCount.skipCount,
                    exerciseCount.totalCount
                )
            })
    }

    private fun fetchExerciseList() {
        disposables.add(interactor.getSavedExercisesItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { exerciseItems ->
                createViewItems(exerciseItems)
            })
    }

    private fun createViewItems(exerciseList: List<ExerciseModel>) {
        val itemsList = exerciseList.map { exerciseModel ->
            ExerciseViewItem(
                exerciseModel.name,
                exerciseModel.coverImageUrl,
                exerciseModel.getExerciseStatusIcon()
            )
        }
        viewItems.value = itemsList
    }

    fun navigateToHome() {
        navigator.back()
    }
}