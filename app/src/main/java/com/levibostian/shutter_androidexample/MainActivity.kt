package com.levibostian.shutter_androidexample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayerFactory
import com.levibostian.shutter_android.Shutter
import com.levibostian.shutter_android.builder.ShutterResultCallback
import com.levibostian.shutter_android.builder.ShutterResultListener
import com.levibostian.shutter_android.vo.ShutterResult
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    private val INTERNAL_PRIVATE_RADIO_INDEX = 1
    private val EXTERNAL_PRIVATE_RADIO_INDEX = 2

    private var shutterResultListener: ShutterResultListener? = null

    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()
    }

    override fun onStop() {
        super.onStop()

        exoPlayer?.release()
    }

    private fun setupViews() {
        main_activity_take_photo.setOnClickListener {
            var shutterBuilder = Shutter.with(this)
                    .takePhoto()

            when (main_activity_photo_save_location_radiogroup.checkedRadioButtonId) {
                INTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppInternalStorage()
                }
                EXTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppExternalStorage()
                }
            }

            if (main_activity_add_photo_gallery_checkbox.isChecked) shutterBuilder = shutterBuilder.addPhotoToGallery()

            val userGivenFilename = main_activity_filename_edittext.text.toString()
            if (!userGivenFilename.isEmpty()) {
                if (shutterBuilder.isValidFilename(userGivenFilename)) {
                    shutterBuilder = shutterBuilder.filename(userGivenFilename)
                } else {
                    main_activity_filename_edittext.error = "You did not enter a valid filename."
                    return@setOnClickListener
                }
            }

            shutterResultListener = shutterBuilder.snap(object : ShutterResultCallback {
                override fun onComplete(result: ShutterResult) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Image taken to path: ${result.absoluteFilePath}")
                    Glide.with(this@MainActivity)
                            .load(result.absoluteFilePath)
                            .into(main_activity_photo_taken_imageview)

                    showImageView()
                    main_activity_photo_taken_location_textview.text = "Photo saved to path: ${result.absoluteFilePath}"
                    main_activity_photo_shown_here_textview.visibility = View.GONE
                }
                override fun onError(humanReadableErrorMessage: String, error: Throwable) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Error encountered: ${error.message}")
                    Snackbar.make(findViewById(android.R.id.content), humanReadableErrorMessage, Snackbar.LENGTH_LONG).show()
                    main_activity_photo_taken_location_textview.text = ""
                }
            })
        }

        main_activity_record_video.setOnClickListener {
            var shutterBuilder = Shutter.with(this)
                    .recordVideo()

            when (main_activity_photo_save_location_radiogroup.checkedRadioButtonId) {
                INTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppInternalStorage()
                }
                EXTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppExternalStorage()
                }
            }

            if (main_activity_add_photo_gallery_checkbox.isChecked) shutterBuilder = shutterBuilder.addVideoToGallery()

            val userGivenFilename = main_activity_filename_edittext.text.toString()
            if (!userGivenFilename.isEmpty()) {
                if (shutterBuilder.isValidFilename(userGivenFilename)) {
                    shutterBuilder = shutterBuilder.filename(userGivenFilename)
                } else {
                    main_activity_filename_edittext.error = "You did not enter a valid filename."
                    return@setOnClickListener
                }
            }

            shutterResultListener = shutterBuilder.snap(object : ShutterResultCallback {
                override fun onComplete(result: ShutterResult) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Video recorded to path: ${result.absoluteFilePath}")

                    exoPlayer = getMp4StreamingExoPlayer(this@MainActivity, result.mediaUri()!!)
                    main_activity_photo_taken_videoview.player = exoPlayer

                    showVideoPlayer()
                    main_activity_photo_taken_location_textview.text = "Video saved to path: ${result.absoluteFilePath}"
                    main_activity_photo_shown_here_textview.visibility = View.GONE
                }
                override fun onError(humanReadableErrorMessage: String, error: Throwable) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Error encountered: ${error.message}")
                    Snackbar.make(findViewById(android.R.id.content), humanReadableErrorMessage, Snackbar.LENGTH_LONG).show()
                    main_activity_photo_taken_location_textview.text = ""
                }
            })
        }
    }

    private fun showVideoPlayer() {
        main_activity_photo_taken_videoview.visibility = View.VISIBLE
        main_activity_photo_taken_imageview.visibility = View.GONE
    }

    private fun showImageView() {
        main_activity_photo_taken_videoview.visibility = View.GONE
        main_activity_photo_taken_imageview.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!shutterResultListener!!.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getMp4StreamingExoPlayer(context: Context, videoUri: Uri): SimpleExoPlayer {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shutter-Android Example"), bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()

        // using the ExtractorMediaSource because it is best for mp4 videos.
        val videoSource = ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null)

        val videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        videoPlayer.prepare(videoSource)
        videoPlayer.playWhenReady = true

        return videoPlayer
    }

}