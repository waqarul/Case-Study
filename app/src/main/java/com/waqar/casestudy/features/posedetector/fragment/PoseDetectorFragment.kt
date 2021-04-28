package com.waqar.casestudy.features.posedetector.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.snackbar.Snackbar
import com.waqar.casestudy.R
import com.waqar.casestudy.base.view.BaseFragment
import com.waqar.casestudy.features.posedetector.viewmodel.PoseDetectorViewModel
import com.waqar.casestudy.posedetector.widget.GraphicOverlay
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

        viewModel.cameraProviderDataLiveData.observe(viewLifecycleOwner, {
            it.cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    it.cameraSelector,
                    it.analysisUseCase
            )
        })

        viewModel.graphOverlayImageSourceLiveData.observe(viewLifecycleOwner, {
            graphicOverlay.setImageSourceInfo(it.width, it.height, it.isImageFlipped)
        })

        viewModel.poseDetectorProcessorSourceLiveData.observe(viewLifecycleOwner, {
            it.poseDetectorProcessor.processImageProxy(it.imageProxy, graphicOverlay)
        })


        viewModel.errorMessage.observe(viewLifecycleOwner, { message ->
            Snackbar.make(
                    previewView,
                    message,
                    Snackbar.LENGTH_LONG
            ).show()
        })

        viewModel.confidenceLevel.observe(viewLifecycleOwner, { confidenceLevel ->
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

    }

    override fun loadData() {
        super.loadData()
        viewModel.loadData(arguments)
    }

    override fun allPermissionGranted() {
        super.allPermissionGranted()

        viewModel.bindPoseDetector()
    }

    override fun onResume() {
        super.onResume()
        requestAllRequiredPermissions()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        viewModel.changeCameraLensFacing()
    }

    override fun onPause() {
        super.onPause()

        toast?.cancel()

        viewModel.stopPoseDetector()
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

        viewModel.stopPoseDetector()
        disposable.clear()

        super.onDestroyView()
    }
}