# ── WiFi Card Master Pro – ProGuard Rules ─────────

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.wdmaster.app.domain.model.** { *; }
-keep class com.wdmaster.app.data.local.entity.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class com.wdmaster.app.presentation.test.JavaScriptInterface {
    public *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**