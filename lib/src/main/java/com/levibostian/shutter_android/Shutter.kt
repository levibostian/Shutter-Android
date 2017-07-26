package com.levibostian.shutter_android

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.levibostian.shutter_android.builder.ShutterPickPhotoGalleryBuilder
import com.levibostian.shutter_android.builder.ShutterPickVideoGalleryBuilder
import com.levibostian.shutter_android.builder.ShutterRecordVideoBuilder
import com.levibostian.shutter_android.builder.ShutterTakePhotoBuilder

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
        var regularActivity: Activity? = null
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
            this.regularActivity = activity
        }

        fun takePhoto(): ShutterTakePhotoBuilder {
            return ShutterTakePhotoBuilder(this)
        }

        fun recordVideo(): ShutterRecordVideoBuilder {
            return ShutterRecordVideoBuilder(this)
        }

        fun getPhotoFromGallery(): ShutterPickPhotoGalleryBuilder {
            return ShutterPickPhotoGalleryBuilder(this)
        }

        fun getVideoFromGallery(): ShutterPickVideoGalleryBuilder {
            return ShutterPickVideoGalleryBuilder(this)
        }

        fun getContext(): Context? = getActivity()

        fun getActivity(): Activity? = fragment?.activity ?: supportFragment?.activity ?: regularActivity ?: appCompatActivity

    }

}