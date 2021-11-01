package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.EOF
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.STORAGE_LOCATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


fun Context.readFilesFromDir(dir: File): MutableList<DataModel> {
    val list = mutableListOf<DataModel>()
    val pdfPattern = ".pdf"
    val listFile = dir.listFiles()
    if (listFile != null) {
        for (i in listFile.indices) {
            if (listFile[i].isDirectory) {
                readFilesFromDir(listFile[i])
                Log.e("Path", listFile[i].path)
            } else {
                if (listFile[i].name.contains(pdfPattern)) {

                    list.add(
                        getDataModelFromFile(listFile[i])
                    )
                    Log.e("Path", listFile[i].path)
                }
            }
        }
    }
    return list
}

fun Context.getFilePathFromUri(uri: Uri): String? {
    var filePath: String? = null
    try {
        val contentResolver = this.contentResolver
        val cur = contentResolver.query(
            uri, arrayOf(MediaStore.MediaColumns.DATA),
            null, null, null
        )
        if (cur != null) {
            if (cur.moveToFirst()) {
                val filePath0 = cur.getString(0)
                val exists = File(filePath0).exists()
                if (exists) {
                    filePath = filePath0
                }
            }
            try {
                cur.close()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }

    return filePath
}

fun Activity.getFileNameByUri(uri: Uri): String {
    var fileName = System.currentTimeMillis().toString()
    val cursor = contentResolver.query(uri, null, null, null, null)
    if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        fileName =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
        cursor.close()
    }
    return fileName
}

fun deleteFileFromPath(path: String) {
    File(path).delete()
}

fun Context.getDataModelFromFile(file: File, isPdfLocked: Boolean = false): DataModel {
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
    return DataModel(
        file.name,
        sizeFormatter(file.length()),
        str,
        file.path,
        isLock = isPdfLocked,
        lastModifiedTime = getFormattedTime(file.lastModified()),
        encryptedLength = file.length(),
        encryptedTime = file.lastModified()
    )
}

fun Context.isFileExist(mFileName: String, sharedPref: SharedPref): Boolean {
    val path = if (sharedPref.getString(STORAGE_LOCATION).isNullOrEmpty()) {
        getStoragePathPdf() + mFileName
    } else {
        sharedPref.getString(STORAGE_LOCATION) + mFileName
    }
    val file = File(path)
    return file.exists()
}

private fun Context.checkRepeat(finalOutputFile: String, mFile: List<File>): Int {
    var flag = true
    var append = 0
    while (flag) {
        append++
        val name: String = finalOutputFile.replace(
            getString(R.string.pdf_ext),
            "$append${getString(R.string.pdf_ext)}"
        )
        flag = mFile.contains(File(name))
    }
    return append
}


fun Context.getAllPdfFiles(): MutableList<DataModel> {
    val list = mutableListOf<DataModel>()
    val ROOT_DIR = getStoragePathPdf()
    val ANDROID_DIR = File("$ROOT_DIR/Android")
    val DATA_DIR = File("$ROOT_DIR/data")
    File(ROOT_DIR).walk()
        .onEnter {
            !it.isHidden // it is not hidden
                    && it != ANDROID_DIR // it is not Android directory
                    && it != DATA_DIR // it is not data directory
                    && !File(it, ".nomedia").exists() //there is no .nomedia file inside
        }.filter { it.extension == "pdf" }
        .toList().forEach {
            list.add(getDataModelFromFile(it))
        }
    return list
}

fun Long.millisToDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.time
}

fun Date.formatToDMY(): String {
    val df = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    return df.format(this)
}

fun getFormattedTime(lastModifiedTime: Long): String {
    return lastModifiedTime.millisToDate().formatToDMY()
}

fun Context.getFilePaths(): MutableList<DataModel> {
    val list = mutableListOf<DataModel>()
    val pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
    //Table
    val table = MediaStore.Files.getContentUri("external")
    //Column
    val column = arrayOf(
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MIME_TYPE
    )
    //Where
    val where = (MediaStore.Files.FileColumns.MIME_TYPE + "=?")
    //args
    val args = arrayOf(pdf)
    val fileCursor = contentResolver.query(table, column, where, args, null)
    val column_index_data = fileCursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    val size = fileCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
    val type = fileCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
    Log.e("type", fileCursor.moveToNext().toString() + "")
    while (fileCursor.moveToNext()) {
        Log.e("Name", fileCursor.getString(column_index_data))
        Log.e("Path", fileCursor.getString(size))
        Log.e("type", fileCursor.getString(type))
        Timber.d(fileCursor.getString(column_index_data) + "==2")
        val path = fileCursor.getString(column_index_data)
        val name = path.substring(path.lastIndexOf("/") + 1)
        val file_type = fileCursor.getString(type)
        val file_size = convertToStringRepresentation(fileCursor.getString(size).toLong())

        list.add(DataModel(name, file_size!!, file_type, path))
    }
    return list
}

private fun convertToStringRepresentation(value: Long): String? {
    val K: Long = 1024
    val M = K * K
    val G = M * K
    val T = G * K
    val dividers = longArrayOf(T, G, M, K, 1)
    val units = arrayOf("TB", "GB", "MB", "KB", "B")
    if (value < 0) {
        return ""
        //throw new IllegalArgumentException("Invalid file size: " + value);
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

/**
 * Extracts file name from the URI
 *
 * @param uri - file uri
 * @return - extracted filename
 */
fun Context.getFileName(uri: Uri): String? {
    var fileName: String? = null
    val scheme = uri.scheme ?: return null
    if (scheme == "file") {
        return uri.lastPathSegment
    } else if (scheme == "content") {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.count != 0) {
                val columnIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(columnIndex)
            }
            cursor.close()
        }
    }
    return fileName
}

/**
 * Returns the filename without its extension
 *
 * @param fileNameWithExt fileName with extension. Ex: androidDev.jpg
 * @return fileName without extension. Ex: androidDev
 */
fun stripExtension(fileNameWithExt: String?): String? {
    // Handle null case specially.
    if (fileNameWithExt == null) return null

    // Get position of last '.'.
    val pos = fileNameWithExt.lastIndexOf(".")

    // If there wasn't any '.' just return the string as is.
    return if (pos == -1) fileNameWithExt else fileNameWithExt.substring(0, pos)

    // Otherwise return the string, up to the dot.
}

fun Context.copyUriToExternalFilesDir(uri: Uri, fileDirPath: String, fileName: String): File {
    var newFile = File("$fileDirPath$fileName")
    try {
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
        val tempDir = externalCacheDir.toString()
        if (inputStream != null) {
            newFile = File("$tempDir/$fileName")
            val fos = FileOutputStream(newFile)
            val bis = BufferedInputStream(inputStream)
            val bos = BufferedOutputStream(fos)
            val byteArray = ByteArray(1024)
            var bytes = bis.read(byteArray)
            while (bytes > 0) {
                bos.write(byteArray, 0, bytes)
                bos.flush()
                bytes = bis.read(byteArray)
            }
            bos.close()
            fos.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()

    }
    return newFile
}

fun copyUriToExternalFilesDir(c: Context, uri: Uri, fileName: String?) {
    try {
        val inputStream = c.applicationContext.contentResolver.openInputStream(uri)
        val tempDir = c.externalCacheDir.toString()
        if (inputStream != null) {
            val file = File("$tempDir/$fileName")
            val fos = FileOutputStream(file)
            val bis = BufferedInputStream(inputStream)
            val bos = BufferedOutputStream(fos)
            val byteArray = ByteArray(1024)
            var bytes = bis.read(byteArray)
            while (bytes > 0) {
                bos.write(byteArray, 0, bytes)
                bos.flush()
                bytes = bis.read(byteArray)
            }
            bos.close()
            fos.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()

    }
}


fun saveCopyOfFile(srcPath: String, destPath: String): String {
    val destFile = File(destPath)
    val sourceFile = File(srcPath)
    try {
        if (!destFile.parentFile.exists()) {
            destFile.mkdirs()
        }
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        if (!sourceFile.parentFile.exists()) {
            sourceFile.mkdirs()
        }
        if (!sourceFile.exists()) {
            sourceFile.createNewFile()
        }
        val `in`: InputStream = FileInputStream(srcPath)
        val out: OutputStream = FileOutputStream(destPath)
        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    } catch (e: Exception) {
    }
    if (!destFile.parentFile.exists())
        destFile.mkdirs()
    if (!destFile.exists())
        destFile.createNewFile()
    return destPath
}

private fun format(
    value: Long,
    divider: Long,
    unit: String
): String {
    val result = if (divider > 1) value.toDouble() / divider.toDouble() else value.toDouble()
    return DecimalFormat("#,##0.#").format(result) + " " + unit
}

private const val TAG = "FilesUtils"

fun File.renameFile(newName: String): File {
    val oldFile = File(parentFile, this.name)
    val newFile = File(parentFile, newName)
    if (!newFile.exists()) {
        if (oldFile.renameTo(newFile)) {
            Log.d(TAG, "File save successful")
        } else {
            Log.d(TAG, "Rename failed")
        }
    } else {
        Log.d(TAG, "file name already exist")
    }
    return newFile
}

@Throws(IOException::class)
fun Context.from(uri: Uri): File {
    try {
        //val uri2 = ContentUris.withAppendedId(uri, 0)
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileName(uri)
        val splitName = splitFileName(fileName)
        var tempFile = File.createTempFile(splitName[0] ?: "", splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (inputStream != null) {
            copy(inputStream, out)
            inputStream.close()
        }
        out?.close()
        return tempFile
    } catch (e: Exception) {
        return File("")
    }
}

@Throws(IOException::class)
private fun copy(input: InputStream, output: OutputStream?): Long {
    var count: Long = 0
    var n: Int
    val buffer =
        ByteArray(DEFAULT_BUFFER_SIZE)
    while (EOF != input.read(buffer)
            .also { n = it }
    ) {
        output!!.write(buffer, 0, n)
        count += n.toLong()
    }
    return count
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

private fun splitFileName(fileName: String?): Array<String?> {
    var name = fileName
    var extension: String? = ""
    val i = fileName!!.lastIndexOf(".")
    if (i != -1) {
        name = fileName.substring(0, i)
        extension = fileName.substring(i)
    }
    return arrayOf(name, extension)
}

private fun rename(file: File, newName: String?): File {
    val newFile = File(file.parent, newName ?: "")
    if (newFile != file) {
        if (newFile.exists() && newFile.delete()) {
            Log.d("FileUtil", "Delete old $newName file")
        }
        if (file.renameTo(newFile)) {
            Log.d("FileUtil", "Rename file to $newName")
        }
    }
    return newFile
}

fun Context.getFileName2(uri: Uri?): String? {
    var cursor: Cursor? = null
    val projection = arrayOf(
        MediaStore.MediaColumns.DISPLAY_NAME
    )
    try {
        cursor = contentResolver.query(uri!!, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}


fun Context.createMyFilesDirs() {
    GlobalScope.launch(Dispatchers.IO) {
        val f4 = File(getStoragePathPdf())
        if (!f4.exists()) f4.mkdirs()
    }
}

fun Context.getStoragePathPdf(): String {
//    return filesDir.path + "/PdfDocuments/"
    return "/storage/emulated/0/"
}

fun Context.getPrivateStoragePathPdf(): String {
    return filesDir.path + "/PdfTempDocuments/"
}


fun Context.getStoragePathCopiedPdf(): String {
//    return filesDir.path + "/PdfDocuments/Copies/"
    return "/storage/emulated/0/PdfDocuments/Copies/"
}

fun getFormattedSize(file: File): String {
    return String.format("%.2f MB", file.length().toDouble() / (1024 * 1024))
}

fun rename(from: File, to: File): Boolean {
    return from.parentFile.exists() && from.exists() && from.renameTo(to)
}