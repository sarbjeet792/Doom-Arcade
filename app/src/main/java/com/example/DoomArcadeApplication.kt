package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DoomArcadeApplication : Application() {

    var isCrashlyticsInitialized: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        initializeCrashlytics()
    }

    private fun initializeCrashlytics() {
        runCatching {
            val firebaseApp = FirebaseApp.initializeApp(this)
                ?: FirebaseApp.getApps(this).firstOrNull()

            if (firebaseApp != null) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                // Enable automatic crash reporting in production builds (and allow explicit collection configuration)
                crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
                crashlytics.setCustomKey("app_version_name", BuildConfig.VERSION_NAME)
                crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
                crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
                isCrashlyticsInitialized = true
                Log.i(TAG, "Firebase Crashlytics initialized successfully (collectionEnabled=${!BuildConfig.DEBUG}).")
            } else {
                Log.w(
                    TAG,
                    "FirebaseApp not initialized (google-services.json not present); Crashlytics standing by."
                )
            }
        }.onFailure { throwable ->
            Log.w(TAG, "Firebase Crashlytics initialization skipped: ${throwable.message}")
        }
    }

    fun recordNonFatalException(tag: String, throwable: Throwable) {
        runCatching {
            if (isCrashlyticsInitialized) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log("Non-fatal error in $tag: ${throwable.message}")
                crashlytics.recordException(throwable)
            }
        }
    }

    companion object {
        private const val TAG = "DoomArcadeApplication"
    }
}
