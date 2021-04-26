package com.waqar.casestudy.features.posedetector.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.constants.NavigationConstants
import java.util.concurrent.ExecutionException

class PoseDetectorViewModel : BaseViewModel() {
    private val TAG = this::class.simpleName

    // Live data
    var cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()

    override fun loadData(params: Bundle?) {
        getProcessCameraProvider()
    }

    private fun getProcessCameraProvider() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProviderLiveData.setValue(cameraProviderFuture.get())
                } catch (e: ExecutionException) {
                    // Handle any errors (including cancellation) here.
                    Log.e(TAG, "Unhandled exception", e)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Unhandled exception", e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun navigateToPlayExercise() {
        navigator.back()
        navigator.navigate(NavigationConstants.EXERCISE)
    }
}