package ui.smartpro.map

import android.app.Application
import ui.smartpro.map.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                arrayListOf(
                    appModule
                )
            )
        }
    }
}