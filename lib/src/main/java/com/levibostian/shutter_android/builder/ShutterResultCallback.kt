package com.levibostian.shutter_android.builder

import com.levibostian.shutter_android.vo.ShutterResult

interface ShutterResultCallback {
    fun onComplete(result: ShutterResult)
    fun onError(humanReadableErrorMessage: String, error: Throwable)
}