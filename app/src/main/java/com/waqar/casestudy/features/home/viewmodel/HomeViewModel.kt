package com.waqar.casestudy.features.home.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewitem.IViewItem
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.base.viewmodel.Event
import com.waqar.casestudy.constants.NavigationConstants
import com.waqar.casestudy.core.model.AlertModel
import com.waqar.casestudy.core.model.ExerciseModel
import com.waqar.casestudy.extentions.getFavoriteIcon
import com.waqar.casestudy.features.home.viewitems.ExerciseViewItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModel : BaseViewModel() {
    // Private fields

    // Live data
    var viewItems = MutableLiveData<List<IViewItem>>()
    val isRecordFound: MutableLiveData<Boolean> = MutableLiveData()
    val showRefreshIndicator: MutableLiveData<Boolean> = MutableLiveData()

    override fun loadData(params: Bundle?) {
        fetchExerciseList()
    }

    fun navigateToPoseDetector() {
        navigator.navigate(NavigationConstants.POSE_DETECTOR)
    }

    private fun fetchExerciseList() {

        if (!isInternetConnectionAvailable) {
            showNoInternetAlert.value = Event(true)
            checkIfOfflineDataIsAvailable()
            return
        }
        showLoading.value = true
        val disposable = interactor.getExerciseItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ exerciseItems ->
                shouldShowLoadingAndRefreshIndicator(false)

                if (exerciseItems.isNotEmpty()) {
                    isRecordFound.value = true
                    createViewItems(exerciseItems)
                } else {
                    isRecordFound.value = false
                }

            }, {

                shouldShowLoadingAndRefreshIndicator(false)
                this.showAlertDialog.value = Event(
                    AlertModel(
                        title = context.getString(R.string.title_error_in_request),
                        message = context.getString(R.string.message_error_in_request),
                        positiveButtonTitle = context.getString(R.string.alert_ok_label)
                    )
                )

            })

        disposables.add(disposable)
    }

    private fun checkIfOfflineDataIsAvailable() {
        shouldShowLoadingAndRefreshIndicator(false)

        getExercisesLocally()
    }

    private fun getExercisesLocally() {
        val disposable = interactor.getSavedExercisesItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { exerciseItems ->
                if (exerciseItems.isNotEmpty()) {
                    isRecordFound.value = true
                    createViewItems(exerciseItems)
                } else {
                    isRecordFound.value = false
                }
            }

        disposables.add(disposable)
    }

    private fun shouldShowLoadingAndRefreshIndicator(isShow: Boolean) {
        showLoading.value = isShow
        showRefreshIndicator.value = isShow
    }

    private fun createViewItems(exerciseItems: List<ExerciseModel>) {
        exerciseList.clear()
        exerciseList.addAll(exerciseItems)

        val itemsList = exerciseList.map { exerciseModel ->
            ExerciseViewItem(
                exerciseModel.name,
                exerciseModel.coverImageUrl,
                exerciseModel.getFavoriteIcon()
            )
        }
        viewItems.value = itemsList
    }

    fun onItemFavoriteClicked(position: Int) {
        val exercise = exerciseList.getOrNull(position) ?: return
        exercise.isFavorite = !exercise.isFavorite
        interactor.updateExercise(exercise)
        getExercisesLocally()
    }
}