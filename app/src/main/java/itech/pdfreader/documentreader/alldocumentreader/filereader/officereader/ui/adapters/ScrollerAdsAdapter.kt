package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.AdListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.AdModelClass
import androidx.annotation.Keep
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdsViewHolderClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdCallback
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.AdViewerListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants

@Keep
class ScrollerAdsAdapter(val isViewerScreen: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), NativeAdCallback {
    private var nativeAd: Any? = null

    private var nativePositionArray = mutableListOf<Int>()

    inner class MyViewHolder(private val itemViewBinding: AdListItemBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(adModelClass: AdModelClass) {
            itemViewBinding.apply {
                this.adModel = adModelClass
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(adModelClass)
                }
            }
            itemViewBinding.tvActionBtnTitle.setOnClickListener {
                onItemClickListener?.let {
                    it(adModelClass)
                }
            }
        }
    }

    inner class MyViewHolderViewer(private val itemViewBinding: AdViewerListItemBinding) :
            RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(adModelClass: AdModelClass) {
            itemViewBinding.apply {
                this.adModel = adModelClass
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(adModelClass)
                }
            }
            itemViewBinding.tvActionBtnTitle.setOnClickListener {
                onItemClickListener?.let {
                    it(adModelClass)
                }
            }
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<AdModelClass>() {

        override fun areItemsTheSame(oldItem: AdModelClass, newItem: AdModelClass): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: AdModelClass, newItem: AdModelClass): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position].adType) {
            Constants.NATIVE_AD -> 0
            else -> 1
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                if (!isViewerScreen) {
                    val view2 = LayoutInflater.from(parent.context).inflate(R.layout.top_native_ad_list_item, parent, false)
                    NativeAdsViewHolderClass(context = parent.context, view2, R.string.native_id, this)
                } else {
                    val view2 = LayoutInflater.from(parent.context).inflate(R.layout.top_viewer_native_ad_list_item, parent, false)
                    NativeAdsViewHolderClass(context = parent.context, view2, R.string.pdfViewerScreenNativeId, this)
                }
            }
            else ->
                if (!isViewerScreen)
                    MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.ad_list_item, parent, false))
                else
                    MyViewHolderViewer(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.ad_viewer_list_item, parent, false))
        }
    }

    private var onItemClickListener: ((AdModelClass) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        if (holder is MyViewHolder)
            holder.bindView(item)
        else if (holder is MyViewHolderViewer) {
            holder.bindView(item)
        }else if (holder is NativeAdsViewHolderClass) {
            if (!nativePositionArray.contains(position)) {
                holder.setData(nativeAd, position, 2)
//                nativePositionArray.add(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (AdModelClass) -> Unit) {
        onItemClickListener = listener
    }

    override fun onNewAdLoaded(nativeAd: Any, position: Int) {
        nativePositionArray.add(position)
        this.nativeAd = nativeAd
    }

    override fun onAdClicked(position: Int) {}
}