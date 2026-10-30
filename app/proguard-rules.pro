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

# ---------- ViewModels ----------
# VmFactory builds every ViewModel reflectively via getConstructor(BloodNetworkRepository).
# Nothing calls those constructors statically, so R8 would strip/alter them and release
# builds would crash on the first screen with NoSuchMethodException.
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ---------- Logging ----------
# Release builds must not write to Logcat (user ids, push/hub state). Strips every
# android.util.Log call; the verbose/debug/info/warn variants have no return-value use.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# ---------- kotlinx.coroutines ----------
-dontwarn kotlinx.coroutines.**

# ---------- SignalR client (notifications realtime) ----------
# Ships its own consumer rules; these are defensive in case R8 strips reflectively-used
# Gson/RxJava2 pieces it depends on internally.
-dontwarn com.microsoft.signalr.**
# SignalR logs through slf4j-api but ships no binding; R8 flags the missing StaticLoggerBinder.
-dontwarn org.slf4j.**
-dontwarn io.reactivex.**
-keep class com.microsoft.signalr.** { *; }
