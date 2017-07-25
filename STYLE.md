# Style guides for Shutter
This doc is designed to help contributors understand the design choices on how Shutter was built.

## Where do we save photos/videos for the user?

The user has the decision to save to internal or external private storage. That is all at this time as saving to public external storage requires read/write permissions and that causes extra pains for the dev. We may add this in the future if needed.

No matter where we are saving, we want to always follow this pattern with the absolute file path:

```
/Pictures/nameofapp/20170712_212332.jpg
```

We always start with specifying what type of file we are saving to. Then, we put the name of the app. This is to avoid collisions with other apps as well as keeping the files nice and neat on the device. When the user browses their files on their device, they can see their files nice and neat in a directory for that app. Finish it off with the file name which is randomly generated by the date or provided by the user.

## Why do you not provide a method for users to save to external public storage directory?

That directory requires permissions to save to. Because of that, I am not allowing it for now to ease of use.

This is flexible, however, if we were to check permissions on the calling fragment/activity when `.snap()` is called to throw a runtime exception if the permissions do not exist.

# Did I not answer your question?

Open a [issue](https://github.com/levibostian/Shutter-Android/issues/new) to ask your question.