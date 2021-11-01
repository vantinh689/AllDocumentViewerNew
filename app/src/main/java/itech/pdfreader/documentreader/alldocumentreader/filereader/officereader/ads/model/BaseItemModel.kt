package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model

import androidx.recyclerview.widget.RecyclerView


abstract class BaseItemModel {
    abstract fun itemType(): Int
    abstract fun bindItem(holder: RecyclerView.ViewHolder?, position: Int)
}