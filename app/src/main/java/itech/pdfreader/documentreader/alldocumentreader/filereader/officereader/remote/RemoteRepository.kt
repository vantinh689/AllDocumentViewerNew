package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.appOpenCounterKey
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.isPremiumUserKey
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.remoteKeyforDocumantViewer

private const val AGREED_WITH_TERMS_KEY = "agreed_with_terms"
private const val AUTO_DETECTION_PURCHASED = "spKeyHsPurchasedAutoDetect"
private const val DO_NOT_SHOW_EXIT_DIALOG = "donotShowExitDialogue"
private const val IS_RATING_APPLIED = "isRatingApplied"

class DataRepository(
    var application: Application,
    var firebaseRemoteConfig: FirebaseRemoteConfig,
    var sharedPref: SharedPref
) {

    private val LOG_TAG = "BillingViewModel"
    var remoteConfigModel: RemoteConfigModel? = getRemoteValues()

    private fun getRemoteValues(): RemoteConfigModel? {
        return if (firebaseRemoteConfig.getString(remoteKeyforDocumantViewer()) == null) {
            Log.d("remote_status", "Status is  Data is null")
            RemoteConfigModel()
        } else {
            Log.d(
                "remote_status",
                "Data ${firebaseRemoteConfig.getString(remoteKeyforDocumantViewer())}"
            )
            Gson().fromJson(
                firebaseRemoteConfig.getString(remoteKeyforDocumantViewer()),
                RemoteConfigModel::class.java
            )
        }
    }

    val isAutoAdsRemoved = MutableLiveData<Boolean>()

    fun setAutoAdsRemoved(isRemoved: Boolean) {
        isAutoAdsRemoved.postValue(isRemoved)
    }

    val isUserLogin = MutableLiveData<Boolean>()

    fun setUserLogin(isRemoved: Boolean) {
        isUserLogin.postValue(isRemoved)
    }

    val isPremiumUser = sharedPref.getBoolean(isPremiumUserKey)

    fun setPremiumUser(isRemoved: Boolean) {
        sharedPref.putBoolean(isPremiumUserKey, isRemoved)
    }

    fun getAppOpenCount() = sharedPref.getInt(appOpenCounterKey)
    fun setAppOpentCount(count: Int) = sharedPref.putInt(appOpenCounterKey, count)

    fun setAgreedWithTerms() = sharedPref.putBoolean(AGREED_WITH_TERMS_KEY, true)
    fun isAgreedWithTerms() = sharedPref.getBoolean(AGREED_WITH_TERMS_KEY)

    fun setRatingApplied() = sharedPref.putBoolean(IS_RATING_APPLIED, true)
//    fun isRatingApplied() = tinyDB.getBoolean(IS_RATING_APPLIED, false)

    fun setDoNotShowExitDialogue() = sharedPref.putBoolean(DO_NOT_SHOW_EXIT_DIALOG, true)
    fun isShowExitDialogue() = sharedPref.getBoolean(DO_NOT_SHOW_EXIT_DIALOG)
    fun setAutoDetectionPurchased() = sharedPref.putBoolean(AUTO_DETECTION_PURCHASED, true)

    fun getTinyDb(): SharedPref {
        return sharedPref
    }

    fun syncConfigData() {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                remoteConfigModel = Gson().fromJson(
                    firebaseRemoteConfig.getString(remoteKeyforDocumantViewer()),
                    RemoteConfigModel::class.java
                )
            }.addOnSuccessListener {
                Log.d("remote_status", "Status is $it")
            }.addOnFailureListener {
                Log.d("remote_status", "Status is ${it.stackTrace}")
            }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(application: Application, firebaseRemoteConfig: FirebaseRemoteConfig,
                        sharedPref: SharedPref): DataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: DataRepository(application,firebaseRemoteConfig,sharedPref)
                        .also { INSTANCE = it }
            }

        private const val TAG = "DataRepository"
    }

}