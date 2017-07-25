package com.levibostian.shutter_android

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.content.FileProvider
import com.levibostian.shutter_android.builder.ShutterRecordVideoBuilder
import com.levibostian.shutter_android.builder.ShutterTakePhotoBuilder
import com.levibostian.shutter_android.exception.ShutterUserCancelledOperation
import com.levibostian.shutter_android.vo.ShutterResult

open class Shutter private constructor() {

    companion object {

        @JvmStatic fun with(fragment: Fragment): ShutterCompanion = ShutterCompanion(fragment)

        @JvmStatic fun with(fragment: android.support.v4.app.Fragment): ShutterCompanion = ShutterCompanion(fragment)

        @JvmStatic fun with(activity: AppCompatActivity): ShutterCompanion = ShutterCompanion(activity)

        @JvmStatic fun with(activity: Activity): ShutterCompanion = ShutterCompanion(activity)

    }

    class ShutterCompanion {

        var fragment: Fragment? = null
        var supportFragment: android.support.v4.app.Fragment? = null
        var activity: Activity? = null
        var appCompatActivity: AppCompatActivity? = null

        constructor(fragment: Fragment) {
            this.fragment = fragment
        }

        constructor(fragment: android.support.v4.app.Fragment) {
            this.supportFragment = fragment
        }

        constructor(activity: AppCompatActivity) {
            this.appCompatActivity = activity
        }

        constructor(activity: Activity) {
            this.activity = activity
        }

        fun takePhoto(): ShutterTakePhotoBuilder {
            return ShutterTakePhotoBuilder(this)
        }

        fun recordVideo(): ShutterRecordVideoBuilder {
            return ShutterRecordVideoBuilder(this)
        }

        fun getContext(): Context? = fragment?.activity ?: supportFragment?.activity ?: activity ?: appCompatActivity

    }

}