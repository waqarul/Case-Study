package com.waqar.casestudy.features.posedetector.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.demo.kotlin.posedetector.PoseDetectorProcessor
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.waqar.casestudy.R
import com.waqar.casestudy.base.Domain
import com.waqar.casestudy.base.view.BaseFragment
import com.waqar.casestudy.features.posedetector.viewmodel.PoseDetectorViewModel
import com.waqar.casestudy.posedetector.widget.GraphicOverlay
import com.waqar.casestudy.posedetector.processor.VisionImageProcessor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class PoseDetectorFragment : BaseFragment<PoseDetectorViewModel>(),
    CompoundButton.OnCheckedChangeListener {
    private val TAG = this::class.simpleName

    @BindView(R.id.preview_view)
    lateinit var previewView: PreviewView

    @BindView(R.id.graphic_overlay)
    lateinit var graphicOverlay: GraphicOverlay

    @BindView(R.id.facing_switch)
    lateinit var facingSwitch: ToggleButton

    private var needUpdateGraphicOverlayImageSourceInfo = false

    private var cameraProvider: ProcessCameraProvider? = null
    private var poseDetectorProcessor: VisionImageProcessor? = null
    private var analysisUseCase: ImageAnalysis? = null

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? =
        CameraSelector.Builder().requireLensFacing(lensFacing).build()

    private var disposable = CompositeDisposable()
    private var toast: Toast? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        facingSwitch.setOnCheckedChangeListener(this)
    }

    override fun getViewLayout(): Int {
        return R.layout.fragment_pose_detector
    }

    override fun getOrCreateViewModel(): PoseDetectorViewModel {
        return ViewModelProvider(this).get(PoseDetectorViewModel::class.java)
    }

    override fun setupBindings() {
        super.setupBindings()

        viewModel.cameraProviderLiveData.observe(viewLifecycleOwner, { provider ->
            cameraProvider = provider

            requestAllRequiredPermissions()
        })

    }

    override fun loadData() {
        super.loadData()
        viewModel.loadData(arguments)
    }

    override fun allPermissionGranted() {
        super.allPermissionGranted()

        bindPoseDetector()
    }

    override fun onResume() {
        super.onResume()
        bindPoseDetector()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
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
        Snackbar.make(
            previewView,
            "This device does not have lens with facing: $newLensFacing",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun bindPoseDetector() {
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
                activityContext!!,
                poseDetectorOptions,
                shouldShowInFrameLikelihood,
                visualizeZ,
                rescaleZ,
                runClassification,
                true
            )

        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: Pose Detector", e)
            Snackbar.make(
                previewView,
                "Can not create image processor: " + e.localizedMessage,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        disposable.add(
            (poseDetectorProcessor as PoseDetectorProcessor).confidenceLevel.observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe { confidenceLevel ->
                toast?.cancel()
                if (confidenceLevel == 0f) {  // Alert the user as no pose, to get inside the frame
                    toast = Toast.makeText(
                        context,
                        getString(R.string.pose_detector_no_pose_found),
                        Toast.LENGTH_SHORT
                    )
                    toast?.show()
                } else if (confidenceLevel < 0.9f) {
                    // As person user not inside the frame and if confidence level is less than 60, alert user as per the docs
                    //https://developers.google.com/ml-kit/vision/pose-detection/android#4_get_information_about_the_detected_pose
                    toast = Toast.makeText(
                        context,
                        getString(R.string.pose_detector_not_full_frame),
                        Toast.LENGTH_SHORT
                    )
                    toast?.show()
                }


            })
        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(activityContext),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped =
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees =
                        imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    poseDetectorProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Snackbar.make(
                        previewView,
                        e.localizedMessage,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )
        cameraProvider!!.bindToLifecycle(
            this,
            cameraSelector!!,
            analysisUseCase
        )
    }


    override fun onPause() {
        super.onPause()

        toast?.cancel()

        poseDetectorProcessor?.run {
            stop()
        }
    }

    @OnClick(R.id.btn_close)
    fun onClosePressed() {
        viewModel.handleBackButton()
    }

    @OnClick(R.id.btn_start)
    fun onNavigateToPlayExercise() {
        viewModel.navigateToPlayExercise()
    }

    override fun onDestroyView() {
        viewModel.cameraProviderLiveData.removeObservers(viewLifecycleOwner)
        poseDetectorProcessor?.run {
            stop()
        }

        disposable.clear()

        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        poseDetectorProcessor?.run {
            stop()
        }
    }
}