package com.waqar.casestudy.features.exercise.fragments

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.snackbar.Snackbar
import com.waqar.casestudy.R
import com.waqar.casestudy.base.view.AbstractToolbarFragment
import com.waqar.casestudy.common.viewitems.ExerciseViewItem
import com.waqar.casestudy.constants.AppConstants
import com.waqar.casestudy.features.exercise.viewmodel.ExerciseViewModel


class ExerciseFragment : AbstractToolbarFragment<ExerciseViewModel>() {

    @BindView(R.id.video_view)
    lateinit var videoView: VideoView

    @BindView(R.id.btn_skip_video)
    lateinit var btnSkip: Button

    // Current playback position (in milliseconds).
    private var currentSeekPosition = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            currentSeekPosition =
                savedInstanceState.getInt(AppConstants.PLAYBACK_TIME)
        }

        showCloseButton(true)

        // Set up the media controller widget and attach it to the video view.
        val controller = MediaController(activityContext)
        controller.setMediaPlayer(videoView)
        videoView.setMediaController(controller)
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.exercise_label)
    }

    override fun getViewLayout(): Int {
        return R.layout.fragment_exercise
    }

    override fun getOrCreateViewModel(): ExerciseViewModel {
        return ViewModelProvider(this).get(ExerciseViewModel::class.java)
    }

    override fun setupBindings() {
        super.setupBindings()

        viewModel.playingItem.observe(viewLifecycleOwner, { item ->
            initializePlayer(item)
            setToolbarTitle(item.title)
        })

        viewModel.shouldShowTitle.observe(viewLifecycleOwner, { shouldShowTitle ->
            showToolbarTitle(shouldShowTitle)
        })
    }

    override fun loadData() {
        super.loadData()
        viewModel.loadData(arguments)
    }

    override fun onResume() {
        super.onResume()

        // make the screen orientation landscape
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() {
        super.onPause()

        // In Android versions less than N (7.0, API 24), onPause() is the
        // end of the visual lifecycle of the app.  Pausing the video here
        // prevents the sound from continuing to play even after the app
        // disappears.
        //
        // This is not a problem for more recent versions of Android because
        // onStop() is now the end of the visual lifecycle, and that is where
        // most of the app teardown should take place.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoView.pause()
        }
    }

    override fun onStop() {
        super.onStop()

        // Media playback takes a lot of resources, so everything should be
        // stopped and released at this time.
        releasePlayer()

        // make the screen orientation portrait
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        outState.putInt(
            AppConstants.PLAYBACK_TIME,
            videoView.currentPosition
        )
    }

    private fun initializePlayer(item: ExerciseViewItem) {
        // if video player is already running.
        releasePlayer()

        // show loading as video takes a bit of time to load.
        showLoading(true)
        btnSkip.visibility = View.GONE

        // Buffer and decode the video sample.
        val videoUri: Uri = Uri.parse(item.url)
        videoView.setVideoURI(videoUri)

        // Listener for onPrepared() event (runs after the media is prepared).
        videoView.setOnPreparedListener { // Hide buffering message.

            btnSkip.visibility = View.VISIBLE
            showLoading(false)

            // As soon the video is loaded, the title will be hidden after 5s
            viewModel.startTimerToHideTitle()

            // Restore saved position, if available.
            if (currentSeekPosition > 0) {
                videoView.seekTo(currentSeekPosition)
            } else {
                // Skipping to 1 shows the first frame of the video.
                videoView.seekTo(1)
            }

            // Start playing!
            videoView.start()
        }

        // Listener for onCompletion() event (runs after media has finished
        // playing).
        videoView.setOnCompletionListener {
            val message = String.format(getString(R.string.exercise_complete_msg), item.title)
            Snackbar.make(
                videoView,
                message,
                Snackbar.LENGTH_SHORT
            ).show()

            viewModel.videoCompleted()
            // Return the video position to the start.
            videoView.seekTo(0)
        }
    }


    // Release all media-related resources. In a more complicated app this
    // might involve unregistering listeners or releasing audio focus.
    private fun releasePlayer() {
        videoView.stopPlayback()
    }

    override fun onDestroyView() {
        viewModel.playingItem.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    @OnClick(R.id.btn_skip_video)
    fun onSkipVideo() {
        viewModel.skipVideo()
    }
}