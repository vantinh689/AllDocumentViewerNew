package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.BuildConfig
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.remoteKeyforDocumantViewer

class FirebaseRemoteConfigDataClass() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init(): FirebaseRemoteConfig {
        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings: FirebaseRemoteConfigSettings = if (BuildConfig.DEBUG){
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
        }else{
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1800)
                .build()
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            remoteKeyforDocumantViewer() to Gson().toJson(
            RemoteConfigModel()
        ))).addOnCompleteListener {
            Log.d("FirebaseRemote", "Data ${remoteConfig.getString(remoteKeyforDocumantViewer())}")
            Log.d("FirebaseRemote", "Data Received Successfully Data ${it.result}")
        }.addOnFailureListener {
            Log.d("FirebaseRemote", "Failed")
        }.addOnCanceledListener {
            Log.d("FirebaseRemote", "Cancelled")
        }

        return remoteConfig
    }
}

