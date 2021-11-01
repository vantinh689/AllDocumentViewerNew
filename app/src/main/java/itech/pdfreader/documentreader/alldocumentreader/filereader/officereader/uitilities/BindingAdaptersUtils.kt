package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@BindingAdapter("setImageSrc")
fun loadImage(imageView: ImageView, url: String) {
    val circularProgressDrawable = CircularProgressDrawable(imageView.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(imageView)
        .load(url)
        .placeholder(circularProgressDrawable)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .into(imageView)
}
@BindingAdapter("setImageBitmapSrc")
fun loadImageFromBitmap(imageView: ImageView, bitmap: Bitmap) {
    val circularProgressDrawable = CircularProgressDrawable(imageView.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(imageView)
        .load(bitmap)
        .placeholder(circularProgressDrawable)
        .into(imageView)
}


@BindingAdapter("setImagePdfPath", "setImagePdfPageNo", "setImageBitmap")
fun loadImageFromPdfPageBitmap(
    imageView: ImageView,
    path: String,
    pageNo: Int,
    bitmap: Bitmap? = null
) {
    var file: File? = null
    GlobalScope.launch(Dispatchers.IO) {
        file = File(path)
    }.invokeOnCompletion {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val myBitmap = MyCache.getInstance().retrieveBitmapFromCache(path + "_" + pageNo)
                if (myBitmap == null) {
                    imageView.loadImageThroughBitmap()
                    file?.let { it1 ->
                        imageView.getPdfBitmapAdapter2(it1, pageNo) { bitmap ->
                            MyCache.getInstance().saveBitmapToCahche(path + "_" + pageNo, bitmap)
                            imageView.loadImageThroughBitmap(bitmap)
                        }
                    }
                } else {
                    imageView.loadImageThroughBitmap(myBitmap)
                }
            } catch (e: Exception) {
                imageView.loadImageThroughBitmap()
            }
        }
    }
}

//
//var indexList = mutableListOf<Int>()
//var bitmapList = mutableListOf<Bitmap>()
@BindingAdapter("setImagePdfPath2", "setImagePdfPageNo2", "setImageBitmap2")
fun loadImageFromPdfPageBitmap2(
    imageView: ImageView,
    path: String = "",
    pageNo: Int = 0,
    bitmap: Bitmap? = null
) {
    var file: File? = null
    GlobalScope.launch(Dispatchers.IO) {
        file = File(path)
    }.invokeOnCompletion {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val myBitmap = MyCache.getInstance().retrieveBitmapFromCache(path + "_" + pageNo)
                if (myBitmap == null) {
                    imageView.loadImageThroughBitmap()
                    file?.let { it1 ->
                        imageView.getPdfBitmapAdapter2(it1, pageNo) { bitmap ->
                            MyCache.getInstance().saveBitmapToCahche(path + "_" + pageNo, bitmap)
                            imageView.loadImageThroughBitmap(bitmap)
                        }
                    }
                } else {
                    imageView.loadImageThroughBitmap(myBitmap)
                }
            } catch (e: Exception) {
                imageView.loadImageThroughBitmap()
            }
        }
    }
}

@BindingAdapter("bookmarkImageSrc")
fun setBookmarkImage(imageView: ImageView, isBookmark: Boolean) {
    if (isBookmark)
        imageView.setImageResource(R.drawable.ic_bookamrk_star)
    else
        imageView.setImageResource(R.drawable.ic_star)
}

@BindingAdapter("lockImageSrc")
fun setLockImage(imageView: ImageView, isBookmark: Boolean) {
    if (isBookmark) {
        imageView.setImageResource(R.drawable.ic_lock)
        imageView.visibility = View.VISIBLE
    } else
        imageView.visibility = View.GONE
}

@RequiresApi(Build.VERSION_CODES.M)
@BindingAdapter("setBackground")
fun setBackground(view: View, colorId: Int) {
    view.setBackgroundColor(view.context.getColor(colorId))
}

@BindingAdapter("changeBackgroundColorOnSelect")
fun setBackgroundColor(view: View, isSelect: Boolean) {
    if (isSelect)
        view.setBackgroundColor(
            ResourcesCompat.getColor(
                view.resources,
                R.color.primaryColor,
                view.context.theme
            )
        )
    else
        view.background = null
}

@BindingAdapter("setCardViewBackground")
fun setCardViewBackground(cardView: CardView, colorId: Int) {
    cardView.setCardBackgroundColor(
        ResourcesCompat.getColor(
            cardView.resources,
            colorId,
            cardView.context.theme
        )
    )
}

@BindingAdapter("setListItemCardViewBackground")
fun setListItemCardViewBackground(cardView: CardView, name: String) {
    if (name.contains(Constants.PDF) || name.contains(".PDF"))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorPdf,
                cardView.context.theme
            )
        )
    else if (name.contains(Constants.excelExtension) || name.contains(Constants.excelWorkbookExtension) || name.contains(".excel"))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorExcel,
                cardView.context.theme
            )
        )
    else if (name.contains(Constants.PPT) || name.contains(".pptx") || name.contains(".powerpoint"))
        cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                cardView.resources,
                R.color.listItemColorPPT,
                cardView.context.theme
            )
        )
    else if (name.contains(Constants.docExtension) || name.contains(Constants.docxExtension))
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

@BindingAdapter("imageSrc")
fun setImageSrc(imageView: ImageView, resId: Int) {
    imageView.setImageResource(resId)
}

@BindingAdapter("setListItemimageSrc")
fun setListItemImageSrc(imageView: ImageView,name: String ) {
    if (name.contains(Constants.PDF) || name.contains(".PDF"))
        imageView.setImageResource(R.drawable.pdf_ic)
    else if (name.contains(Constants.excelExtension) || name.contains(Constants.excelWorkbookExtension) || name.contains(".excel") )
        imageView.setImageResource(R.drawable.excel_ic)
    else if (name.contains(Constants.PPT) || name.contains(".pptx") || name.contains(".powerpoint"))
        imageView.setImageResource(R.drawable.ppt_ic)
    else if (name.contains(Constants.docExtension) || name.contains(Constants.docxExtension))
        imageView.setImageResource(R.drawable.word_ic)
    else
        imageView.setImageResource(R.drawable.all_doc_ic)
}
