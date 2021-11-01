package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.HeaderListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.HomeToolListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ToolsListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.ToolClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import androidx.annotation.Keep
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdsViewHolderClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdCallback

@Keep
class FileTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), NativeAdCallback {

    private val nativePositionArray = mutableListOf<Int>()
    private val nativeAd:Any?=null
    inner class MyViewHolder(private val itemViewBinding: ToolsListItemBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(toolClass: ToolClass) {
            itemViewBinding.apply {
                this.tool = toolClass
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(toolClass,adapterPosition)
                }
            }
        }
    }

    inner class HomeViewHolder(private val itemViewBinding: HomeToolListItemBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(toolClass: ToolClass) {
            itemViewBinding.apply {
                this.tool = toolClass
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(toolClass,adapterPosition)
                }
            }
        }
    }

    inner class HeaderViewHolder(private val itemViewBinding: HeaderListItemBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(toolClass: ToolClass) {
            itemViewBinding.apply {
                this.headerName = toolClass.text
            }
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<ToolClass>() {

        override fun areItemsTheSame(oldItem: ToolClass, newItem: ToolClass): Boolean {
            return oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: ToolClass, newItem: ToolClass): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> HeaderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.header_list_item, parent, false))
            1 -> MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.tools_list_item, parent, false))
            2 -> {
                val view2 = LayoutInflater.from(parent.context).inflate(R.layout.recycler_native_ad_item_old, parent, false)
                return NativeAdsViewHolderClass(context = parent.context,view2, R.string.native_id, this)
            }
            else -> HomeViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.home_tool_list_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position].itemType) {
            Constants.HEADER -> 0
            Constants.TOOLS -> 1
            Constants.NATIVE_AD -> 2
            else -> 3
        }
    }

    private var onItemClickListener: ((ToolClass, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        if (holder is MyViewHolder)
            holder.bindView(item)
        else if(holder is HeaderViewHolder)
            holder.bindView(item)
        else if(holder is HomeViewHolder)
            holder.bindView(item)
        else if(holder is NativeAdsViewHolderClass) {
            if (!nativePositionArray.contains(position)) {
                holder.setData(nativeAd, position, 2)
//                nativePositionArray.add(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (ToolClass, Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun onNewAdLoaded(nativeAd: Any, position: Int) {
        nativePositionArray.add(position)
    }

    override fun onAdClicked(position: Int) {

    }
}