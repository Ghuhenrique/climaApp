package br.ueg.clima

import android.app.Application
import br.ueg.clima.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClimaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ClimaApplication)
            modules(appModule)
        }
    }
}
