# ============================================================================
# QuizApp ProGuard / R8 rules
# ----------------------------------------------------------------------------
# Generated for the v4.0 release pass. Each block matches a dependency declared
# in app/build.gradle.kts. Keep this file in sync when adding libraries.
# ============================================================================

# --- Diagnostics --------------------------------------------------------------
# Crashlytics needs source/line attributes to symbolicate; renamesourcefile
# replaces the obfuscated source name so it's "SourceFile" in the trace, which
# the Crashlytics mapping pipeline expects.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Reflection-driven libraries (Gson, Firestore, Compose, AdMob) need these.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisible*Annotations
-keepattributes AnnotationDefault

# --- @Keep ------------------------------------------------------------------
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# --- Kotlin runtime ---------------------------------------------------------
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keepclassmembers class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation { *; }

# --- Jetpack Compose --------------------------------------------------------
# Compose's runtime relies on a handful of intrinsics; R8 generally handles
# Compose well, but these guard against version regressions.
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.tooling.preview.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-dontwarn androidx.compose.**

# --- AndroidX core / lifecycle / navigation ---------------------------------
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.activity.** { *; }

# --- Koin -------------------------------------------------------------------
# Koin reflects on injected classes' constructors and KClasses.
-keep class org.koin.** { *; }
-keep class * implements org.koin.core.module.Module { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}
-dontwarn org.koin.**

# --- Firebase (BoM) ---------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore uses reflection on POJOs for serialization. Keep our remote/model
# classes that are written-to or read-from documents.
-keep class kz.yers.quiz.model.** { *; }
-keep class kz.yers.quiz.data.remote.** { *; }
-keepclassmembers class kz.yers.quiz.model.** {
    public <init>(...);
    <fields>;
}

# --- Gson -------------------------------------------------------------------
# We parse info.json into kz.yers.quiz.model classes via Gson; the keep above
# covers the targets. These are belt-and-suspenders.
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# --- Room (KSP-generated) ---------------------------------------------------
# Room codegen names DAO impls "<Dao>_Impl". Keep them all under our package.
-keep class kz.yers.quiz.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class kz.yers.quiz.data.local.entity.** { *; }
-dontwarn androidx.room.paging.**

# --- AdMob (play-services-ads) ----------------------------------------------
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- Media3 ExoPlayer -------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Lottie -----------------------------------------------------------------
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# --- Konfetti ---------------------------------------------------------------
-keep class nl.dionsegijn.konfetti.** { *; }
-dontwarn nl.dionsegijn.konfetti.**

# --- WorkManager ------------------------------------------------------------
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Play Review ------------------------------------------------------------
-keep class com.google.android.play.core.review.** { *; }
-dontwarn com.google.android.play.core.**

# --- Coil -------------------------------------------------------------------
-dontwarn coil.**

# --- DataStore --------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# --- App entry points -------------------------------------------------------
# MainActivity, Application, and ViewModels are loaded by name from the
# manifest / Koin module declarations.
-keep class kz.yers.quiz.MyApplication { *; }
-keep class kz.yers.quiz.MainActivity { *; }
-keep class kz.yers.quiz.QuizAppViewModel { *; }
-keep class kz.yers.quiz.repo.** { *; }
-keep class kz.yers.quiz.koin.** { *; }
-keep class kz.yers.quiz.data.notifications.** { *; }
-keep class kz.yers.quiz.data.prefs.** { *; }
