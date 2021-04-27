package com.waqar.casestudy.features.exercise.viewmodel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.base.viewmodel.Event
import com.waqar.casestudy.common.viewitems.ExerciseViewItem
import com.waqar.casestudy.constants.AppConstants
import com.waqar.casestudy.constants.NavigationConstants
import com.waqar.casestudy.core.model.enum.ExerciseStatusType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ExerciseViewModel : BaseViewModel() {

    private var isHandlerRunning = false;
    private lateinit var titleVisibilityHandler: Handler
    private var runnable: Runnable? = null

    // Live data
    val playingItem: MutableLiveData<ExerciseViewItem> = MutableLiveData()
    val shouldShowTitle: MutableLiveData<Boolean> = MutableLiveData()

    var currentVideoIndex = 0
    override fun loadData(params: Bundle?) {
        getExercisesLocally()
    }

    private fun getExercisesLocally() {
        val disposable = interactor.getSavedExercisesItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { exerciseItems ->
                exerciseList.addAll(exerciseItems)
                playNextVideo()
            }

        disposables.add(disposable)
    }

    private fun playNextVideo() {
        clearNavigateHandler()
        if (!isInternetConnectionAvailable) {
            showNoInternetAlert.value = Event(true)
            showLoading.value = false
            return
        }

        exerciseList.let {
            // play the next video available,
            // if not, navigate to Summary Screen
            val exercise = it.getOrNull(currentVideoIndex)
            exercise?.let {
                playingItem.value = ExerciseViewItem(it.name, it.videoUrl)
            } ?: kotlin.run {
                navigateToSummaryView()
            }
        }
    }

    fun videoCompleted() {
        updateExerciseWithStatus(ExerciseStatusType.IS_COMPLETE)
        currentVideoIndex++
        playNextVideo()
    }

    fun skipVideo() {
        updateExerciseWithStatus(ExerciseStatusType.IS_SKIP)
        currentVideoIndex++
        playNextVideo()
    }

    private fun updateExerciseWithStatus(exerciseStatus: ExerciseStatusType) {
        val currentExercise = exerciseList.getOrNull(currentVideoIndex) ?: return
        currentExercise.status = exerciseStatus.status
        interactor.updateExercise(currentExercise)
    }

    private fun navigateToSummaryView() {
        navigator.back()
        navigator.navigate(NavigationConstants.SUMMARY)
    }

    fun startTimerToHideTitle() {
        if (!::titleVisibilityHandler.isInitialized) {
            titleVisibilityHandler = Handler(Looper.getMainLooper())
        }

        if (!isHandlerRunning) {
            runnable = Runnable { shouldShowTitle.value = false }
            isHandlerRunning = true
            titleVisibilityHandler.postDelayed(runnable!!, AppConstants.TIME_TO_HIDE_TOOLBAR_TITLE)
        }
    }

    private fun clearNavigateHandler() {
        if (isHandlerRunning && ::titleVisibilityHandler.isInitialized) {
            titleVisibilityHandler.removeCallbacks(runnable!!)
            isHandlerRunning = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearNavigateHandler()
    }
}