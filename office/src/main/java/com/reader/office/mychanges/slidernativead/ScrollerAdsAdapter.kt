package com.reader.office.mychanges.slidernativead

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.reader.office.mychanges.slidernativead.SliderNativeAd.Companion.NATIVE_AD
import com.reader.office.R
import com.reader.office.databinding.AdViewerListItem2Binding

class ScrollerAdsAdapter(private val currentList: MutableList<AdModelClass>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), NativeAdCallback {
    private var nativeAd: Any? = null

    private var nativePositionArray = mutableListOf<Int>()


    inner class MyViewHolderViewer(private val itemViewBinding: AdViewerListItem2Binding) :
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
        }
    }

//    private val differCallBack = object : DiffUtil.ItemCallback<AdModelClass>() {
//
//        override fun areItemsTheSame(oldItem: AdModelClass, newItem: AdModelClass): Boolean {
//            return oldItem.title == newItem.title
//        }
//
//        override fun areContentsTheSame(oldItem: AdModelClass, newItem: AdModelClass): Boolean {
//            return oldItem == newItem
//        }
//
//    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].adType) {
            NATIVE_AD -> 0
            else -> 1
        }
    }

//    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view2 = LayoutInflater.from(parent.context).inflate(R.layout.top_viewer_native_ad_list_item, parent, false)
                NativeAdsViewHolderClass(context = parent.context, view2, R.string.admobNative, this)
            }
            else ->
                MyViewHolderViewer(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.ad_viewer_list_item_2,
                        parent,
                        false
                    )
                )
        }
    }

    private var onItemClickListener: ((AdModelClass) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        if (holder is MyViewHolderViewer) {
            holder.bindView(item)
        } else if (holder is NativeAdsViewHolderClass) {
            if (!nativePositionArray.contains(position)) {
                holder.setData(nativeAd, position, 2)
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
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