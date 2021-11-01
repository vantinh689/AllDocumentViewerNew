package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentInfo
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class PdfDocumentAdapter(ctxt: Context, private val documentUri: Uri) :
    ThreadedPrintDocumentAdapter(ctxt) {

    override fun buildLayoutJob(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ): LayoutJob {
        return PdfLayoutJob(oldAttributes, newAttributes, cancellationSignal, callback, extras)
    }

    override fun buildWriteJob(
        pages: Array<PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?,
        ctxt: Context?
    ): WriteJob {
        return PdfWriteJob(pages, destination, cancellationSignal, callback, ctxt)
    }

    private class PdfLayoutJob internal constructor(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?, extras: Bundle?
    ) :
        LayoutJob(oldAttributes, newAttributes, cancellationSignal, callback, extras) {
        override fun run() {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
            } else {
                val builder = PrintDocumentInfo.Builder("document.pdf")
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN).build()
                callback?.onLayoutFinished(builder.build(), !newAttributes?.equals(oldAttributes)!!)
            }
        }
    }

    private inner class PdfWriteJob internal constructor(
        pages: Array<PageRange>?, destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?, ctxt: Context?
    ) :
        WriteJob(pages, destination, cancellationSignal, callback, ctxt) {
        override fun run() {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = ctxt?.contentResolver?.openInputStream(documentUri)
                out = FileOutputStream(destination?.fileDescriptor)
                val buf = ByteArray(16384)
                var size = 0
                while (`in`?.read(buf).also {
                        if (it != null) {
                            size = it
                        }
                    }!! >= 0
                    && !cancellationSignal?.isCanceled!!
                ) {
                    out.write(buf, 0, size)
                }
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                } else {
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                }
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
                Log.e(javaClass.simpleName, "Exception printing PDF", e)
            } finally {
                try {
                    `in`?.close()
                    out?.close()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, "Exception cleaning up from printing PDF", e)
                }
            }
        }
    }

}