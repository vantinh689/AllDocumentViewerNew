package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.AppOpen
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
@Keep
class App : Application() {
    companion object{
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val appOpen = AppOpen(this)
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@App)
            modules(listOf(AppModule.getModule))
        }
     }
}
