package com.reader.office.mychanges.slidernativead

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.io.Serializable

@Keep
class AppsLinks : Serializable {
    @SerializedName("AppLink")
    @Expose
    var appLink: String? = null


    @SerializedName("AppCoverLink")
    @Expose
    var appCoverLink: String? = null

    @SerializedName("AppShortDesc")
    @Expose
    var appShortDesc: String? = null

    @SerializedName("AppLongDesc")
    @Expose
    var appLongDesc: String? = null

    @SerializedName("AppIconLink")
    @Expose
    var appIconLink: String? = null

    @SerializedName("SliderDelay")
    @Expose
    var sliderDelay: Int? = null
}