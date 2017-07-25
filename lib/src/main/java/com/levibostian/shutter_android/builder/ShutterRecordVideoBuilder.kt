package com.levibostian.shutter_android.builder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import com.levibostian.shutter_android.Shutter
import com.levibostian.shutter_android.exception.ShutterUserCancelledOperation
import com.levibostian.shutter_android.vo.ShutterResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ShutterRecordVideoBuilder(val companion: Shutter.ShutterCompanion): ShutterResultListener {

    private var fileName: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    private var directoryPath: File = companion.getContext()!!.filesDir
    private var addVidToGallery: Boolean = false
    private var resultCallback: ShutterResultCallback? = null

    private var fileAbsolutePath: String? = null

    private val RECORD_VIDEO_REQUEST_CODE = 0

    /**
     * @param[name] Name to give for the file. By default, name is the date in the format: yyyyMMdd_HHmmss. *Note: filename cannot contain any characters not alphabetical or underscores.*
     *
     * @throws IllegalArgumentException If filename contains characters that are not alphabetical and underscores.
     */
    fun filename(name: String): ShutterRecordVideoBuilder {
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
    fun usePrivateAppInternalStorage(): ShutterRecordVideoBuilder {
        directoryPath = File("${companion.getContext()!!.filesDir.absolutePath}/Movies/")
        return this
    }

    /**
     * If you wish to have your app use external storage (compared to internal storage) that is private to your app, use this method. Files saved to this directory *will* be deleted when the user uninstalls your app.
     *
     * Check out the Android [documentation about internal storage][https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)] to learn more about this option.
     *
     * @see usePrivateAppInternalStorage
     */
    fun usePrivateAppExternalStorage(): ShutterRecordVideoBuilder {
        directoryPath = companion.getContext()!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return this
    }

    /**
     * If you wish to add your recorded video to the Gallery on the user's device.
     */
    fun addVideoToGallery(): ShutterRecordVideoBuilder {
        addVidToGallery = true
        return this
    }

    // for now, we are removing this. it requires read/write permissions and we do not want to have the user require that.
//        fun usePublicExternalStorage(): ShutterRecordVideoBuilder {
//            directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//            return this
//        }

    fun snap(callback: ShutterResultCallback): ShutterResultListener {
        this.resultCallback = callback

        if (!isValidFilename(fileName)) {
            callback.onError("You did not enter a valid file name for the video. Name must contain only alphanumeric characters and underscores.", RuntimeException("User entered invalid filename: $fileName it can only contain alphanumeric characters and underscores."))
            return this
        }

        val recordVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (recordVideoIntent.resolveActivity(companion.getContext()!!.packageManager) == null) {
            callback.onError("You do not have an app installed on your device to record a video.", RuntimeException("User does not have app installed on device to record a video."))
            return this
        }

        val nameOfApp = companion.getContext()!!.packageName.split(".").last()
        directoryPath = File("${directoryPath.absolutePath}/$nameOfApp")
        directoryPath.mkdirs()

        val videoFile: File = File(directoryPath, fileName + ".mp4")

        if (!videoFile.createNewFile()) {
            callback.onError("Error recording video.", RuntimeException("Error creating new video file where video will save: ${directoryPath.absolutePath} with filename: $fileName"))
            return this
        }

        fileAbsolutePath = videoFile.absolutePath

        val recordVideoDestinationContentUri: Uri = FileProvider.getUriForFile(companion.getContext()!!, "${companion.getContext()!!.packageName}.fileprovider", videoFile)

        recordVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, recordVideoDestinationContentUri)

        companion.activity?.startActivityForResult(recordVideoIntent, RECORD_VIDEO_REQUEST_CODE)
        companion.appCompatActivity?.startActivityForResult(recordVideoIntent, RECORD_VIDEO_REQUEST_CODE)
        companion.fragment?.startActivityForResult(recordVideoIntent, RECORD_VIDEO_REQUEST_CODE)
        companion.supportFragment?.startActivityForResult(recordVideoIntent, RECORD_VIDEO_REQUEST_CODE)

        return this
    }

    /**
     * In the Fragment or Activity that you provided to Shutter via it's constructor, call [onActivityResult] on the return value of [snap].
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        fun addVideoToPublicGallery() {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val file = File(fileAbsolutePath)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            companion.getContext()?.sendBroadcast(mediaScanIntent)
        }

        if (requestCode == RECORD_VIDEO_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                resultCallback?.onError("You cancelled recording a video.", ShutterUserCancelledOperation("User cancelled recording a video."))
            }

            if (addVidToGallery) addVideoToPublicGallery()

            resultCallback?.onComplete(ShutterResult(fileAbsolutePath))

            return true
        } else {
            return false
        }
    }

}