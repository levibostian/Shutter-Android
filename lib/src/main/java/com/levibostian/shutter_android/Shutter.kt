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
import com.levibostian.shutter_android.exception.ShutterUserCancelledOperation

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

        fun getContext(): Context? = fragment?.activity ?: supportFragment?.activity ?: activity ?: appCompatActivity

    }

    interface ShutterResultListener {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
    }
    interface ShutterResultCallback {
        fun onComplete(result: ShutterResult)
        fun onError(humanReadableErrorMessage: String, error: Throwable)
    }

    class ShutterTakePhotoBuilder(val companion: ShutterCompanion): ShutterResultListener {

        private var fileName: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        private var directoryPath: File = companion.getContext()!!.filesDir
        private var addPicToGallery: Boolean = false
        private var resultCallback: ShutterResultCallback? = null

        private var fileAbsolutePath: String? = null

        private val TAKE_PHOTO_REQUEST_CODE = 0

        /**
         * @param[name] Name to give for the file. By default, name is the date in the format: yyyyMMdd_HHmmss. *Note: filename cannot contain any characters not alphabetical or underscores.*
         *
         * @throws IllegalArgumentException If filename contains characters that are not alphabetical and underscores.
         */
        fun filename(name: String): ShutterTakePhotoBuilder {
            if (isValidFilename(name)) this.fileName = name
            return this
        }

        /**
         * Check if the given name is a valid filename. Good for checking user determined filenames before calling [fileName].
         *
         * @see fileName
         */
        fun isValidFilename(name: String): Boolean {
            return !name.isEmpty() && name.matches(Regex("\\w+"))
        }

        /**
         * If you wish to have your app use internal storage (compared to external storage) that is private to your app, use this method. Files saved to this directory *will* be deleted when the user uninstalls your app.
         *
         * Check out the Android [documentation about internal storage][https://developer.android.com/reference/android/content/Context.html#getFilesDir()] to learn more about this option.
         *
         * @see usePrivateAppExternalStorage
         */
        fun usePrivateAppInternalStorage(): ShutterTakePhotoBuilder {
            directoryPath = File("${companion.getContext()!!.filesDir.absolutePath}/Pictures/")
            return this
        }

        /**
         * If you wish to have your app use external storage (compared to internal storage) that is private to your app, use this method. Files saved to this directory *will* be deleted when the user uninstalls your app.
         *
         * Check out the Android [documentation about internal storage][https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)] to learn more about this option.
         *
         * @see usePrivateAppInternalStorage
         */
        fun usePrivateAppExternalStorage(): ShutterTakePhotoBuilder {
            directoryPath = companion.getContext()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return this
        }

        /**
         * If you wish to add your captured photo to the Gallery on the user's device.
         */
        fun addPhotoToGallery(): ShutterTakePhotoBuilder {
            addPicToGallery = true
            return this
        }

        // for now, we are removing this. it requires read/write permissions and we do not want to have the user require that.
//        fun usePublicExternalStorage(): ShutterTakePhotoBuilder {
//            directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//            return this
//        }

        fun snap(callback: ShutterResultCallback): ShutterResultListener {
            this.resultCallback = callback

            if (!isValidFilename(fileName)) {
                callback.onError("You did not enter a valid file name for the photo. Name must contain only alphanumeric characters and underscores.", RuntimeException("User entered invalid filename: $fileName it can only contain alphanumeric characters and underscores."))
                return this
            }

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(companion.getContext()!!.packageManager) == null) {
                callback.onError("You do not have an app installed on your device to take a photo.", RuntimeException("User does not have app installed on device to take a photo."))
                return this
            }

            val nameOfApp = companion.getContext()!!.packageName.split(".").last()
            directoryPath = File("${directoryPath.absolutePath}/$nameOfApp")
            directoryPath.mkdirs()

            val imageFile: File = File(directoryPath, fileName + ".jpg")

            if (!imageFile.createNewFile()) {
                callback.onError("Error taking image.", RuntimeException("Error creating new image where image will save: ${directoryPath.absolutePath} with filename: $fileName"))
                return this
            }

            fileAbsolutePath = imageFile.absolutePath

            val takePhotoDestinationContentUri: Uri = FileProvider.getUriForFile(companion.getContext()!!, "${companion.getContext()!!.packageName}.fileprovider", imageFile)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoDestinationContentUri)

            companion.activity?.startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE)
            companion.appCompatActivity?.startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE)
            companion.fragment?.startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE)
            companion.supportFragment?.startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE)

            return this
        }

        /**
         * In the Fragment or Activity that you provided to Shutter via it's constructor, call [onActivityResult] on the return value of [snap].
         */
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            fun addPhotoToPublicGallery() {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val file = File(fileAbsolutePath)
                val contentUri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                companion.getContext()?.sendBroadcast(mediaScanIntent)
            }

            if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                if (resultCode != Activity.RESULT_OK) {
                    resultCallback?.onError("You cancelled taking a photo.", ShutterUserCancelledOperation("User cancelled taking a photo."))
                }

                if (addPicToGallery) addPhotoToPublicGallery()

                resultCallback?.onComplete(ShutterResult(fileAbsolutePath))

                return true
            } else {
                return false
            }
        }

    }

    class ShutterResult(val absoluteImagePath: String? = null) {

        fun imageUri(): Uri? = Uri.parse(absoluteImagePath)
        fun imageFile(): File? = File(absoluteImagePath)

    }

}