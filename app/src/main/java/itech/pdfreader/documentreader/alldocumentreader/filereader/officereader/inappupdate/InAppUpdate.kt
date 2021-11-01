package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.inappupdate

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdate(val activity:Activity) {

    private val TAG = "MyInAppUpdate"
    private var myInAppUpdateManager: AppUpdateManager? = null
    private val MY_REQUEST_CODE = 111
    init {
        myInAppUpdateManager = AppUpdateManagerFactory.create(activity)
    }
    
    fun checkUpdate(onResult: ((Boolean?) -> Unit)? = null){
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = myInAppUpdateManager?.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        Log.d(TAG, "Checking for updates")
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                Log.e(TAG, "checkUpdate: Available", )
                // Request the update.
                startRequest(appUpdateInfo)
                onResult?.invoke(true)
            } else {

                Log.e(TAG, "checkUpdate: No Available", )

                onResult?.invoke(false)
            }
        }
    }
  
    private fun startRequest(appUpdateInfo: AppUpdateInfo) {
        myInAppUpdateManager?.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            AppUpdateType.FLEXIBLE,
            // The current activity making the update request.
            activity,
            // Include a request code to later monitor this update request.
            MY_REQUEST_CODE)
    }
}