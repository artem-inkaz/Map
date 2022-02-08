package ui.smartpro.map

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ui.smartpro.map.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin  {
            androidContext(this@App)
            modules(
                arrayListOf(
                    appModule
                )
            )
        }
    }
}