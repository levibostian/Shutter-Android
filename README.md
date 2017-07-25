# Shutter-Android
It's finally easy to take photos/videos via camera or get photos/videos from gallery on Android.

### What is Shutter?

Shutter is an Android library to take photos, record videos ([coming soon](https://github.com/levibostian/Shutter-Android/issues/1)), pick images/videos from gallery ([coming soon](https://github.com/levibostian/Shutter-Android/issues/2)), with ease. I always have difficulty working with images/videos while developing Android apps *especially since file providers were added*.

### Why use Shutter?

* Less bugs. No more publishing apps and have your app crash on user's devices because of file permission issues you did not handle or boilerplate code you forgot to include (believe me, I have done this many times).
* No more copy/paste of boilerplate code. Install lib, call Shutter, and be done.
* Lightweight.
* No Android runtime permissions needed. No need to ask for reading/writing files permissions. 
* Java and Kotlin support. Shutter-Android is written in Kotlin :)

## Install

Install Shutter-Android via [JitPack.io](https://jitpack.io/#levibostian/Shutter-Android).

Add the following to the root build.gradle file (/build.gradle) at the end of repositories:

```
allprojects {
	repositories {
	    ...
		maven { url 'https://jitpack.io' }
	}
}
```

Then, add the following to your project build.gradle file (app/build.gradle)

```
dependencies {
    compile 'com.github.levibostian:Shutter-Android:0.1.0'
}
```

## Take images easily with Shutter

When you want to take a photo in your Activity or Fragment, call Shutter:

```
shutterResultListener = Shutter.with(this)
    .takePhoto()
    .usePrivateAppInternalStorage()
    .snap(object : Shutter.ShutterResultCallback {
        override fun onComplete(result: Shutter.ShutterResult) {
            result.absoluteImageFile // <--- file:// path to the image.
        }
        override fun onError(humanReadableErrorMessage: String, error: Throwable) {
            Log.d("SHUTTER_EXAMPLE_APP", "Error encountered: ${error.message}")
            Snackbar.make(findViewById(android.R.id.content), humanReadableErrorMessage, Snackbar.LENGTH_LONG).show()
        }
})
```

When you call `.snap()` to tell Shutter to take a photo, save the result object. You will need to call if on your Activity or Fragment's `onActivityResult()` call:

```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (!shutterResultListener!!.onActivityResult(requestCode, resultCode, data)) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```

Shutter-Android uses the [FileProvider API](https://developer.android.com/reference/android/support/v4/content/FileProvider.html) to take photos. Because of this, Android requires you to add the following to your manifest file:

```
<application
   ...>
    <provider android:name="android.support.v4.content.FileProvider"
              android:authorities="${applicationId}.fileprovider"
              android:exported="false"
              android:grantUriPermissions="true">
        <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
                   android:resource="@xml/file_paths" />
    </provider>
</application>
```

Then, create a file: `app/src/main/res/xml/file_paths.xml` to specify permissions of the FileProvider API.

If you call `.usePrivateAppInternalStorage()` while using Shutter, include `<files-path name="internal_files" path="Pictures/" />` in this `file_paths.xml` file. If you call `.usePrivateAppExternalStorage()` while using Shutter, include `<external-files-path name="external_files" path="Pictures/" />` in this `files_paths.xml` file.

Complete `file_paths.xml` file:

```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="internal_files" path="Pictures/" />
    <external-files-path name="external_files" path="Pictures/" />
</paths>
```

More info about how to specify permissions can be found in the [official docs](https://developer.android.com/reference/android/support/v4/content/FileProvider.html#SpecifyFiles)

Done!

**Notes/Warnings:**

* The Shutter API might confuse you because you are able to save your captured media to "private internal or external storage" with the option to also "save the captured media to the gallery". This is true but not true.

*TL;DR Your photos/videos captured with Shutter may be added to the public gallery on the Android device no matter how you setup Shutter.*

Android is a fragmented operating system as we all know. Even if you decide to save your photos to private internal storage and not save to the gallery, your photo still *might* be saved to the gallery. Shutter has no control over this. Every user device will behave differently depending on the camera app that is installed. Shutter takes photos by using an Android Intent which asks a camera app on the user's device to take the photo for us. That camera app has the ability to do with your photo whatever it wishes after it is taken including but not limited to saving the photo to the public gallery.

The only way to enforce your photos are private to your app and your app only is to add the camera functionality to your own app which we will not be covering in Shutter at this time and probably never.

"Why does shutter even give the ability to add the captured media to the public gallery if it might do it already?" you answered your own question there. *Might* is the keyword. Adding the ability to add the media to the gallery through Shutter is to assert it is added to the gallery even if the camera app decides not to when the photo/video is captured.

## Contribute

Shutter is open for pull requests. Please, read the STYLE.md doc in the root of this project which will answer some questions for you as to why Shutter is built the way it is.

## Credits

* API design inspiration came from the [kayvannj PermissionUtil library](https://github.com/kayvannj/PermissionUtil). Check this library out for an awesome way to work with Android permissions.