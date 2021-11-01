package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote.DataRepository
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote.FirebaseRemoteConfigDataClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote.RemoteConfigModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories.UtilsRepository
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.AllowPermissionBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.isPermissionsGranted

class UtilsViewModel(application: Application) : AndroidViewModel(application) {
    private var repository = UtilsRepository.getInstance(application, SharedPref(application))
    private var dataRepositry = DataRepository.getInstance(application, FirebaseRemoteConfigDataClass().init(),SharedPref(application))

    var onSaveFileHomeFragmentCallback: (() -> Unit)? = null

    fun checkStoragePermission(
        context: Context,
        onPermissionsDenied: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        onPermissionsGranted: (() -> Unit)? = null,
    ) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        onPermissionsGranted?.invoke()
                    }

                    if (report?.isAnyPermissionPermanentlyDenied == true) {
                        onPermissionsDenied?.invoke()
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { error ->
                onError?.invoke("Error occurred! $error")
            }
            .onSameThread()
            .check()
    }

    var remoteConfigModel : RemoteConfigModel? = dataRepositry.remoteConfigModel

    fun syncRemoteConfig(){
        dataRepositry.syncConfigData()
    }

    fun isPremiumUser(): Boolean {
//        return repository.isPremiumUser
        return Companions.isPurchased
    }

    fun setPremiumUser(isPremiumUser: Boolean) {
        repository.setPremiumUser(isPremiumUser)
    }

    fun setAutoAdsRemoved(isRemoved: Boolean): Unit = repository.setAutoAdsRemoved(isRemoved)

    fun isAutoAdsRemoved(): Boolean {
        return if (repository.isAutoAdsRemoved.value == null) {
            repository.isPremiumUser
        } else {
            repository.isAutoAdsRemoved.value!!
        }
    }

    fun androidVersionIs11OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    @SuppressLint("NewApi")
    fun checkPermission11(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun checkPermission(activity: AppCompatActivity):Boolean{
        return if (androidVersionIs11OrAbove()) {
            checkPermission11()
        }else{
            activity.isPermissionsGranted()
        }
    }


}