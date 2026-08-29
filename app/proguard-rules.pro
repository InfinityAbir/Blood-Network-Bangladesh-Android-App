# R8 / ProGuard rules for the Blood Network Android app.

# ---------- kotlinx.serialization ----------
# Keep serializer classes and their Companion so runtime lookup works after obfuscation.
-keepattributes *Annotation*, InnerClasses, Signature, ExceptionSignature

-dontnote kotlinx.serialization.AnnotationsKt
-dontwarn kotlinx.serialization.**

-keep,includedescriptorclasses class com.bloodnetwork.bangladesh.**$$serializer { *; }
-keepclassmembers class com.bloodnetwork.bangladesh.** {
    *** Companion;
}
-keepclasseswithmembers class com.bloodnetwork.bangladesh.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------- Retrofit / OkHttp ----------
# Retrofit and OkHttp ship their own consumer rules; these are defensive additions.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions

# Keep generic signatures of types used by Retrofit (List<T>, PagedResult<T>).
-keep,allowobfuscation,allowshrinking class com.bloodnetwork.bangladesh.data.model.**
-keepclassmembers,allowshrinking,allowobfuscation class com.bloodnetwork.bangladesh.data.model.** {
    <fields>;
}

# Media/ICU online resources are not used.

# ---------- kotlinx.coroutines ----------
-dontwarn kotlinx.coroutines.**

# ---------- SignalR client (notifications realtime) ----------
# Ships its own consumer rules; these are defensive in case R8 strips reflectively-used
# Gson/RxJava2 pieces it depends on internally.
-dontwarn com.microsoft.signalr.**
-dontwarn io.reactivex.**
-keep class com.microsoft.signalr.** { *; }
