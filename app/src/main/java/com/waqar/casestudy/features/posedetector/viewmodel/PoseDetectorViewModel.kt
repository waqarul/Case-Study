package com.waqar.casestudy.features.posedetector.viewmodel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.demo.kotlin.posedetector.PoseDetectorProcessor
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.waqar.casestudy.base.viewmodel.BaseViewModel
import com.waqar.casestudy.constants.NavigationConstants
import com.waqar.casestudy.features.posedetector.models.CameraProviderData
import com.waqar.casestudy.features.posedetector.models.GraphOverlayImageSourceData
import com.waqar.casestudy.features.posedetector.models.PoseDetectorProcessorData
import com.waqar.casestudy.posedetector.processor.VisionImageProcessor
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.ExecutionException

class PoseDetectorViewModel : BaseViewModel() {
    private val TAG = this::class.simpleName

    // Live data
    var graphOverlayImageSourceLiveData = MutableLiveData<GraphOverlayImageSourceData>()
    var poseDetectorProcessorSourceLiveData = MutableLiveData<PoseDetectorProcessorData>()
    var cameraProviderDataLiveData = MutableLiveData<CameraProviderData>()

    var errorMessage = MutableLiveData<String>()
    var confidenceLevel = MutableLiveData<Float>()

    private var cameraProvider: ProcessCameraProvider? = null
    private var poseDetectorProcessor: VisionImageProcessor? = null
    private var analysisUseCase: ImageAnalysis? = null

    private var needUpdateGraphicOverlayImageSourceInfo = false

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

    override fun loadData(params: Bundle?) {
        getProcessCameraProvider()
    }

    private fun getProcessCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
                {
                    try {
                        cameraProvider = cameraProviderFuture.get()
                        bindPoseDetector()
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

    fun bindPoseDetector() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindAnalysisUseCase()
        }
    }

    @SuppressLint("CheckResult")
    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (poseDetectorProcessor != null) {
            poseDetectorProcessor!!.stop()
        }
        poseDetectorProcessor = try {

            val poseDetectorOptions = PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build();

            val shouldShowInFrameLikelihood = true
            val visualizeZ = true
            val rescaleZ = true
            val runClassification = true
            PoseDetectorProcessor(
                    context,
                    poseDetectorOptions,
                    shouldShowInFrameLikelihood,
                    visualizeZ,
                    rescaleZ,
                    runClassification,
                    true
            )

        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: Pose Detector", e)
            errorMessage.value = "Can not create image processor: " + e.localizedMessage
            return
        }

        disposables.add(
                (poseDetectorProcessor as PoseDetectorProcessor).confidenceLevel.observeOn(
                        AndroidSchedulers.mainThread()
                ).subscribe { confidenceLevel ->
                    this.confidenceLevel.value = confidenceLevel
                })
        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(context),
                ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        val isImageFlipped =
                                lensFacing == CameraSelector.LENS_FACING_FRONT
                        val rotationDegrees =
                                imageProxy.imageInfo.rotationDegrees
                        if (rotationDegrees == 0 || rotationDegrees == 180) {

                            graphOverlayImageSourceLiveData.value = GraphOverlayImageSourceData(imageProxy.width, imageProxy.height, isImageFlipped)
                        } else {

                            graphOverlayImageSourceLiveData.value = GraphOverlayImageSourceData(imageProxy.height, imageProxy.width, isImageFlipped)

                        }
                        needUpdateGraphicOverlayImageSourceInfo = false
                    }
                    try {
                        poseDetectorProcessorSourceLiveData.value = PoseDetectorProcessorData(imageProxy, poseDetectorProcessor!!)

                    } catch (e: MlKitException) {
                        Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                        errorMessage.value = e.localizedMessage
                    }
                }
        )
        cameraProvider?.let {
            cameraProviderDataLiveData.value = CameraProviderData(it, cameraSelector!!, analysisUseCase!!)
        }
    }


    fun changeCameraLensFacing() {
        if (cameraProvider == null) {
            return
        }
        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        val newCameraSelector =
                CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                Log.d(TAG, "Set facing to $newLensFacing")
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindPoseDetector()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            // Falls through
        }

        errorMessage.value = "This device does not have lens with facing: $newLensFacing"
    }

    fun stopPoseDetector() {
        poseDetectorProcessor?.run {
            stop()
        }
    }

    fun navigateToPlayExercise() {
        navigator.back()
        navigator.navigate(NavigationConstants.EXERCISE)
    }
}