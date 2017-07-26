package com.levibostian.shutter_android.builder

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import com.levibostian.shutter_android.Shutter
import com.levibostian.shutter_android.exception.ShutterUserCancelledOperation
import com.levibostian.shutter_android.vo.ShutterResult
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ShutterPickVideoGalleryBuilder(val companion: Shutter.ShutterCompanion): ShutterResultListener {

    private var fileName: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    private var directoryPath: File
    private var resultCallback: ShutterResultCallback? = null

    private var fileAbsolutePath: String? = null

    private val GET_VIDEO_REQUEST_CODE = 0

    init {
        directoryPath = getDirectoryPathInternalPrivateStorage()
    }

    /**
     * @param[name] Name to give for the file. By default, name is the date in the format: yyyyMMdd_HHmmss. *Note: filename cannot contain any characters not alphabetical or underscores.*
     *
     * @throws IllegalArgumentException If filename contains characters that are not alphabetical and underscores.
     */
    fun filename(name: String): ShutterPickVideoGalleryBuilder {
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
    fun usePrivateAppInternalStorage(): ShutterPickVideoGalleryBuilder {
        directoryPath = getDirectoryPathInternalPrivateStorage()
        return this
    }

    private fun getDirectoryPathInternalPrivateStorage(): File {
        return File("${companion.getContext()!!.filesDir.absolutePath}/Movies/")
    }

    /**
     * If you wish to have your app use external storage (compared to internal storage) that is private to your app, use this method. Files saved to this directory *will* be deleted when the user uninstalls your app.
     *
     * Check out the Android [documentation about internal storage][https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)] to learn more about this option.
     *
     * @see usePrivateAppInternalStorage
     */
    fun usePrivateAppExternalStorage(): ShutterPickVideoGalleryBuilder {
        directoryPath = companion.getContext()!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return this
    }

    // for now, we are removing this. it requires read/write permissions and we do not want to have the user require that.
//        fun usePublicExternalStorage(): ShutterPickVideoGalleryBuilder {
//            directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//            return this
//        }

    fun snap(callback: ShutterResultCallback): ShutterResultListener {
        this.resultCallback = callback

        if (!isValidFilename(fileName)) {
            callback.onError("You did not enter a valid file name for the video. Name must contain only alphanumeric characters and underscores.", RuntimeException("User entered invalid filename: $fileName it can only contain alphanumeric characters and underscores."))
            return this
        }

        val getVideoGalleryIntent = Intent(Intent.ACTION_PICK)
        getVideoGalleryIntent.type = "video/*"
        if (getVideoGalleryIntent.resolveActivity(companion.getContext()!!.packageManager) == null) {
            callback.onError("You do not have an app installed on your device view videos.", RuntimeException("You do not have an app installed on your device view videos."))
            return this
        }

        companion.regularActivity?.startActivityForResult(getVideoGalleryIntent, GET_VIDEO_REQUEST_CODE)
        companion.appCompatActivity?.startActivityForResult(getVideoGalleryIntent, GET_VIDEO_REQUEST_CODE)
        companion.fragment?.startActivityForResult(getVideoGalleryIntent, GET_VIDEO_REQUEST_CODE)
        companion.supportFragment?.startActivityForResult(getVideoGalleryIntent, GET_VIDEO_REQUEST_CODE)

        return this
    }

    /**
     * In the Fragment or Activity that you provided to Shutter via it's constructor, call [onActivityResult] on the return value of [snap].
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == GET_VIDEO_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                resultCallback?.onError("You cancelled finding a video.", ShutterUserCancelledOperation("User cancelled finding a video from gallery."))
                return true
            }

            val contentUriVideo = intent!!.data
            val videoInputStream = companion.getContext()!!.contentResolver.openInputStream(contentUriVideo)

            if (android.os.Environment.getExternalStorageState() != android.os.Environment.MEDIA_MOUNTED) {
                resultCallback?.onError("Error getting video from gallery. Unmount for device external storage and try again.", RuntimeException("User has mounted their device storage: ${directoryPath.absolutePath} with filename: $fileName"))
                return true
            }

            val nameOfApp = companion.getContext()!!.packageName.split(".").last()
            directoryPath = File("${directoryPath.absolutePath}/$nameOfApp")
            directoryPath.mkdirs()

            val videoFile: File = File(directoryPath, fileName + ".mp4")

            if (!videoFile.createNewFile()) {
                resultCallback?.onError("Error getting video from gallery.", RuntimeException("Error creating new video where video will save: ${directoryPath.absolutePath} with filename: $fileName"))
                return true
            }

            fileAbsolutePath = videoFile.absolutePath

            SaveVideoToFileAsyncTask().execute(SaveVideoToFileAsyncTaskData(videoFile, videoInputStream))

            return true
        } else {
            return false
        }
    }

    private inner class SaveVideoToFileAsyncTaskData(val videoFile: File, val videoInputStream: InputStream)
    private inner class SaveVideoToFileAsyncTask : AsyncTask<SaveVideoToFileAsyncTaskData, Int, ShutterResult?>() {
        override fun doInBackground(vararg dataArray: SaveVideoToFileAsyncTaskData): ShutterResult? {
            var outputStream: OutputStream? = null
            val data: SaveVideoToFileAsyncTaskData = dataArray[0]
            try {
                outputStream = FileOutputStream(data.videoFile)

                val bytes = ByteArray(1024)
                var read = data.videoInputStream.read(bytes)
                while (read != -1) {
                    outputStream.write(bytes, 0, read)
                    read = data.videoInputStream.read(bytes)
                }

                return ShutterResult(fileAbsolutePath)
            } catch (e: IOException) {
                resultCallback?.onError("Error getting video from gallery.", e)
                return null
            } finally {
                try {
                    data.videoInputStream.close()
                    // outputStream?.flush()
                    outputStream?.close()
                } catch (e: IOException) {
                    resultCallback?.onError("Error getting video from gallery.", e)
                    return null
                }
            }
        }

        protected fun onProgressUpdate(vararg progress: Int) {
        }

        override fun onPostExecute(result: ShutterResult?) {
            result?.let { resultCallback?.onComplete(it) }
        }
    }

}
