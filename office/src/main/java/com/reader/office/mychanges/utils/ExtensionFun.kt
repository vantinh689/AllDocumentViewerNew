package com.reader.office.mychanges.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.reader.office.R
import com.reader.office.databinding.DialogInfoFooter2Binding
import com.reader.office.databinding.DialogInfoHeader2Binding
import com.reader.office.mychanges.bottomsheetfragment.ViewerToolsBottomSheet2
import com.reader.office.mychanges.dialogs.FileSaveDialog2
import com.reader.office.mychanges.interfaces.OnBookmarkCallback
import com.reader.office.mychanges.interfaces.OnDeleteCallback
import com.reader.office.mychanges.interfaces.OnRenameCallback
import com.reader.office.mychanges.model.DataModel2
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build

fun Activity.hideStatusBar() {
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
//    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    if (Build.VERSION.SDK_INT >= 22) {
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT
    }
}

fun Activity.setWindowFlag(bits: Int, on: Boolean) {
    val win = window
    val winParams = win.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.attributes = winParams
}

fun AppCompatActivity.showBottomSheet(view: View, docModel: DataModel2){
    supportFragmentManager.let {
        ViewerToolsBottomSheet2.newInstance(Bundle().apply {
            putSerializable(DOC_MODEL, docModel)
        }).apply {
            show(it, tag)
            mListener = object : ViewerToolsBottomSheet2.ItemClickListener {
                override fun onItemClick(item: String) {
                    when(item){
                        ViewerToolsBottomSheet2.SHARE -> shareDocument(docModel.path)
                        ViewerToolsBottomSheet2.RENAME -> showRenameDialog(this@showBottomSheet,docModel.name,docModel.path)
                        ViewerToolsBottomSheet2.DELETE -> showDeleteDialog(this@showBottomSheet,view,
                            File(docModel.path)
                        )
                        ViewerToolsBottomSheet2.BOOKMARK ->{
                            bookmarkCallback?.onBookmark(File(docModel.path),docModel.isBookmarked)
                        }
                    }
                }
            }
        }
    }
}

fun Context.shareDocument(path: String) {
    try {
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                applicationContext,
                "itech.pdfreader.documentreader.alldocumentreader.filereader.officereader" + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
    }
}

fun Context.getDataModelFromFile(file: File, isPdfLocked: Boolean = false, isBookmark:Boolean=false): DataModel2 {
    var str = "${
        MimeTypeMap.getFileExtensionFromUrl(file.path) ?: file.path.substring(
            file.path.lastIndexOf(
                "."
            )
        )
    }"
    if (str.isEmpty()) {
        str = file.path.substring(file.path.lastIndexOf(".")).split(".")[1]
    }
    return DataModel2(
        file.name,
        sizeFormatter(file.length()),
        str,
        file.path,
        isBookmarked = isBookmark,
        isLock = isPdfLocked,
        lastModifiedTime = getFormattedTime(file.lastModified()),
        encryptedLength = file.length(),
        encryptedTime = file.lastModified()
    )
}

fun getFormattedTime(lastModifiedTime: Long): String {
    return lastModifiedTime.millisToDate().formatToDMY()
}

fun Date.formatToDMY(): String {
    val df = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    return df.format(this)
}

fun Long.millisToDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.time
}

fun Context.sizeFormatter(size: Long) = android.text.format.Formatter.formatShortFileSize(this, size)

@BindingAdapter("setListItemimageSrc")
fun setListItemImageSrc(imageView: ImageView, name: String) {
    if (name.contains(PDF) || name.contains(".PDF"))
        imageView.setImageResource(R.drawable.pdf_ic)
    else if (name.contains(excelExtension) || name.contains(excelWorkbookExtension) || name.contains(
            ".excel"
        ) )
        imageView.setImageResource(R.drawable.excel_ic)
    else if (name.contains(PPT) || name.contains(".pptx") || name.contains(".powerpoint"))
        imageView.setImageResource(R.drawable.ppt_ic)
    else if (name.contains(docExtension) || name.contains(docxExtension))
        imageView.setImageResource(R.drawable.word_ic)
    else
        imageView.setImageResource(R.drawable.all_doc_ic)
}

@BindingAdapter("setListItemCardViewBackground")
fun setListItemCardViewBackground(cardView: CardView, name: String) {
    if (name.contains(PDF) || name.contains(".PDF"))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorPdf,
                cardView.context.theme
            )
        )
    else if (name.contains(excelExtension) || name.contains(excelWorkbookExtension) || name.contains(
            ".excel"
        ))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorExcel,
                cardView.context.theme
            )
        )
    else if (name.contains(PPT) || name.contains(".pptx") || name.contains(".powerpoint"))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorPPT,
                cardView.context.theme
            )
        )
    else if (name.contains(docExtension) || name.contains(docxExtension))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorDoc,
                cardView.context.theme
            )
        )
    else
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorAny,
                cardView.context.theme
            )
        )
}

fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    val snack = Snackbar.make(this, message, duration)
    snack.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, context.theme))
    snack.show()
}

fun String.isValidFileName(): Boolean {
    return this.toLowerCase(Locale.ROOT).contains(PDF) ||
            this.toLowerCase(Locale.ROOT).contains(PPT) ||
            this.toLowerCase(Locale.ROOT).contains(PPTX) ||
            this.toLowerCase(Locale.ROOT).contains(docExtension) ||
            this.toLowerCase(Locale.ROOT).contains(docxExtension) ||
            this.toLowerCase(Locale.ROOT).contains(excelExtension) ||
            this.toLowerCase(Locale.ROOT).contains(excelWorkbookExtension) ||
            this.toLowerCase(Locale.ROOT).contains(excelWorkbookExtension) ||
            this.toLowerCase(Locale.ROOT).contains(PDF)
}

fun String.getFileNameExtension(): String {
    return substring(lastIndexOf("."))
}

fun String.changeExtension(newExtension: String): String {
    return if (this.contains(".")) {
        val i = lastIndexOf('.')
        val name = substring(0, i)
        name + newExtension
    } else {
        this + newExtension
    }
}

private fun showRenameDialog(context: Context, fileName: String, filePath:String) {
    val dialog = FileSaveDialog2(context, filePath, fileName,
        ViewerToolsBottomSheet2.RENAME
    ) { fileName: String? ->
        val currentFile = File(filePath)
        val newFile = File("${currentFile.parent}/$fileName")
        GlobalScope.launch(Dispatchers.IO) {
            copy(currentFile, newFile)
        }.invokeOnCompletion {
            renameCallback?.onRename(currentFile,newFile)
        }
    }
    dialog.show()
}

private fun showDeleteDialog(context: Context, view: View, currentFile: File) {
    (context as AppCompatActivity).showInfoDialog(view,
        "Delete File",
        "Are you sure you want to delete?",
        ALERT
    ) {
        deleteCallback?.onDelete(currentFile)
    }
}

fun AppCompatActivity.showInfoDialog(
    view: View,
    title: String,
    message: String,
    type: String,
    onDismissResult: (() -> Unit)? = null,
    onResult: (() -> Unit)? = null,
) {
    val bindingHeader = DialogInfoHeader2Binding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
    val bindingFooter = DialogInfoFooter2Binding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

    bindingHeader.title.text = title

    if (type == MESSAGE) {
        bindingFooter.cancelBtn.visibility = View.GONE
        bindingFooter.processBtn.text = "Ok"
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


@Throws(IOException::class)
fun copy(src: File?, dst: File?) {
    val `in`: InputStream = FileInputStream(src)
    `in`.use { `in` ->
        val out: OutputStream = FileOutputStream(dst)
        out.use { out ->
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        }
    }
}


var renameCallback : OnRenameCallback?=null
var renameCompleteCallback : OnRenameCallback?=null

var deleteCallback : OnDeleteCallback?=null
var deleteCompleteCallback : OnDeleteCallback?=null

var bookmarkCallback : OnBookmarkCallback?=null
var bookmarkCompleteCallback : OnBookmarkCallback?=null

const val excelExtension = ".xls"
const val excelWorkbookExtension = ".xlsx"
const val docExtension = ".doc"
const val docxExtension = ".docx"
const val PPT = ".ppt"
const val PPTX = ".pptx"
const val PDF: String = ".pdf"
const val DOC_MODEL = "file"
const val RENAME: String = "RENAME"
const val MESSAGE: String = "Message"
const val ALERT: String = "Alert"