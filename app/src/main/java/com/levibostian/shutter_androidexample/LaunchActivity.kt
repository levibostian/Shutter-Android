package com.levibostian.shutter_androidexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity() {

    companion object {
        @JvmStatic fun getIntent(context: Context): Intent {
            return Intent(context, LaunchActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_launch)

        setupViews()
    }

    private fun setupViews() {
        launch_activity_capture_button.setOnClickListener {
            startActivity(CaptureMediaActivity.getIntent(this))
        }
        launch_activity_gallery_button.setOnClickListener {
            startActivity(GetMediaFromGalleryActivity.getIntent(this))
        }
    }

}