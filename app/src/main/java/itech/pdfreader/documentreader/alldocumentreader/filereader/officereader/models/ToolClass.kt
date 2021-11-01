package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models

import androidx.annotation.Keep

@Keep
data class ToolClass(
    val imgSrcResId: Int,
    val text: String,
    val colorId: Int,
    var itemType: String
)