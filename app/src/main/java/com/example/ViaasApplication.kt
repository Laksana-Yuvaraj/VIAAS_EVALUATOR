package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * ViaasApplication initializes application-wide components at startup.
 * Configures Firebase Firestore offline persistence with PersistentCacheSettings
 * to ensure student exam progress is cached locally on device and automatically
 * synchronized whenever network connectivity is restored.
 */
class ViaasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirestoreOfflinePersistence()
    }

    private fun initializeFirestoreOfflinePersistence() {
        try {
            FirebaseApp.initializeApp(this)

            // Resolve custom enterprise database ID if present, otherwise default
            val databaseId = try {
                getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                null
            }

            val db = if (!databaseId.isNullOrBlank()) {
                FirebaseFirestore.getInstance(databaseId)
            } else {
                FirebaseFirestore.getInstance()
            }

            // Configure persistent cache settings for robust offline exam support
            val cacheSettings = PersistentCacheSettings.newBuilder()
                .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()

            val firestoreSettings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
                .setLocalCacheSettings(cacheSettings)
                .build()

            db.firestoreSettings = firestoreSettings
            Log.i(TAG, "Firestore offline persistence initialized successfully with persistent cache settings for database: ${databaseId ?: "(default)"}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed or already initialized Firestore persistent cache settings: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ViaasApplication"
    }
}
