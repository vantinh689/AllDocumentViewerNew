package com.reader.office.mychanges.slidernativead

import androidx.annotation.Keep

@Keep
data class AdModelClass(
    var title: String,
    var body: String,
    var actionBtnTxt: String,
    var imageUri: String,
    var iconImgUri: String,
    var adType: String,
)