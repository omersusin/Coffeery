# Coffeery — keep rules.
# Room generates code at build time; no runtime reflection rules needed for the
# entities used here. Keep model classes to be safe if minify is enabled later.
-keep class co.coffeery.app.data.model.** { *; }

# Room entities (data/local)
-keep class co.coffeery.app.data.local.** { *; }

# JSON parsing (org.json reflection)
-keep class org.json.** { *; }
-dontwarn org.json.**

# Kotlin data classes used for StateFlow
-keep class co.coffeery.app.ui.screens.root.AppUiState { *; }
-keep class co.coffeery.app.util.BrewResult { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# General
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.auth.**

# Google Drive API
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.services.drive.**
-keep class com.google.api.client.http.** { *; }
-dontwarn com.google.api.client.http.**
-keep class com.google.http.client.** { *; }
-dontwarn com.google.http.client.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
-dontwarn javax.naming.**
-dontwarn com.google.api.client.http.**
