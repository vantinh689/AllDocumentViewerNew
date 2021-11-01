package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories

import android.app.Application
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.itextpdf.text.pdf.PdfReader
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.getDataModelFromFile
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

class FilesRepository(private val application: Application) {
    private val K: Long = 1024
    private val M = K * K
    private val G = M * K
    private val T = G * K

    private fun convertToStringRepresentation(value: Long): String? {
        val dividers = longArrayOf(T, G, M, K, 1)
        val units = arrayOf("TB", "GB", "MB", "KB", "B")
        if (value < 0) {
            return ""
        }
        var result: String? = null
        for (i in dividers.indices) {
            val divider = dividers[i]
            if (value >= divider) {
                result = format(value, divider, units[i])
                break
            }
        }
        return result
    }

    private fun format(
        value: Long,
        divider: Long,
        unit: String
    ): String {
        val result = if (divider > 1) value.toDouble() / divider.toDouble() else value.toDouble()
        return DecimalFormat("#,##0.#").format(result) + " " + unit
    }

    var allFiles = MutableLiveData<ArrayList<DataModel>>()
    var allFilesFromDevice = ArrayList<DataModel>()
    var allPdfFilesFromDevice = ArrayList<DataModel>()
    var allDocFilesFromDevice = ArrayList<DataModel>()
    var allPptFilesFromDevice = ArrayList<DataModel>()
    var allExcelFilesFromDevice = ArrayList<DataModel>()
    var allDocAndTxtFilesFromDevice = ArrayList<DataModel>()

    fun initializeAllFilesList(){
        allFilesFromDevice = ArrayList()
        allPdfFilesFromDevice = ArrayList()
        allDocFilesFromDevice = ArrayList()
        allPptFilesFromDevice = ArrayList()
        allExcelFilesFromDevice = ArrayList()
    }

    fun getAllFilesList(): LiveData<ArrayList<DataModel>> {
        allFiles.postValue(allFilesFromDevice)
        return allFiles
    }

    fun getFilePaths() {
        val pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
        val docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
        val xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")
        val xlsx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")
        val ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")
        val pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")
        val txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
        val rtx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtx")
        val rtf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtf")
        val html = MimeTypeMap.getSingleton().getMimeTypeFromExtension("html")
        val zip = MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip")
        //Table
//            val table = MediaStore.Files.getContentUri("external")
        val table = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        //Column
        val column = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        //Where
        val where = (MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?")
        //args
        //val args = arrayOf(pdf)
        val args = arrayOf(pdf, doc, docx, xls, xlsx, ppt, pptx, txt, rtx, rtf, html, zip)
        val fileCursor = application.contentResolver.query(table, column, where, args, null)
        val columnIndexData = fileCursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val size = fileCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val type = fileCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
        val lastModified = fileCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
        while (fileCursor.moveToNext()) {
            Timber.e(fileCursor.getString(columnIndexData))
            Timber.e(fileCursor.getString(size))
            Timber.e(fileCursor.getString(type))
            Timber.e(fileCursor.getString(lastModified))
            val path = fileCursor.getString(columnIndexData)
            fileCursor.getString(type)
            Timber.e("pathfile%s", path?:"")
            try {
                val fdelete = File(path?:"")
                if (fdelete.exists()) {
                    if (fdelete.path.endsWith("pdf") || fdelete.path.endsWith("PDF")) {
                        if(isPDFEncrypted(fdelete.path)) {
                            allPdfFilesFromDevice.add(application.getDataModelFromFile(fdelete,true))
                            allFilesFromDevice.add(application.getDataModelFromFile(fdelete,true))
                        }else{
                            allPdfFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                            allFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        }
                    }
                    if (fdelete.path.endsWith("doc") || fdelete.path.endsWith("docx")) {
                        allDocFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        allFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        allDocAndTxtFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        Timber.d(fdelete.path)
                    }
                    if (fdelete.path.endsWith("ppt") || fdelete.path.endsWith("pptx") || fdelete.path.endsWith("powerpoint")) {
                        allPptFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        allFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                    }
                    if (fdelete.path.endsWith("xls") || fdelete.path.endsWith("xlsx") || fdelete.path.endsWith("excel")) {
                        allExcelFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                        allFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                    }
                    if (fdelete.path.endsWith("txt")) {
                        allDocAndTxtFilesFromDevice.add(application.getDataModelFromFile(fdelete))
                    }
                } else {
                    Timber.e("FileNotExist%s", fdelete)
                }
            } catch (ex: Exception) {
            }
        }
    }

    fun isPDFEncrypted(path: String?): Boolean {
        var isEncrypted: Boolean
        var pdfReader: PdfReader? = null
        try {
            pdfReader = PdfReader(path)
            isEncrypted = pdfReader.isEncrypted
        } catch (e: IOException) {
            isEncrypted = true
        } finally {
            pdfReader?.close()
        }
        return isEncrypted
    }
    companion object {
        @Volatile
        private var INSTANCE: FilesRepository? = null
        fun getInstance(application: Application): FilesRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: FilesRepository(application).also { INSTANCE = it }
        }
    }
}