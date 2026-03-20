# This file contains the default ProGuard rules for the app and libraries.
# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-dontwarn com.google.gson.**
-dontwarn sun.misc.**

# App models
-keep class com.example.esp32control.models.** { *; }

# Keep all Fragments
-keep class com.example.esp32control.ui.** { *; }

# Kotlin
-keepclassmembers class ** {
    *** **(lambda);
}
