package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.reader.office.constant.MainConstant
import com.reader.office.mychanges.interfaces.OnBookmarkCallback
import com.reader.office.mychanges.interfaces.OnDeleteCallback
import com.reader.office.mychanges.interfaces.OnRenameCallback
import com.reader.office.mychanges.utils.*
import com.reader.office.officereader.AppActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.App.Companion.context
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogInfoFooterBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogInfoHeaderBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation.DashboardFragment.Companion.crossPromotionRemoteList
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.MenuBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isChangeFromOtherModuleDone
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.copy
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.getDataModelFromFile
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.getFormattedTime
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.shareDocument
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.snack
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

fun AppCompatActivity.showInfoDialog(
    view: View,
    title: String,
    message: String,
    type: String,
    onDismissResult: (() -> Unit)? = null,
    onResult: (() -> Unit)? = null,
) {
    val bindingHeader = DialogInfoHeaderBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
    val bindingFooter = DialogInfoFooterBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

    bindingHeader.title.text = title

    if (type == Constants.MESSAGE) {
        bindingFooter.cancelBtn.visibility = View.GONE
        bindingFooter.processBtn.text = getText(R.string.ok)
    }

    val powerMenu: PowerMenu = PowerMenu.Builder(this)
        .setHeaderView(bindingHeader.root) // header used for title
        .setFooterView(bindingFooter.root) // footer used for yes and no buttons
        .addItem(PowerMenuItem(message, false)) // this is body
        .setLifecycleOwner(this)
        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
        .setMenuRadius(10f)
        .setMenuShadow(10f)
        .setWidth(600)
        .setSelectedEffect(false)
        .setOnDismissListener {
            onDismissResult?.invoke()
        }
        .build()

    bindingFooter.processBtn.setOnClickListener {
        if (powerMenu.isShowing)
            powerMenu.dismiss()
        onResult?.invoke()
    }
    bindingFooter.cancelBtn.setOnClickListener {
        if (powerMenu.isShowing)
            powerMenu.dismiss()
    }
    powerMenu.showAtCenter(view)
}

fun AppCompatActivity.showSortingDialog(
    view: View,
    onResult: ((Int, PowerMenuItem) -> Unit)? = null,
    onDismissResult: (() -> Unit)? = null
) {
    val powerMenu: PowerMenu = PowerMenu.Builder(this)
        .setLifecycleOwner(this)
        .addItem(PowerMenuItem("Sort by date", R.drawable.ic_calendar_sort, false))
        .addItem(PowerMenuItem("Sort by name", R.drawable.ic_font_sort, false))
        .addItem(PowerMenuItem("Sort by size", R.drawable.ic_increase_size_option, false))
        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
        .setMenuRadius(10f)
        .setIconSize(20)
        .setMenuShadow(10f)
        .setTextGravity(Gravity.CENTER)
        .setSelectedEffect(false)
        .setOnDismissListener {
            onDismissResult?.invoke()
        }
        .build()

    powerMenu.setOnMenuItemClickListener { position, item ->
        powerMenu.dismiss()
        Timber.d("1122113----$position")
        onResult?.invoke(position, item)
    }

    powerMenu.showAsDropDown(view)
}

fun Activity.performViewPagerAdapterItemClickListeners(
    dataModel: DataModel,
    dataViewModel: DataViewModel
) {
    if (dataModel.name.toLowerCase(Locale.ROOT).endsWith(Constants.PDF))
        openActivity(ViewerActivity::class.java) {
            putSerializable(Constants.DOC_MODEL, dataModel)
        }
    else {
        try {
            val intent = Intent()
            intent.setClass(this, AppActivity::class.java)
            intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, dataModel.path)
            intent.putExtra("FileName", dataModel.name)
            intent.putExtra("IsBookmarked", dataModel.isBookmarked)
//                intent.putExtra("crossPromotionRemoteList",crossPromotionRemoteList as Serializable)
            val sharedPref = SharedPref(this)
            sharedPref.putListRemoteList("crossPromotionRemoteList", crossPromotionRemoteList)

            GlobalScope.launch(Dispatchers.Main) {
//                if (!dataModel.isRecent) {
//                    Companions.isRecentAdded = true
//                    dataViewModel.addRecent(dataModel.path, true)
//                }
                if (dataViewModel.findDataModel(dataModel.path).isEmpty()) {
                    dataModel.isRecent = true
                    dataViewModel.addDataModel(dataModel)
                }else{
                    dataViewModel.addRecent(dataModel.path, true)
                }
            }
            startActivityForResult(intent, 102)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            println("run///ex" + ex.printStackTrace())
        }
    }
}

fun Context.showMenuBtnBottomSheet(
    view: View,
    item: String,
    docModel: DataModel,
    mainViewModel: DataViewModel,
    onFinish: (() -> Unit)? = null
) {
    when (item) {
        MenuBottomSheet.RENAME -> {
            if (!docModel.isLock) {
                val currentFile = File(docModel.path)
                val dialog = FileSaveDialog(
                    this,
                    currentFile.parent ?: "",
                    currentFile.name,
                    type = Constants.RENAME
                ) { fileName ->
                    Timber.i(currentFile.parent ?: "" + fileName)
                    val newFile = File("${currentFile.parent}/$fileName")
                    var doc: DataModel? = null
                    GlobalScope.launch(Dispatchers.IO) {
                        copy(currentFile, newFile)
                    }.invokeOnCompletion {
                        GlobalScope.launch(Dispatchers.IO) {
                            doc = if (newFile.exists()) {
                                getDataModelFromFile(newFile)
                            } else {
                                getDataModelFromFile(newFile)
                            }
                        }.invokeOnCompletion {
                            GlobalScope.launch(Dispatchers.IO) {
                                doc?.let { doc ->
                                    doc.id = docModel.id
                                    doc.isBookmarked = docModel.isBookmarked
                                    doc.isLock = docModel.isLock
                                    doc.isRecent = docModel.isRecent
                                    doc.isMyFile = docModel.isMyFile
                                    val newDate = getFormattedTime(newFile.lastModified())
                                    mainViewModel.renameFile(currentFile, newFile,newDate)
                                    try {
                                        mainViewModel.updateDocModelNameByLength(
                                            docModel.encryptedLength,
                                            fileName
                                        )
                                        mainViewModel.updateDocModelPathByLength(
                                            docModel.encryptedLength,
                                            newFile.path
                                        )
                                        mainViewModel.updateDocModelDataByLength(
                                            docModel.encryptedLength,
                                            newDate
                                        )
                                    } catch (e: Exception) {
                                    }
                                    Timber.i("Success----%s", newFile.path)
                                }
                            }.invokeOnCompletion {
                                currentFile.delete()
                                GlobalScope.launch(Dispatchers.Main) {
                                    mainViewModel.setObserveForRefreshHomeState(true)
                                    onFinish?.invoke()
                                }
                            }
                        }
                    }
                }
                dialog.show()
            } else {
                showToast("Encrypted file cannot be rename")
            }
        }
        MenuBottomSheet.SHARE -> {
            if (!docModel.isLock) {
                shareDocument(docModel.path)
            } else {
                showToast("Encrypted file cannot be share")
            }
        }
        MenuBottomSheet.DELETE -> {
            if (!docModel.isLock) {
                (this as AppCompatActivity).showInfoDialog(
                    view,
                    "Delete File",
                    "Are you sure you want to delete?",
                    Constants.ALERT
                ) {
                    GlobalScope.launch(Dispatchers.Default) {
//                    mainViewModel.setObserveChanges(true)
                        deleteFileFromPath(docModel.path)
                        mainViewModel.deleteDocModelByPath(docModel.path)
                        mainViewModel.removeFile(docModel)
                        mainViewModel.setObserveForRefreshHomeState(true)
                        GlobalScope.launch(Dispatchers.Main) {
                            onFinish?.invoke()
                        }
                    }
                }
            } else {
                showToast("Encrypted file cannot be delete")
            }
        }
        MenuBottomSheet.BOOKMARK -> {
            docModel.apply {
                isBookmarked = !isBookmarked
                GlobalScope.launch(Dispatchers.Default) {
                    if (mainViewModel.findDataModel(docModel.path).isEmpty()) {
                        docModel.isBookmarked = true
                        mainViewModel.addDataModel(docModel)
                    } else {
                        mainViewModel.addBookmarkByPath(path, isBookmarked)
                    }
                }
                if (isBookmarked) {
                    view.snack("Bookmarked")
                } else {
                    view.snack("Remove from Bookmarks")
                }
            }
        }
    }
}

fun renameFromOtherModule(mainViewModel: DataViewModel){
    renameCallback = object : OnRenameCallback {
        override fun onRename(currentFile:File,newFile:File) {
            var doc: DataModel? = null
            val docModel = context?.getDataModelFromFile(currentFile)
            GlobalScope.launch(Dispatchers.IO) {
                doc = if (newFile.exists()) {
                    context?.getDataModelFromFile(newFile)
                } else {
                    context?.getDataModelFromFile(newFile)
                }
            }.invokeOnCompletion {
                GlobalScope.launch(Dispatchers.IO) {
                    doc?.let { doc ->
                        val newDate = getFormattedTime(newFile.lastModified())
                        mainViewModel.renameFile(currentFile, newFile,newDate)
                        docModel?.let { docModel ->
                            if(docModel.name!="") {
                                doc.id = docModel.id
                                doc.isBookmarked = docModel.isBookmarked
                                doc.isLock = docModel.isLock
                                doc.isRecent = docModel.isRecent
                                doc.isMyFile = docModel.isMyFile
                                try {
                                    mainViewModel.updateDocModelNameByLength(
                                        docModel.encryptedLength,
                                        newFile.name
                                    )
                                    mainViewModel.updateDocModelPathByLength(
                                        docModel.encryptedLength,
                                        newFile.path
                                    )
                                    mainViewModel.updateDocModelDataByLength(
                                        docModel.encryptedLength,
                                        newDate
                                    )
                                } catch (e: Exception) {
                                }
                            }
                        }
                        Timber.i("Success----%s", newFile.path)
                    }
                }.invokeOnCompletion {
                    currentFile.delete()
                    GlobalScope.launch(Dispatchers.Main) {
                        mainViewModel.setObserveForRefreshHomeState(true)
                        renameCompleteCallback?.onRename(currentFile,newFile)
                        isChangeFromOtherModuleDone = true
                    }
                }
            }
        }
    }
}
fun deleteFromOtherModule(mainViewModel: DataViewModel){
    deleteCallback = object : OnDeleteCallback {
        override fun onDelete(currentFile: File) {
            val docModel = context?.getDataModelFromFile(currentFile)
            GlobalScope.launch(Dispatchers.Default) {
                docModel?.let { docModal ->
                    deleteFileFromPath(docModel.path)
                    mainViewModel.deleteDocModelByPath(docModel.path)
                    mainViewModel.removeFile(docModel)
                    mainViewModel.setObserveForRefreshHomeState(true)
                    GlobalScope.launch(Dispatchers.Main) {
                        deleteCompleteCallback?.onDelete(currentFile)
                        isChangeFromOtherModuleDone = true
                    }
                }
            }
        }
    }
}
fun bookmarkFromOtherModule(mainViewModel: DataViewModel){
    bookmarkCallback = object : OnBookmarkCallback {
        override fun onBookmark(currentFile: File,isBookmark:Boolean) {
            val docModel = context?.getDataModelFromFile(currentFile)
            docModel?.apply {
                isBookmarked = !isBookmarked
                GlobalScope.launch(Dispatchers.IO) {
                    if (mainViewModel.findDataModel(docModel.path).isEmpty()) {
                        mainViewModel.addDataModel(docModel)
                        Log.d(TAG, "onBookmark:  mainViewModel.addDataModel(docModel)")
                    } else {
                        mainViewModel.addBookmarkByPath(path, isBookmarked)
                        Log.d(TAG, "onBookmark:  mainViewModel.addBookmarkByPath(path, isBookmarked)")
                    }
                }.invokeOnCompletion {
                    GlobalScope.launch(Dispatchers.Main) {
                        if (isBookmarked) {
                            context?.showToast("Bookmarked")
                        } else {
                            context?.showToast("Remove from Bookmarks")
                        }
                        isChangeFromOtherModuleDone = true
                        bookmarkCompleteCallback?.onBookmark(currentFile,isBookmarked)
                    }
                }
            }
        }
    }
}

private const val TAG = "ViewExtensionsFun"