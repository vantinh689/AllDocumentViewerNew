package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.isPremiumUserKey
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref

@Keep
class UtilsRepository(application: Application,var sharedPref: SharedPref) {

    val isPremiumUser = sharedPref.getBoolean(isPremiumUserKey)
    val isAutoAdsRemoved = MutableLiveData<Boolean>()

    fun setPremiumUser(isRemoved: Boolean) {
        sharedPref.putBoolean(isPremiumUserKey, isRemoved)
    }

    fun setAutoAdsRemoved(isRemoved: Boolean) {
        isAutoAdsRemoved.postValue(isRemoved)
    }

    companion object {
        private const val LOG_TAG = "UtilsRepository"

        @Volatile
        private var INSTANCE: UtilsRepository? = null

        fun getInstance(application: Application, sharedPref: SharedPref): UtilsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: UtilsRepository(application,sharedPref)
                        .also { INSTANCE = it }
            }

        private const val TAG = "UtilsRepository"
    }
}