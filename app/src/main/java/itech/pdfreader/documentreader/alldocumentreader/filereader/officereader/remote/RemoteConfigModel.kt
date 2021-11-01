package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.io.Serializable

@Keep
class AppsLink :Serializable{
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
@Keep
class CrossPromotionAppsData {
    @SerializedName("isCrossPromotionEnabled")
    @Expose
    var isCrossPromotionEnabled: Boolean? = null

    @SerializedName("AppsLinks")
    @Expose
    var appsLinks = mutableListOf<AppsLink>()
}
@Keep
class PremiumScreenData {
    @SerializedName("ShowSubscriptionScreen")
    @Expose
    var showSubscriptionScreen: Boolean? = null

    @SerializedName("ShowSubscriptionScreenAfterCount")
    @Expose
    var showSubscriptionScreenAfterCount: Int? = null

    @SerializedName("AnnualSubscriptionDiscount")
    @Expose
    var annualSubscriptionDiscount: Int? = null

    @SerializedName("ShowSubscriptionScreenCloseBtn")
    @Expose
    var showSubscriptionScreenCloseBtn: Boolean? = null
}
@Keep
class RemoteConfigModel {
    @SerializedName("PremiumScreenData")
    @Expose
    var premiumScreenData: PremiumScreenData? = null

    @SerializedName("CrossPromotionAppsData")
    @Expose
    var crossPromotionAppsData: CrossPromotionAppsData? = null

    @SerializedName("SplashScreenNative")
    @Expose
    var splashScreenNative = null
}
@Keep
internal class SplashScreenNative {
    @SerializedName("show")
    @Expose
    var show: Boolean? = null

    @SerializedName("priority")
    @Expose
    var priority: Int? = null
}