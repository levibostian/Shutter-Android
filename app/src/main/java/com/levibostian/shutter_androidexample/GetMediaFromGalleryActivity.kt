package com.levibostian.shutter_androidexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.SimpleExoPlayer
import com.levibostian.shutter_android.Shutter
import com.levibostian.shutter_android.builder.ShutterResultCallback
import com.levibostian.shutter_android.builder.ShutterResultListener
import com.levibostian.shutter_android.vo.ShutterResult
import kotlinx.android.synthetic.main.activity_get_media_from_gallery.*

class GetMediaFromGalleryActivity : AppCompatActivity() {

    companion object {
        @JvmStatic fun getIntent(context: Context): Intent {
            return Intent(context, GetMediaFromGalleryActivity::class.java)
        }
    }

    private val INTERNAL_PRIVATE_RADIO_INDEX = 1
    private val EXTERNAL_PRIVATE_RADIO_INDEX = 2

    private var shutterResultListener: ShutterResultListener? = null

    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_get_media_from_gallery)

        setupViews()
    }

    override fun onStop() {
        super.onStop()

        exoPlayer?.release()
    }

    private fun setupViews() {
        get_media_activity_get_photo_button.setOnClickListener {
            var shutterBuilder = Shutter.with(this)
                    .getPhotoFromGallery()

            when (get_media_activity_photo_save_location_radiogroup.checkedRadioButtonId) {
                INTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppInternalStorage()
                }
                EXTERNAL_PRIVATE_RADIO_INDEX -> {
                    shutterBuilder = shutterBuilder.usePrivateAppExternalStorage()
                }
            }

            val userGivenFilename = get_media_activity_filename_edittext.text.toString()
            if (!userGivenFilename.isEmpty()) {
                if (shutterBuilder.isValidFilename(userGivenFilename)) {
                    shutterBuilder = shutterBuilder.filename(userGivenFilename)
                } else {
                    get_media_activity_filename_edittext.error = "You did not enter a valid filename."
                    return@setOnClickListener
                }
            }

            shutterResultListener = shutterBuilder.snap(object : ShutterResultCallback {
                override fun onComplete(result: ShutterResult) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Image copied to path: ${result.absoluteFilePath}")
                    Glide.with(this@GetMediaFromGalleryActivity)
                            .load(result.absoluteFilePath)
                            .into(get_media_activity_photo_taken_imageview)

                    showImageView()
                    get_media_activity_photo_taken_location_textview.text = "Photo saved to path: ${result.absoluteFilePath}"
                    get_media_activity_photo_shown_here_textview.visibility = View.GONE
                }
                override fun onError(humanReadableErrorMessage: String, error: Throwable) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Error encountered: ${error.message}")
                    Snackbar.make(findViewById(android.R.id.content), humanReadableErrorMessage, Snackbar.LENGTH_LONG).show()
                    get_media_activity_photo_taken_location_textview.text = ""
                }
            })
        }

        get_media_activity_get_video_button.setOnClickListener {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!shutterResultListener!!.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showVideoPlayer() {
        get_media_activity_photo_taken_videoview.visibility = View.VISIBLE
        get_media_activity_photo_taken_imageview.visibility = View.GONE
    }

    private fun showImageView() {
        get_media_activity_photo_taken_videoview.visibility = View.GONE
        get_media_activity_photo_taken_imageview.visibility = View.VISIBLE
    }

}