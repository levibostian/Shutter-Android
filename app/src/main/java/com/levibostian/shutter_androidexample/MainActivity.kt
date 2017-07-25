package com.levibostian.shutter_androidexample

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.levibostian.shutter_android.Shutter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val INTERNAL_PRIVATE_RADIO_INDEX = 1
    private val EXTERNAL_PRIVATE_RADIO_INDEX = 2

    private var shutterResultListener: Shutter.ShutterResultListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()
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
            if (!userGivenFilename.isEmpty())

            if (shutterBuilder.isValidFilename(userGivenFilename)) {
                shutterBuilder = shutterBuilder.filename(userGivenFilename)
            } else {
                main_activity_filename_edittext.error = "You did not enter a valid filename."
                return@setOnClickListener
            }

            shutterResultListener = shutterBuilder.snap(object : Shutter.ShutterResultCallback {
                override fun onComplete(result: Shutter.ShutterResult) {
                    Log.d("SHUTTER_EXAMPLE_APP", "Image taken to path: ${result.absoluteImagePath}")
                    Glide.with(this@MainActivity)
                            .load(result.absoluteImagePath)
                            .into(main_activity_photo_taken_imageview)
                    main_activity_photo_taken_location_textview.text = "Photo saved to path: ${result.absoluteImagePath}"
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!shutterResultListener!!.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}