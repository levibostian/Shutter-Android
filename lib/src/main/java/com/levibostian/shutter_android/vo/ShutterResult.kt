package com.levibostian.shutter_android.vo

import android.net.Uri
import java.io.File

class ShutterResult(val absoluteFilePath: String? = null) {

    fun mediaUri(): Uri? = Uri.parse(absoluteFilePath)
    fun mediaFile(): File? = File(absoluteFilePath)

}