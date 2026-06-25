# Add project specific ProGuard rules here.
# Keep our core app files from being shrunk/obfuscated incorrectly
-keep class com.echomusic.app.** { *; }

# Keep Media3 classes
-keep class androidx.media3.** { *; }
