package com.levibostian.shutter_android.builder

import android.content.Intent

interface ShutterResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}