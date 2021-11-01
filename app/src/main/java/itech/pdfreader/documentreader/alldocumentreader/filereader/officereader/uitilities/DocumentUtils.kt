package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.itextpdf.text.pdf.PdfReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*

@Keep
class DocumentUtils(val context: Context) {
    /**
     * Check if a PDF at given path is encrypted
     *
     * @param path - path of PDF
     * @return true - if encrypted otherwise false
     */
    @WorkerThread
    fun isPDFEncrypted(path: String?,callback:((Boolean)->Unit)) {
        var isEncrypted = false
        GlobalScope.launch(Dispatchers.IO) {
            var pdfReader: PdfReader? = null
            try {
                pdfReader = PdfReader(path)
                isEncrypted = pdfReader.isEncrypted
            } catch (e: IOException) {
                isEncrypted = true
            } finally {
                pdfReader?.close()
            }
        }.invokeOnCompletion {
            GlobalScope.launch(Dispatchers.Main) {
                callback.invoke(isEncrypted)
            }
        }
    }
}