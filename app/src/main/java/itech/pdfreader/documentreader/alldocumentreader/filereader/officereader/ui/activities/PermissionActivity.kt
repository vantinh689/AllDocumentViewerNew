package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivityPermissionBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.AllowPermissionBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionActivity : BaseActivity() {
    lateinit var binding: ActivityPermissionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openActivityWithClearTask(DashboardActivity::class.java)
        }else{
            setContentView(binding.root)
        }

        binding.allowPermissionBtnUnChecked.setOnClickListener {
            checkAllPermissions()
        }

        binding.letsGoBtn.setOnClickListener {
//            retrieveAllFiles()
            openActivityWithClearTask(DashboardActivity::class.java)
        }
    }

    private fun checkAllPermissions() {
        onPermissionFailed = {
            showToast("Permission must required")
        }
        onPermissionGranted = {
            binding.allowPermissionBtn.isVisible(true)
            binding.allowPermissionBtnUnChecked.isVisible(false)
            binding.letsGoBtn.isVisible(true)
        }
        onPermissionError = {
            showToast("There is some error while getting permission")
        }
        if (utilsViewModel.androidVersionIs11OrAbove()) {
            if (!utilsViewModel.checkPermission11()) {
                supportFragmentManager.let {
                    AllowPermissionBottomSheet.newInstance(Bundle()).apply {
                        show(it, tag)
                        mListener = object : AllowPermissionBottomSheet.ItemClickListener {
                            override fun onItemClick(item: String) {
                                if (item == Constants.ALLOW) {
                                    get11Permission()
                                } else if (item == Constants.CANCEL) {
                                    onPermissionFailed?.invoke()
                                }
                            }
                        }
                    }
                }
            } else {
                onPermissionGranted?.invoke()
            }
        } else {
            getStoragePermission(onPermissionGranted = {
                onPermissionGranted?.invoke()
            }, onPermissionDenied = {
                onPermissionFailed?.invoke()
            })
        }
    }

    @SuppressLint("InlinedApi")
    fun get11Permission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
            someActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            someActivityResultLauncher.launch(intent)
        }
    }

    private var someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (utilsViewModel.checkPermission11()) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionFailed?.invoke()
                showToast("Permission must required to retrieve files")
            }
        }


    var onPermissionGranted: (() -> Unit)? = null
    var onPermissionFailed: (() -> Unit)? = null
    var onPermissionError: ((String) -> Unit)? = null

//    private fun retrieveAllFiles() {
//        GlobalScope.launch(Dispatchers.Main) {
//            progressDialog.show()
//        }.invokeOnCompletion {
//            dataViewModel.retrieveFilesFormDevice { allFilesFromDevice ->
//                GlobalScope.launch(Dispatchers.IO) {
//                    allFilesFromDevice.forEach {
//                        try {
//                            if (it.path.contains(Constants.PDF))
//                                if (documentUtils.isPDFEncrypted(it.path)) {
//                                    it.isLock = true
//                                }
//                            dataViewModel.addDocModel(it)
//                        } catch (e: Exception) {
//                        }
//                    }
//                }.invokeOnCompletion {
//                    GlobalScope.launch(Dispatchers.Main) {
//                        progressDialog.dismiss()
//                        openActivity(DashboardActivity::class.java)
//                    }
//                }
//            }
//        }
//    }
}