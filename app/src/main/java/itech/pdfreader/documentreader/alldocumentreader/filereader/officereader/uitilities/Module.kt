package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import androidx.annotation.Keep
import com.google.firebase.analytics.FirebaseAnalytics
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdsHelperClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote.FirebaseRemoteConfigDataClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.DataViewModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.UtilsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


@Keep
object AppModule {
    val getModule = module {
        //single { DocModelRepository(get()) }
        single { SharedPref(get()) }
        single { DocumentUtils(get()) }
        single { NativeAdsHelperClass(get()) }
        single { FirebaseRemoteConfigDataClass().init() }
        single { FirebaseAnalytics.getInstance(get()) }

//        viewModel { BillingViewModel(application = get()) }
        viewModel { DataViewModel(get()) }
        viewModel { UtilsViewModel(get()) }
    }
}
