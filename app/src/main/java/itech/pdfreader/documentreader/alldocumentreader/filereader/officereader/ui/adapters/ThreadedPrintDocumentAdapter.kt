package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import java.util.concurrent.Executors


abstract class ThreadedPrintDocumentAdapter(private val ctxt: Context) :
    PrintDocumentAdapter() {
    private val threadPool = Executors.newFixedThreadPool(1)
    abstract fun buildLayoutJob(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ): LayoutJob?

    abstract fun buildWriteJob(
        pages: Array<PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?,
        ctxt: Context?
    ): WriteJob?

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback, extras: Bundle
    ) {
        threadPool.submit(
            buildLayoutJob(
                oldAttributes, newAttributes,
                cancellationSignal, callback, extras
            )
        )
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        threadPool.submit(buildWriteJob(pages, destination, cancellationSignal, callback, ctxt))
    }

    override fun onFinish() {
        threadPool.shutdown()
        super.onFinish()
    }

    abstract class LayoutJob internal constructor(
        var oldAttributes: PrintAttributes?,
        var newAttributes: PrintAttributes?,
        var cancellationSignal: CancellationSignal?,
        var callback: LayoutResultCallback?, var extras: Bundle?
    ) : Runnable

    abstract class WriteJob internal constructor(
        var pages: Array<PageRange>?, var destination: ParcelFileDescriptor?,
        var cancellationSignal: CancellationSignal?,
        var callback: WriteResultCallback?, var ctxt: Context?
    ) : Runnable
}