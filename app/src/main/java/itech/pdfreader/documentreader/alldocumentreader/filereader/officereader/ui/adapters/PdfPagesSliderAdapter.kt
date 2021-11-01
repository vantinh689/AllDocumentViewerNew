package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.PdfPageSliderItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.FilePageModel
import java.util.*

import androidx.annotation.Keep
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.MyCache
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.getPdfBitmapAdapter2
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.loadImageThroughBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@Keep
class PdfPagesSliderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyViewHolder(private val itemViewBinding: PdfPageSliderItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(filePageModel: FilePageModel) {
            itemViewBinding.apply {
                val pageNo = filePageModel.pageNo - 1
                var file: File? = null
                GlobalScope.launch(Dispatchers.IO) {
                    file = File(filePageModel.path)
                }.invokeOnCompletion {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            val myBitmap = MyCache.getInstance().retrieveBitmapFromCache(filePageModel.path + "_" + pageNo)
                            if (myBitmap == null) {
                                itemViewBinding.pageImgView.loadImageThroughBitmap()
                                file?.let { it1 ->
                                    itemViewBinding.pageImgView.getPdfBitmapAdapter2(it1, pageNo) { bitmap ->
                                        MyCache.getInstance().saveBitmapToCahche(filePageModel.path + "_" + pageNo, bitmap)
                                        itemViewBinding.pageImgView.loadImageThroughBitmap(bitmap)
                                    }
                                }
                            } else {
                                itemViewBinding.pageImgView.loadImageThroughBitmap(myBitmap)
                            }
                        } catch (e: Exception) {
                            itemViewBinding.pageImgView.loadImageThroughBitmap()
                        }
                    }
                }
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(filePageModel, adapterPosition)
                }
            }
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<FilePageModel>() {
        override fun areItemsTheSame(oldItem: FilePageModel, newItem: FilePageModel): Boolean {
            return oldItem.pageNo == newItem.pageNo
        }

        override fun areContentsTheSame(oldItem: FilePageModel, newItem: FilePageModel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.pdf_page_slider_item,
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((FilePageModel, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        if (holder is MyViewHolder)
            holder.bindView(item)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (FilePageModel, Int) -> Unit) {
        onItemClickListener = listener
    }
}