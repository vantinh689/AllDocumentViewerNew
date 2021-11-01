package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models

import android.graphics.Bitmap
import androidx.annotation.Keep

@Keep
data class FilePageModel(var pageNo: Int, var path: String, var isSelected: Boolean = false){
    var bitmap : Bitmap?=null
}